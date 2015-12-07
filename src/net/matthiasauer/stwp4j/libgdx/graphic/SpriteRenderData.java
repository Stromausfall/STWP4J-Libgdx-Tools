package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.graphics.Color;

public final class SpriteRenderData extends RenderData {
    private String textureName;

    public void set(
            String id,
            float positionX,
            float positionY,
            float rotation,
            RenderPositionUnit renderPositionUnit,
            Color tint,
            int renderOrder,
            boolean renderProjected,
            String textureName) {
        this.set(id, positionX, positionY, rotation, renderPositionUnit, tint, renderOrder, renderProjected);
        this.textureName = textureName;
    }
    
    public String getTextureName() {
        return this.textureName;
    }
}
