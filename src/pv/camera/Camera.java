package pv.camera;

import org.joml.Math;
import org.joml.Matrix4f;

import java.util.concurrent.atomic.AtomicBoolean;

public class Camera {
    // Position
    // TODO: Make the xyz a ChangingPreciseWorldPosition instead of floats
    protected float x = 0f;
    protected float y = 0f;
    protected float z = 0f;
    // Rotation
    protected float pitch = 0f; // Up-down
    protected float yaw = 0f; // Left-right
    protected final Frustum frustum;
    private final AtomicBoolean shouldUpdateView;
    private final AtomicBoolean shouldUpdateMeshes;
    protected double FOV = 90d;

    public Camera(Frustum frustum, AtomicBoolean shouldUpdateView, AtomicBoolean shouldUpdateMeshes) {
        this.frustum = frustum;
        this.shouldUpdateView = shouldUpdateView;
        this.shouldUpdateMeshes = shouldUpdateMeshes;
    }

    public void move(float mx, float my, float mz) {
        // Calculate sin and cos
        float sinYaw = Math.sin(yaw);
        float cosYaw = Math.cos(yaw);

        // Calculate movement from yaw
        float moveX = mz * sinYaw +
                mx * cosYaw;
        float moveZ = mz * cosYaw +
                mx * -sinYaw;

        // Apply movement to position
        x -= moveX;
        y -= my;
        z += moveZ;

        shouldUpdateView.set(true);
        shouldUpdateMeshes.set(true);
    }

    public void rotateX(float angle) {
        // Clamp the up-down rotation to looking straight up and looking straight down
        pitch = Math.clamp(-Math.PI_OVER_2_f, Math.PI_OVER_2_f, pitch + angle);

        shouldUpdateView.set(true);
    }

    public void rotateY(float angle) {
        yaw += angle;
        yaw %= (float) (Math.PI * 2);
        if (yaw < 0) {
            yaw += (float) (Math.PI * 2);
        }

        shouldUpdateView.set(true);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public double getFOV() {
        return FOV;
    }

    public float getNear() {
        return 0.1f;
    }

    public float getFar() {
        return 1000f;
    }

    public void updateFrustum(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        frustum.updateFrustum(projectionMatrix, viewMatrix);
    }

    public Frustum getFrustum() {
        return frustum;
    }
}