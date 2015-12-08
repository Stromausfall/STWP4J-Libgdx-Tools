package net.matthiasauer.stwp4j.libgdx.graphic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;

import net.matthiasauer.stwp4j.ChannelOutPort;
import net.matthiasauer.stwp4j.libgdx.utils.InputTools;

class InteractionSubProcess implements InputProcessor {
    private final Collection<InputTouchEventData> lastEvents;
    private final Set<RenderedData> renderedData;
    private final Camera camera;
    private final RenderTextureArchiveSystem archive;

    public InteractionSubProcess(Camera camera) {
        this.archive = new RenderTextureArchiveSystem();
        this.renderedData = new HashSet<RenderedData>();
        this.lastEvents = new ArrayList<InputTouchEventData>();
        this.camera = camera;

        // register this process as an input processor
        InputTools.addInputProcessor(this);
    }

    public void preIteration() {
        for (RenderedData data : this.renderedData) {
            Pools.get(RenderedData.class).free(data);
        }

        this.renderedData.clear();
    }

    public void addRenderedData(RenderedData data) {
        this.renderedData.add(data);
    }

    public void postIteration(ChannelOutPort<InputTouchEventData> outPort) {
        this.lastTouchedEntity = null;

        for (InputTouchEventData eventToProcess : this.lastEvents) {
            // find the entity that is touched by the event
            this.iterateOverAllEntitiesToFindTouched(eventToProcess);
            
            outPort.offer(eventToProcess);
        }

        this.lastEvents.clear();
    }

    private Rectangle getRectangle(boolean isProjected, RenderedData renderedComponent) {
        if (isProjected) {
            return renderedComponent.getRenderedTarget();
        } else {
            Rectangle rectangle = new Rectangle(renderedComponent.getRenderedTarget());

            // 'unzoom' the rendered rectangle - because the
            // position is also 'unzoomed' (unprojected)
            rectangle.x /= renderedComponent.getZoomFactor();
            rectangle.y /= renderedComponent.getZoomFactor();

            return rectangle;
        }
    }

    /**
     * Instead of rotating the image we only rotate the mouse position !
     * 
     * @param position
     * @param rectangle
     * @param rotation
     * @param specializationType
     */
    private Vector2 rotatePosition(Vector2 position, Rectangle rectangle, float rotation,
            Class<? extends RenderData> specializationType) {
        rotation = 360 - rotation;

        // get center of rectangle
        Vector2 center = new Vector2();
        center = rectangle.getCenter(center);

        if (specializationType == TextRenderData.class) {
            rectangle.getPosition(center);
        }

        // the arrow points from the center to the position
        // we create a new vecotr because we don't want to modify the position !
        Vector2 arrow = new Vector2(position);
        arrow.sub(center);

        // now rotate the arrow
        arrow.rotate(rotation);

        // finally attach it to the center again !
        arrow.add(center);

        return arrow;
    }

    private boolean touchesVisiblePartOfTarget(InputTouchEventData eventData, RenderedData renderedData) {
        final RenderData renderData = renderedData.getRenderData();
        final Class<? extends RenderData> specializationType = renderData.getClass();
        boolean isProjected = renderData.isRenderProjected();
        Vector2 position = eventData.getPosition(isProjected);
        Rectangle rectangle = this.getRectangle(isProjected, renderedData);

        if (renderData.getRotation() != 0) {
            // get a new 'rotated vector'
            position = this.rotatePosition(position, rectangle, renderData.getRotation(), specializationType);
        }

        // if in the bounding box
        if (rectangle.contains(position)) {
            if (specializationType == TextRenderData.class) {
                // for the text render the mouse has to be just in the rectangle
                // !
                return true;
            }

            if (specializationType == SpriteRenderData.class) {
                // for a sprite we do pixel perfect detection !
                AtlasRegion spriteTexture = renderedData.getTexture();

                if (spriteTexture == null) {
                    throw new NullPointerException("texture was null !");
                }

                if (this.isClickedPixelVisible(rectangle, spriteTexture, position)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String lastTouchedEntity;

    private String iterateOverAllEntitiesToFindTouched(InputTouchEventData eventData) {
        int orderOfCurrentTarget = -1;
        String touchedEntity = null;

        // we only need to get the entity once - because the mouse is at the
        // same position
        // at the moment of time
        if (this.lastTouchedEntity != null) {
            return this.lastTouchedEntity;
        }

        // go over all entities
        for (RenderedData renderedEvent : this.renderedData) {
            // search for the one that is touched and has the highest order of
            // the layer
            if (this.touchesVisiblePartOfTarget(eventData, renderedEvent)) {
                final int renderOrder = renderedEvent.getRenderData().getRenderOrder();
                final String id = renderedEvent.getRenderData().getId();

                if (renderOrder > orderOfCurrentTarget) {
                    eventData.setProjected(renderedEvent.getRenderData().isRenderProjected());
                    eventData.setTouchedRenderDataId(renderedEvent.getRenderData().getId());
                    orderOfCurrentTarget = renderOrder;
                    touchedEntity = id;
                }
            }
        }

        return touchedEntity;
    }

    private boolean isClickedPixelVisible(Rectangle renderedRectangle, AtlasRegion spriteTexture, Vector2 position) {
        // http://gamedev.stackexchange.com/questions/43943/how-to-detect-a-touch-on-transparent-area-of-an-image-in-a-libgdx-stage
        Pixmap pixmap = this.archive.getPixmap(spriteTexture.getTexture());

        // we want the position of the pixel in the texture !
        // first add the offset of the region inside the texture, then add the
        // position inside the texture !
        // -> because we need the position inside the texture
        int pixelX = (int) (spriteTexture.getRegionX() + position.x - renderedRectangle.x);

        // the same goes for the Y component, BUT the Y axis is inverted,
        // therefore
        // we need to invert the position INSIDE the texture !
        // --> that's why we use regionHeigth - positionInsideTexture
        int pixelY = (int) (spriteTexture.getRegionY() + spriteTexture.getRegionHeight()
                - (position.y - renderedRectangle.y));

        int pixel = pixmap.getPixel(pixelX, pixelY);

        return (pixel & 0x000000ff) != 0;
    }

    @Override
    public boolean keyDown(int keycode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        // TODO Auto-generated method stub
        return false;
    }

    private void saveEvent(int screenX, int screenY, InputTouchEventType inputType, int argument) {
        InputTouchEventData event = Pools.get(InputTouchEventData.class).obtain();

        event.set(screenX, screenY, inputType, argument, this.camera);

        this.lastEvents.add(event);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        this.saveEvent(screenX, screenY, InputTouchEventType.TouchDown, button);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        this.saveEvent(screenX, screenY, InputTouchEventType.TouchUp, button);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        this.saveEvent(screenX, screenY, InputTouchEventType.Dragged, pointer);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        this.saveEvent(screenX, screenY, InputTouchEventType.Moved, 0);
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        // TODO Auto-generated method stub
        return false;
    }
}