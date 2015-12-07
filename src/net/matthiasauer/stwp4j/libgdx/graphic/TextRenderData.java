package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.graphics.Color;

public final class TextRenderData extends RenderData {
    private String textString;
    private String textFont;

    public void set(
            String id,
            float positionX,
            float positionY,
            float rotation,
            RenderPositionUnit renderPositionUnit,
            Color tint,
            int renderOrder,
            boolean renderProjected,
            String textString,
            String textFont) {
        this.set(id, positionX, positionY, rotation, renderPositionUnit, tint, renderOrder, renderProjected);
        this.textString = textString;
        this.textFont = textFont;
    }
    
    public String getTextString() {
        return this.textString;
    }
    
    public String getTextFont() {
        return this.textFont;
    }
}
