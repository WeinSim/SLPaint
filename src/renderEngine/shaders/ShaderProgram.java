package renderEngine.shaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;

import renderEngine.Loader;

public class ShaderProgram {

    private int programID;
    private int vertexShaderID;
    private int geometryShaderID;
    private int fragmentShaderID;

    private boolean hasGemoetryShader;

    private HashMap<String, UniformVariable> uniformVariables;
    private HashMap<String, UniformBufferObject> uniformBufferObjects;

    public ShaderProgram(String name, String[] attributeNames, int[] attributeSizes, boolean hasGemoetryShader) {
        this.hasGemoetryShader = hasGemoetryShader;

        uniformVariables = new HashMap<>();
        uniformBufferObjects = new HashMap<>();

        vertexShaderID = loadShader("res/shaders/" + name + "/vertexShader.glsl", GL20.GL_VERTEX_SHADER);
        if (hasGemoetryShader) {
            geometryShaderID = loadShader("res/shaders/" + name + "/geometryShader.glsl", GL32.GL_GEOMETRY_SHADER);
        }
        fragmentShaderID = loadShader("res/shaders/" + name + "/fragmentShader.glsl", GL20.GL_FRAGMENT_SHADER);

        programID = GL20.glCreateProgram();

        GL20.glAttachShader(programID, vertexShaderID);
        if (hasGemoetryShader) {
            GL20.glAttachShader(programID, geometryShaderID);
        }
        GL20.glAttachShader(programID, fragmentShaderID);

        if (attributeNames != null) {
            int attributeNumber = 0;
            for (int i = 0; i < attributeNames.length; i++) {
                bindAttribute(attributeNumber, attributeNames[i]);
                attributeNumber += attributeSizes[i];
            }
        }

        GL20.glLinkProgram(programID);
        GL20.glValidateProgram(programID);
        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.out.format("Could not link shader \"%s\"!\n", name);
            System.out.println(GL20.glGetProgramInfoLog(programID));
            System.exit(-1);
        }

        for (UniformVariable uniform : uniformVariables.values()) {
            uniform.setLocation(GL20.glGetUniformLocation(programID, uniform.getName()));
        }

        for (UniformBufferObject ubo : uniformBufferObjects.values()) {
            int blockIndex = GL31.glGetUniformBlockIndex(programID, ubo.getName());
            GL31.glUniformBlockBinding(programID, blockIndex, ubo.getBinding());
        }
    }

    protected void bindAttribute(int attribute, String variableName) {
        GL20.glBindAttribLocation(programID, attribute, variableName);
    }

    public void start() {
        GL20.glUseProgram(programID);
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    public void cleanUp() {
        stop();

        GL20.glDetachShader(programID, vertexShaderID);
        if (hasGemoetryShader) {
            GL20.glDetachShader(programID, geometryShaderID);
        }
        GL20.glDetachShader(programID, fragmentShaderID);

        GL20.glDeleteShader(vertexShaderID);
        if (hasGemoetryShader) {
            GL20.glDeleteShader(geometryShaderID);
        }
        GL20.glDeleteShader(fragmentShaderID);

        GL20.glDeleteProgram(programID);
    }

    public void loadUniform(String name, Object value) {
        UniformVariable uniform = uniformVariables.get(name);
        if (uniform == null) {
            throw new RuntimeException(String.format("Uniform variable \"%s\" doesn't exist!\n", name));
        }
        uniform.load(value);
    }

    public void setUniformBlockData(String name, FloatBuffer data) {
        UniformBufferObject ubo = getUniformBlock(name);
        ubo.setData(data);
    }

    public void syncUniformBlock(String name, Loader loader) {
        UniformBufferObject ubo = getUniformBlock(name);
        ubo.syncData(loader);
    }

    private UniformBufferObject getUniformBlock(String name) {
        UniformBufferObject ubo = uniformBufferObjects.get(name);
        if (ubo == null) {
            throw new RuntimeException(String.format("Uniform block \"%s\" doesn't exist!\n", name));
        }
        return ubo;
    }

    private int loadShader(String filename, int type) {
        StringBuilder shaderSource = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");

                String[] parts = line.split(" ");
                if (parts.length != 3) {
                    continue;
                }
                if (!parts[0].equals("uniform")) {
                    continue;
                }
                if (parts[2].equals("{")) {
                    // uniform buffer object
                    UniformBufferObject ubo = new UniformBufferObject(parts[1], uniformBufferObjects.size());
                    uniformBufferObjects.put(parts[1], ubo);
                } else {
                    // normal uniform variable
                    int datatype = -1;
                    for (int i = 0; i < UniformVariable.TYPE_NAMES.length; i++) {
                        if (parts[1].equals(UniformVariable.TYPE_NAMES[i])) {
                            datatype = i;
                            break;
                        }
                    }
                    if (datatype == -1) {
                        System.out.format("Invalid datatype: \"%s\"!\n", parts[1]);
                        continue;
                    }
                    String name = parts[2].replaceAll(";", "");
                    int openIndex = name.indexOf('[');
                    if (openIndex != -1) {
                        int closeIndex = name.indexOf(']');
                        String baseName = name.substring(0, openIndex);
                        int len = Integer.parseInt(name.substring(openIndex + 1, closeIndex));
                        for (int i = 0; i < len; i++) {
                            name = String.format("%s[%d]", baseName, i);
                            uniformVariables.put(name, new UniformVariable(datatype, name));
                        }
                    } else {
                        uniformVariables.put(name, new UniformVariable(datatype, name));
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.format("Could not read file \"%s\"\n!", filename);
            e.printStackTrace();
            System.exit(1);
        }

        int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.format("Could not compile shader \"%s\"!\n", filename);
            System.out.println(GL20.glGetShaderInfoLog(shaderID));
            System.exit(-1);
        }
        return shaderID;
    }
}