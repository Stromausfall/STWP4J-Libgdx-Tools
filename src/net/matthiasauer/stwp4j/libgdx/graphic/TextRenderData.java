package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool.Poolable;

public final class TextRenderData extends RenderData implements Poolable {
    private String textString;
    private String textFont;

    public TextRenderData set(
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
        
        return this;
    }
    
    public String getTextString() {
        return this.textString;
    }
    
    public String getTextFont() {
        return this.textFont;
    }

    @Override
    public void reset() {
        this.set(null, 0, 0, 0, null, null, 0, false, null, null);
    }
}
