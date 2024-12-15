package pv.util.position;

import pv.settings.ConstantGameSettings;

public final class ChangingPreciseWorldPosition implements Position {
    private ChangingChunkPosition chunkPosition;
    private float bx;
    private float by;
    private float bz;

    public ChangingPreciseWorldPosition(ChangingChunkPosition chunkPosition, float bx, float by, float bz) {
        this.chunkPosition = chunkPosition;
        this.bx = bx;
        this.by = by;
        this.bz = bz;
    }

    public ChangingChunkPosition getChunkPosition() {
        return chunkPosition;
    }

    public float x() {
        return bx;
    }

    public float y() {
        return by;
    }

    public float z() {
        return bz;
    }

    public void changeX(float ox) {
        this.bx += ox;
        if (bx < 0) {
            bx += ConstantGameSettings.CHUNK_WIDTH;
            chunkPosition.setX(chunkPosition.x() - 1);
        } else if (bx > ConstantGameSettings.CHUNK_WIDTH) {
            bx -= ConstantGameSettings.CHUNK_WIDTH;
            chunkPosition.setX(chunkPosition.x() + 1);
        }
    }

    public void changeY(float oy) {
        this.by += oy;
        if (by < 0) {
            by += ConstantGameSettings.CHUNK_HEIGHT;
            chunkPosition.setY(chunkPosition.y() - 1);
        } else if (by > ConstantGameSettings.CHUNK_HEIGHT) {
            by -= ConstantGameSettings.CHUNK_HEIGHT;
            chunkPosition.setY(chunkPosition.y() + 1);
        }
    }

    public void changeZ(float oz) {
        this.bz += oz;
        if (bz < 0) {
            bz += ConstantGameSettings.CHUNK_LENGTH;
            chunkPosition.setZ(chunkPosition.z() - 1);
        } else if (bz > ConstantGameSettings.CHUNK_LENGTH) {
            bz -= ConstantGameSettings.CHUNK_LENGTH;
            chunkPosition.setZ(chunkPosition.z() + 1);
        }
    }
}