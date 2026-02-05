package renderengine.shaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import renderengine.Loader;
import renderengine.RawModel;
import renderengine.bufferobjects.AttributeVBO;
import renderengine.bufferobjects.FloatVBO;
import renderengine.bufferobjects.IntVBO;
import renderengine.bufferobjects.MatrixVBO;
import renderengine.bufferobjects.UniformBufferObject;
import renderengine.bufferobjects.VBOType;

public class ShaderProgram {

    private final String name;
    private final ShaderType type;

    private final int programID;
    private final int vertexShaderID;
    private final int geometryShaderID;
    private final int fragmentShaderID;

    private final RawModel rawModel;

    private final HashMap<String, UniformVariable> uniformVariables;
    private final HashMap<String, UniformBufferObject> uniformBufferObjects;

    private Loader loader;

    public ShaderProgram(String name, ShaderType type, Loader loader) {
        this.type = type;
        this.name = name;
        this.loader = loader;

        uniformVariables = new HashMap<>();
        uniformBufferObjects = new HashMap<>();

        ArrayList<AttributeVBO> vbos = new ArrayList<>();
        boolean hasGeometry = type.hasGeometry();
        String vertexName, geometryName = null, fragmentName;

        vertexName = String.format(type.vertexPath, name);
        if (hasGeometry)
            geometryName = String.format(type.geometryPath, name);
        fragmentName = String.format(type.fragmentPath, name);

        vertexShaderID = loadShader(vertexName, GL20.GL_VERTEX_SHADER, vbos);
        geometryShaderID = hasGeometry ? loadShader(geometryName, GL32.GL_GEOMETRY_SHADER) : 0;
        fragmentShaderID = loadShader(fragmentName, GL20.GL_FRAGMENT_SHADER);

        programID = GL20.glCreateProgram();

        GL20.glAttachShader(programID, vertexShaderID);
        if (hasGeometry)
            GL20.glAttachShader(programID, geometryShaderID);
        GL20.glAttachShader(programID, fragmentShaderID);

        int attributeNumber = 0;
        for (AttributeVBO vbo : vbos) {
            GL20.glBindAttribLocation(programID, attributeNumber, vbo.attributeName());
            attributeNumber += vbo.getNumAttributes();
        }
        rawModel = new RawModel(loader, vbos);

        GL20.glLinkProgram(programID);
        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.out.format("Could not link shader \"%s\"!\n", name);
            System.out.println(GL20.glGetProgramInfoLog(programID));
            System.exit(1);
        }
        GL20.glValidateProgram(programID);
        if (GL20.glGetProgrami(programID, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            System.out.format("Could not validate shader \"%s\"!\n", name);
            System.out.println(GL20.glGetProgramInfoLog(programID));
            System.exit(1);
        }

        for (UniformVariable uniform : uniformVariables.values()) {
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

        boolean hasGeometry = type.hasGeometry();

        GL20.glDetachShader(programID, vertexShaderID);
        if (hasGeometry)
            GL20.glDetachShader(programID, geometryShaderID);
        GL20.glDetachShader(programID, fragmentShaderID);

        GL20.glDeleteShader(vertexShaderID);
        if (hasGeometry)
            GL20.glDeleteShader(geometryShaderID);
        GL20.glDeleteShader(fragmentShaderID);

        GL20.glDeleteProgram(programID);
    }

    public void loadUniform(String name, Object value) {
        UniformVariable uniform = uniformVariables.get(name);

        if (uniform == null)
            throw new RuntimeException(
                    String.format("Shader \"%s\": uniform variable \"%s\" doesn't exist!\n", this.name, name));

        uniform.load(value);
    }

    public void loadUBOData(String uboName, ByteBuffer buffer) {
        UniformBufferObject groupAttributes = getUniformBlock(uboName);
        groupAttributes.setData(buffer);
        groupAttributes.syncData(loader);
    }

    private UniformBufferObject getUniformBlock(String name) {
        UniformBufferObject ubo = uniformBufferObjects.get(name);

        if (ubo == null)
            throw new RuntimeException(String.format("Uniform block \"%s\" doesn't exist!\n", name));

        return ubo;
    }

    public RawModel getRawModel() {
        return rawModel;
    }

    private int loadShader(String filename, int type) {
        return loadShader(filename, type, null);
    }

    private int loadShader(String filename, int type, ArrayList<AttributeVBO> vbos) {
        StringBuilder shaderSource = new StringBuilder();
        int attributeNumber = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;

            boolean insideUBO = false;
            String uboName = null;
            int uboBinding = 0;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");

                // ignore comments
                String trimmed = line.trim();
                if (trimmed.startsWith("//"))
                    continue;

                // finish UBO
                if (insideUBO) {
                    if (line.contains("}")) {
                        UniformBufferObject ubo = new UniformBufferObject(uboName, uboBinding);
                        uniformBufferObjects.put(uboName, ubo);
                        insideUBO = false;
                    }
                }

                // attributes
                if (line.startsWith("in")) {
                    String[] parts = line.split(" ");

                    if (parts.length < 3)
                        continue;
                    if (vbos == null)
                        continue;

                    String attributeName = parts[2].substring(0, parts[2].length() - 1);
                    // very crude detection for now
                    VBOType attributeType = switch (attributeName) {
                        case "offset" -> VBOType.VERTEX;
                        default -> VBOType.INSTANCE;
                    };
                    AttributeVBO vbo = switch (parts[1]) {
                        case "int" -> new IntVBO(attributeName, attributeNumber, 1, attributeType);
                        case "float" -> new FloatVBO(attributeName, attributeNumber, 1, attributeType);
                        case "vec2" -> new FloatVBO(attributeName, attributeNumber, 2, attributeType);
                        case "vec3" -> new FloatVBO(attributeName, attributeNumber, 3, attributeType);
                        case "vec4" -> new FloatVBO(attributeName, attributeNumber, 4, attributeType);
                        case "mat2" -> new MatrixVBO(attributeName, attributeNumber, 2, attributeType);
                        case "mat3" -> new MatrixVBO(attributeName, attributeNumber, 3, attributeType);
                        case "mat4" -> new MatrixVBO(attributeName, attributeNumber, 4, attributeType);
                        default -> throw new RuntimeException("Invalid attribute datatype: " + parts[1]);
                    };
                    attributeNumber += vbo.getNumAttributes();
                    vbos.add(vbo);
                }

                // uniform variables
                // uniform blocks
                int uniformIndex = line.indexOf("uniform");
                if (uniformIndex != -1) {
                    if (line.contains("{")) {
                        // uniform buffer object
                        insideUBO = true;
                        uboName = line.substring(uniformIndex + 8, line.length() - 2);
                        int bindingStrIndex = line.indexOf("binding = ");
                        if (bindingStrIndex == -1) {
                            System.err.println("Couldn't find UBO binding!");
                            continue;
                        }
                        // (only works for bindings between 0 and 9)
                        uboBinding = (int) (line.charAt(bindingStrIndex + 10) - '0');
                    } else {
                        // normal uniform variable
                        String[] parts = line.split(" ");
                        int datatype = -1;
                        for (int i = 0; i < UniformVariable.TYPE_NAMES.length; i++) {
                            if (parts[1].equals(UniformVariable.TYPE_NAMES[i])) {
                                datatype = i;
                                break;
                            }
                        }
                        if (datatype == -1) {
                            System.err.format("Invalid datatype: \"%s\"!\n", parts[1]);
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
            }
            reader.close();
        } catch (

        IOException e) {
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