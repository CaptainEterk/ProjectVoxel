package pv.util.shader;

import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL40C.glUniform1d;

public class ShaderProgram {
    private final Map<String, Integer> locationMap;
    private final int program;

    public ShaderProgram(int program) {
        this.program = program;
        this.locationMap = new HashMap<>();
    }

    public void addUniformLocation(String name) {
        locationMap.put(name, glGetUniformLocation(program, name));
    }

    public Integer getLocation(String location) {
        return locationMap.get(location);
    }

    public void setUniform(String name, int x, int y, int z) {
        glUniform3i(getLocation(name), x, y, z);
    }

    public void setUniform(String name, float x, float y, float z) {
        glUniform3f(getLocation(name), x, y, z);
    }

    public void setUniform(String name, Vector3fc vector) {
        glUniform3f(getLocation(name), vector.x(), vector.y(), vector.z());
    }

    public void setUniform(String name, float v) {
        glUniform1f(getLocation(name), v);
    }

    public void setUniform(String name, double v) {
        glUniform1d(getLocation(name), v);
    }

    public void setUniform(String name, int v) {
        glUniform1i(getLocation(name), v);
    }

    public void setUniform(String name, Matrix4f v) {
        glUniformMatrix4fv(getLocation(name), false, v.get(new float[16]));
    }

    public void setUniform(String name, float v1, float v2, float v3, float v4) {
        glUniform4f(getLocation(name), v1, v2, v3, v4);
    }

    public void bind() {
        glUseProgram(program);
    }

    public void unbind() {
        glUseProgram(0);
    }
}