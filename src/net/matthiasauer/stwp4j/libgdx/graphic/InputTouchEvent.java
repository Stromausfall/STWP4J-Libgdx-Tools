package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

public class InputTouchEvent implements Poolable {
    private final Vector2 projected = new Vector2();
    private final Vector2 unprojected = new Vector2();
    private boolean isProjected;
    private int screenX;
    private int screenY;
    private String touchedRenderDataId;
    private InputTouchEventType inputType;
    private boolean isTouched;
    private int argument;
    
    public InputTouchEvent() {
    }

    public InputTouchEvent set(int screenX, int screenY, InputTouchEventType inputType, int argument, boolean isTouched, Vector2 projected,
            Vector2 unprojected) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.inputType = inputType;
        this.argument = argument;
        this.projected.set(projected);
        this.unprojected.set(unprojected);
        this.isTouched = isTouched;

        return this;
    }
    
    public int getScreenX() {
        return this.screenX;
    }
    
    public int getScreenY() {
        return this.screenY;
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

    public boolean isTouched() {
        return this.isTouched;
    }

    @Override
    public void reset() {
        this.inputType = null;
        this.argument = 0;
        this.projected.set(0, 0);
        this.unprojected.set(0, 0);
        this.isProjected = false;
        this.isTouched = false;
        this.touchedRenderDataId = null;
    }
}
