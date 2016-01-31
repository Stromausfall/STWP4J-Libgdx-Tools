package net.matthiasauer.stwp4j.libgdx.graphic;

public class CameraStatusEvent {
    public final int cameraX;
    public final int cameraY;
    public final double zoom;

    public CameraStatusEvent(int cameraX, int cameraY, double zoom) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.zoom = zoom;
    }
}
