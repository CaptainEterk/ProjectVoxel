package pv.settings;

public class ConstantGameSettings {
    public static final int CHUNK_WIDTH = 32; // X
    public static final int CHUNK_HEIGHT = 32; // Y
    public static final int CHUNK_LENGTH = 32; // Z
    public static final int BLOCK_CACHE_SIZE = 1000;
    public static final int NON_GENERATED_QUEUE_OVERLOAD_LIMIT = 10;
    public static final int TARGET_FPS = 60;
    public static final float FRAME_TIME = 1f / TARGET_FPS;
    // 0 keeps all the ones that were accessed LAST frame.
    public static final int MESH_REQUEST_STALE_LIMIT = 0;

    public static final String DEFAULT_WINDOW_TITLE = "Project Voxel v0.0";

    public static final int TICKS_PER_SECOND = 1;
    public static final float TICK_TIME = 1f / TICKS_PER_SECOND;
    public static final int CHUNK_PRIORITY_REFRESH_LIMIT = 10;
    public static final String SETTING_LOCATION = "run/settings";
    public static final String DEFAULT_SETTING_CONTENTS = """
            width=750
            height=750
            chunk_persistence=10
            render_distance=100""";
}