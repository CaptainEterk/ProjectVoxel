package pv.opengl.renderer;

public class Renderer {
    private final WorldRenderer worldRenderer;
    private int renderingState = 0;

    public Renderer(WorldRenderer worldRenderer) {
        this.worldRenderer = worldRenderer;
    }

    public void render() {
        if (renderingState == 0) {
            worldRenderer.render();
        }
    }
}