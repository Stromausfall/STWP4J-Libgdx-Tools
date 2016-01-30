package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.viewport.Viewport;

class RenderSpriteSubSystem {
    private final OrthographicCamera camera;
    private final SpriteBatch spriteBatch;
    private final TextureLoader textureLoader;
    private final InteractionSubProcess interactionSubProcess;
    private final Viewport viewPort;

    public RenderSpriteSubSystem(Viewport viewPort, TextureLoader textureLoader, OrthographicCamera camera,
            SpriteBatch spriteBatch, InteractionSubProcess interactionSubProcess) {
        this.camera = camera;
        this.viewPort = viewPort;
        this.spriteBatch = spriteBatch;
        this.textureLoader = textureLoader;
        this.interactionSubProcess = interactionSubProcess;
    }

    public void drawSprite(SpriteRenderData data) {
        final AtlasRegion texture = this.textureLoader.getTexture(data.getTextureName());
        final Color tint = data.getTint();
        float actualPositionX = data.getRenderPositionUnit().translateX(this.viewPort, data.getPosition().x,
                data.getPosition().y) - texture.getRegionWidth() / 2;
        float actualPositionY = data.getRenderPositionUnit().translateY(this.viewPort, data.getPosition().x,
                data.getPosition().y) - texture.getRegionHeight() / 2;
        float width = texture.getRegionWidth();
        float height = texture.getRegionHeight();
        float originX = width / 2;
        float originY = height / 2;

        if (!data.isRenderProjected()) {
            actualPositionX *= this.camera.zoom;
            actualPositionY *= this.camera.zoom;
            originX *= this.camera.zoom;
            originY *= this.camera.zoom;
            width *= this.camera.zoom;
            height *= this.camera.zoom;
        }

        Color base = this.spriteBatch.getColor();

        if (tint != null) {
            this.spriteBatch.setColor(tint);
        }

        this.spriteBatch.draw(texture, actualPositionX, actualPositionY, originX, originY, width, height, 1, 1,
                data.getRotation());

        if (tint != null) {
            this.spriteBatch.setColor(base);
        }

        this.interactionSubProcess.addRenderedData(new RenderedData(actualPositionX, actualPositionY,
                texture.getRegionWidth(), texture.getRegionHeight(), this.camera.zoom, data, texture));
    }

}
