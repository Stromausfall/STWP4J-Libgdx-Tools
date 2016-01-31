package net.matthiasauer.stwp4j.libgdx.graphic;

public class CameraChangeEvent {
    public final int changeCameraXBy;
    public final int changeCameraYBy;
    public final double changeZoomBy;

    public CameraChangeEvent(int changeCameraXBy, int changeCameraYBy, double changeZoomBy) {
        this.changeCameraXBy = changeCameraXBy;
        this.changeCameraYBy = changeCameraYBy;
        this.changeZoomBy = changeZoomBy;
    }
}
