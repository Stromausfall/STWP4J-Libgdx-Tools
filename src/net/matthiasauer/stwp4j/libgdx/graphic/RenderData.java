package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public abstract class RenderData {
    private String id;
    private final Vector2 position = new Vector2();
    private float rotation;
    private RenderPositionUnit renderPositionUnit;
    private Color tint;
    private int renderOrder;
    private boolean renderProjected;
    
    protected void set(
            String id,
            float positionX,
            float positionY,
            float rotation,
            RenderPositionUnit renderPositionUnit,
            Color tint,
            int renderOrder,
            boolean renderProjected) {
        this.position.set(positionX, positionY);
        this.rotation = rotation;
        this.renderPositionUnit = renderPositionUnit;
        this.tint = tint;
        this.renderOrder = renderOrder;
        this.renderProjected = renderProjected;
        this.id = id;
    }
    
    public String getId() {
        return this.id;
    }

    public Vector2 getPosition() {
        return this.position;
    }
    
    public float getRotation() {
        return this.rotation;
    }
    
    public RenderPositionUnit getRenderPositionUnit() {
        return this.renderPositionUnit;
    }
    
    public Color getTint() {
        return this.tint;
    }
    
    public int getRenderOrder() {
        return this.renderOrder;
    }
    
    public boolean isRenderProjected() {
        return this.renderProjected;
    }
}
