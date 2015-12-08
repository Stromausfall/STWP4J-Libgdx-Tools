package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

public class InputTouchEventData implements Poolable {
    private final Vector2 projected = new Vector2();
    private final Vector2 unprojected = new Vector2();
    private final Vector3 temp = new Vector3();
    private boolean isProjected;
    private String touchedRenderDataId;
    private int screenX;
    private int screenY;
    private InputTouchEventType inputType;
    private int argument;

    public InputTouchEventData set(int screenX, int screenY, InputTouchEventType inputType, int argument,
            Camera camera) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.inputType = inputType;
        this.argument = argument;

        this.calculatePositions(camera);

        return this;
    }
    
    public InputTouchEventType getInputTouchEventType() {
        return this.inputType;
    }
    
    public int getArgument() {
        return this.argument;
    }

    private void calculatePositions(Camera camera) {
        temp.x = screenX;
        temp.y = screenY;
        temp.z = 0;

        camera.unproject(temp);

        projected.x = temp.x;
        projected.y = temp.y;
        unprojected.x = screenX - (Gdx.graphics.getWidth() / 2);
        unprojected.y = (Gdx.graphics.getHeight() / 2) - screenY;
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
        this.screenX = 0;
        this.screenY = 0;
        this.inputType = null;
        this.argument = 0;
        this.projected.set(0, 0);
        this.unprojected.set(0, 0);
        this.isProjected = false;
        this.touchedRenderDataId = null;
    }
}
