package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;

class RenderedData {
	private final Rectangle renderedTarget = new Rectangle();
	private float zoomFactor;
	private RenderData renderData;
	private AtlasRegion texture;
	
	RenderedData() {
    }
	
	public RenderedData set(float x, float y, float width, float height, float zoomFactor, RenderData renderData, AtlasRegion texture) {
		this.renderedTarget.x = x;
		this.renderedTarget.y = y;
		this.renderedTarget.width = width;
		this.renderedTarget.height = height;
		this.zoomFactor = zoomFactor;
		this.renderData = renderData;
		this.texture = texture;
		
		return this;
	}
	
	public AtlasRegion getTexture() {
	    return this.texture;
	}
	
	public Rectangle getRenderedTarget() {
	    return this.renderedTarget;
	}
	
	public float getZoomFactor() {
	    return this.zoomFactor;
	}
	
	public RenderData getRenderData() {
	    return this.renderData;
	}
}
