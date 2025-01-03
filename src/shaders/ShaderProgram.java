package shaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public class ShaderProgram {

    private int programID;
    private int vertexShaderID;
    private int geometryShaderID;
    private int fragmentShaderID;

    private boolean hasGemoetryShader;

    private HashMap<String, UniformVariable> uniforms;

    public ShaderProgram(String name, String[] attributeNames, boolean hasGemoetryShader) {
        this.hasGemoetryShader = hasGemoetryShader;
        uniforms = new HashMap<>();

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
            for (int i = 0; i < attributeNames.length; i++) {
                bindAttribute(i, attributeNames[i]);
            }
        }

        GL20.glLinkProgram(programID);
        GL20.glValidateProgram(programID);

        getAllUniformLocations();
    }

    protected void bindAttribute(int attribute, String variableName) {
        GL20.glBindAttribLocation(programID, attribute, variableName);
    }

    protected void getAllUniformLocations() {
        for (UniformVariable uniform : uniforms.values()) {
            uniform.setLocation(GL20.glGetUniformLocation(programID, uniform.getName()));
        }
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
        UniformVariable uniform = uniforms.get(name);
        if (uniform == null) {
            System.out.format("Uniform variable \"%s\" doesn't exist!\n", name);
            System.exit(1);
        }
        uniform.load(value);
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
                uniforms.put(name, new UniformVariable(datatype, name));
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