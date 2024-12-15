package pv.world.util;

import pv.settings.ConstantGameSettings;
import pv.world.structure.block.BlockFace;
import pv.world.structure.vertex.Vertex;

public class ShapeHelper {
    public static final float PIXEL = 0.0625f;         // Precision of 0.0625 (0.25 / 4)
    private static final int MAX_PACKED_VALUE = 1023;      // Maximum value for 10-bit (2^10 - 1)

    private static final int BITMASK_4 = 0xF;  // 4-bit mask for RGB
    private static final int BITMASK_3 = 0x7;  // 3-bit mask for normal
    private static final int BITMASK_8 = 0xFF; // 8-bit mask for UV
    private static final int MAX_UV_SIZE = 255; // Max UV size for normalization

    public static int packData1(Vertex vertex) {
        // Without the rounding, float imprecision makes this not work
        float x = vertex.px() * PIXEL;
        float y = vertex.py() * PIXEL;
        float z = vertex.pz() * PIXEL;

        // Scale values to fit into 10-bit space (0 to 1023)
        int ix = Math.round(x * MAX_PACKED_VALUE / ConstantGameSettings.CHUNK_WIDTH);  // Scale from 0-32 to 0-1023
        int iy = Math.round(y * MAX_PACKED_VALUE / ConstantGameSettings.CHUNK_HEIGHT);  // Scale from 0-32 to 0-1023
        int iz = Math.round(z * MAX_PACKED_VALUE / ConstantGameSettings.CHUNK_LENGTH);  // Scale from 0-32 to 0-1023

        // Pack the integers into a 32-bit integer: [X(10 bits), Y(10 bits), Z(10 bits), (last 2 bits unused)]
        return (ix << 22) | (iy << 12) | (iz << 2);
    }

    public static int packData2(float r, float g, float b, BlockFace blockFace, int u, int v) {
        // Normalize RGB between 0 and 15 (4 bits each)
        int rPacked = (int) (r * BITMASK_4) & BITMASK_4;
        int gPacked = (int) (g * BITMASK_4) & BITMASK_4;
        int bPacked = (int) (b * BITMASK_4) & BITMASK_4;

        // Pack normal (3 bits)
        int normalPacked = blockFace.ordinal() & BITMASK_3;

        // Pack UV coordinates (8 bits each)
        int uPacked = u & BITMASK_8;
        int vPacked = v & BITMASK_8;

        // Combine all parts into a single packed integer
        return (rPacked << 28)  // Pack R into the top 4 bits
                | (gPacked << 24)  // Pack G into the next 4 bits
                | (bPacked << 20)  // Pack B into the next 4 bits
                | (normalPacked << 17)  // Pack normal into the next 3 bits
                | (uPacked << 9)  // Pack U into the next 8 bits
                | (vPacked << 1);  // Pack V into the lowest 8 bits
    }
}