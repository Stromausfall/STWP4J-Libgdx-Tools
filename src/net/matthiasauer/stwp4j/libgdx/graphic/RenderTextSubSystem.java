package net.matthiasauer.stwp4j.libgdx.graphic;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

import net.matthiasauer.stwp4j.libgdx.utils.Pair;

class RenderTextSubSystem { 
	private final Map<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();
	private final BitmapFont defaultFont = new BitmapFont(); 
	private final OrthographicCamera camera;
	private final SpriteBatch spriteBatch;
	private final InteractionSubProcess interactionSubProcess;
	private Map<Pair<String, String>, GlyphLayout> current;
	private Map<Pair<String, String>, GlyphLayout> last;
	
	public RenderTextSubSystem(
			OrthographicCamera camera,
			SpriteBatch spriteBatch,
			InteractionSubProcess interactionSubProcess) {
		this.camera = camera;
		this.spriteBatch = spriteBatch;
		this.current = new HashMap<Pair<String,String>, GlyphLayout>();
		this.last = new HashMap<Pair<String,String>, GlyphLayout>();
		this.interactionSubProcess = interactionSubProcess;
	}
	
	public void preIteration() {
		// switch current and last !
		// then clear the last one !
		// in this iteration we then fill the last one
		// therefore we can reuse objects from last turn and forget
		// them if they are not used !
		Map<Pair<String, String>, GlyphLayout> temp = this.last;
		this.last = this.current;
		this.current = temp;
		this.current.clear();
	}
	
	private GlyphLayout getCachedGlyphLayout(TextRenderData data, BitmapFont font) {
		Pair<String, String> key =
				Pair.of(data.getTextFont(), data.getTextString());
		GlyphLayout glyphLayout = this.last.get(key);
		
		if (glyphLayout == null) {
			glyphLayout = new GlyphLayout(font, data.getTextString());
			this.current.put(key, glyphLayout);
		}
		
		return glyphLayout;
	}
	
	private BitmapFont getCachedBitmapFont(TextRenderData data) {
		BitmapFont font = this.defaultFont;
		final String textFont = data.getTextFont();
		
		if (textFont != null) {
			font = this.fonts.get(textFont);
			
			if (font == null) {
				// if we don't have a cache
				font =
						new BitmapFont(
								Gdx.files.internal(textFont + ".fnt"),
								Gdx.files.internal(textFont + ".png"),
								false);
				
				this.fonts.put(textFont, font);
			}
		}
		
		return font;
	}

	public void drawText(TextRenderData data) {
	    final Color tint = data.getTint();
		float actualPositionX =
		        data.getRenderPositionUnit().translateX(
				        data.getPosition().x,
				        data.getPosition().y);
		float actualPositionY =
		        data.getRenderPositionUnit().translateY(
                        data.getPosition().x,
                        data.getPosition().y);
		
		if (!data.isRenderProjected()) {
			actualPositionX *= this.camera.zoom;
			actualPositionY *= this.camera.zoom;
		}
		
		BitmapFont font = this.getCachedBitmapFont(data);
		GlyphLayout glyphLayout = this.getCachedGlyphLayout(data, font);
		
		if (tint != null) {
			font.setColor(tint);
		} else {
			font.setColor(Color.BLACK);
		}
		
		Matrix4 rotationMatrix = new Matrix4();
		Matrix4 oldMatrix = this.spriteBatch.getTransformMatrix().cpy();
		rotationMatrix.idt();
		
		Vector3 centerOfRotation =
				new Vector3(
						actualPositionX,
						actualPositionY,
						0);

		rotationMatrix.rotate(new Vector3(0, 0, 1), data.getRotation());
		rotationMatrix.trn(centerOfRotation);
		
		spriteBatch.end();
		this.spriteBatch.setTransformMatrix(rotationMatrix);
		spriteBatch.begin();

		font.draw(
				this.spriteBatch,
				data.getTextString(),
				0,
				0);

		spriteBatch.end();
		this.spriteBatch.setTransformMatrix(oldMatrix);
		spriteBatch.begin();

		this.interactionSubProcess.addRenderedData(
                Pools.get(RenderedData.class).obtain().set(
        		        actualPositionX,
        		        actualPositionY - glyphLayout.height,
        		        glyphLayout.width,
        		        glyphLayout.height,
        		        this.camera.zoom,
        		        data,
        		        null));
	}
}
