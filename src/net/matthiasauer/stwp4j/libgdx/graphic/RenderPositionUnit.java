package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.utils.viewport.Viewport;

public enum RenderPositionUnit {
    /*
     * 0,0 is middle 100,100 is right upper corner -100, -100 left lower corner
     */
    Percent,
    /*
     * 0,0 is middle
     */
    Pixels;

    public float translateX(Viewport viewPort, float x, float y) {
        float result = x;

        if (this == Percent) {
            result = x * viewPort.getWorldWidth() / 200;
        }

        return result;
    }

    public float translateY(Viewport viewPort, float x, float y) {
        float result = y;

        if (this == Percent) {
            result = y * viewPort.getWorldHeight() / 200;
        }

        return result;
    }
}
