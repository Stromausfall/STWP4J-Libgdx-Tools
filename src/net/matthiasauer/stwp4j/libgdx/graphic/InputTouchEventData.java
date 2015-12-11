package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

public class InputTouchEventData implements Poolable {
    private final Vector2 projected = new Vector2();
    private final Vector2 unprojected = new Vector2();
    private boolean isProjected;
    private String touchedRenderDataId;
    private InputTouchEventType inputType;
    private int argument;

    public InputTouchEventData set(InputTouchEventType inputType, int argument, Vector2 projected,
            Vector2 unprojected) {
        this.inputType = inputType;
        this.argument = argument;
        this.projected.set(projected);
        this.unprojected.set(unprojected);

        return this;
    }

    public InputTouchEventType getInputTouchEventType() {
        return this.inputType;
    }

    public int getArgument() {
        return this.argument;
    }

    public boolean isProjected() {
        return this.isProjected;
    }

    public void setProjected(boolean isProjected) {
        this.isProjected = isProjected;
    }

    public Vector2 getPosition(boolean isProjected) {
        if (isProjected) {
            return this.projected;
        } else {
            return this.unprojected;
        }
    }

    public void setTouchedRenderDataId(String touchedRenderDataId) {
        this.touchedRenderDataId = touchedRenderDataId;
    }

    public String getTouchedRenderDataId() {
        return this.touchedRenderDataId;
    }

    @Override
    public void reset() {
        this.inputType = null;
        this.argument = 0;
        this.projected.set(0, 0);
        this.unprojected.set(0, 0);
        this.isProjected = false;
        this.touchedRenderDataId = null;
    }
}
