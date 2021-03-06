package net.matthiasauer.stwp4j.libgdx.graphic;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.matthiasauer.stwp4j.ChannelInPort;
import net.matthiasauer.stwp4j.ChannelOutPort;
import net.matthiasauer.stwp4j.LightweightProcess;
import net.matthiasauer.stwp4j.libgdx.application.ApplicationEvent;
import net.matthiasauer.stwp4j.libgdx.application.ApplicationEventType;
import net.matthiasauer.stwp4j.libgdx.application.ResizeApplicationEvent;

public final class RenderProcess extends LightweightProcess {
    public static enum ResizeBehavior {
        /**
         * Keep the resolution - resizing stretches the content
         */
        KeepResolution,
        /**
         * Changes the resolution - the absolute size of the displayed content
         * stays the same
         */
        ChangeResolution,
        /**
         * Like KeepResolution but the aspect ratio is kept
         */
        KeepResolutionKeepAspect,
        /**
         * Like ChangeResolution but the aspect ratio is kept
         */
        ChangeResolutionKeepAspect
    }

    /**
     * 11 is the default size internally so start with that initial capacity
     */
    private static final int sortedRenderComponentsInitialSize = 11;
    public static final String RENDERDATA_CHANNEL = "renderdata-channel";
    public static final String INPUTTOUCHEVENTDATA_CHANNEL = "inputtoucheventdata-channel";
    public final OrthographicCamera camera;
    private final SpriteBatch spriteBatch;
    private final Queue<RenderData> sortedRenderComponents;
    private final RenderSpriteSubSystem renderSpriteSubSystem;
    private final RenderTextSubSystem renderTextSubSystem;
    private final Viewport viewport;
    private final InteractionSubProcess interactionSubProcess;
    private final boolean createInputTouchEvents;
    private final ChannelInPort<RenderData> renderDataChannel;
    private final ChannelInPort<ApplicationEvent> applicationEventChannel;
    private final ChannelOutPort<InputTouchEvent> inputTouchEventDataChannel;
    private final ChannelInPort<CameraChangeEvent> cameraChangeEventChannel;
    private final ChannelOutPort<CameraStatusEvent> cameraStatusEventChannel;
    private final ResizeBehavior resizeBehavior;
    private final int initalCameraWidth;
    private final int initalCameraHeight;

    /**
     * Creates the RenderProcess
     * 
     * @param atlasFilePaths
     *            a list of file paths pointing to texture atlases
     * @param createInputTouchEvents
     *            indicates whether InputTouchEvents should be created submitted
     *            to the RENDERDATA_CHANNEL channel
     * @param initialCameraWidth
     *            the initial width the camera will have
     * @param initialCameraHeight
     *            the initial height the camera will have
     * @param resizeBehavior
     *            determines the resize behaviour
     * @param renderDataChannel
     *            receives the RenderData instances that will be rendered
     * @param applicationEventChannel
     *            receives the events from the Application - f.e. resize
     * @param inputTouchEventDataChannel
     *            produces input events generated from a mouse or touch input
     * @param cameraChangeEventChannel
     *            channel that allows changing the camera
     * @param cameraStatusEventChannel
     *            produces events after changing the camera that return the
     *            status of the camera
     */
    public RenderProcess(List<String> atlasFilePaths, boolean createInputTouchEvents, int initialCameraWidth,
            int initialCameraHeight, ResizeBehavior resizeBehavior, ChannelInPort<RenderData> renderDataChannel,
            ChannelInPort<ApplicationEvent> applicationEventChannel,
            ChannelOutPort<InputTouchEvent> inputTouchEventDataChannel,
            ChannelInPort<CameraChangeEvent> cameraChangeEventChannel,
            ChannelOutPort<CameraStatusEvent> cameraStatusEventChannel) {
        this.initalCameraHeight = initialCameraHeight;
        this.initalCameraWidth = initialCameraWidth;
        this.inputTouchEventDataChannel = inputTouchEventDataChannel;
        this.applicationEventChannel = applicationEventChannel;
        this.renderDataChannel = renderDataChannel;
        this.resizeBehavior = resizeBehavior;
        this.cameraChangeEventChannel = cameraChangeEventChannel;
        this.cameraStatusEventChannel = cameraStatusEventChannel;

        this.createInputTouchEvents = createInputTouchEvents;
        this.camera = new OrthographicCamera(this.initalCameraWidth, this.initalCameraHeight);
        this.viewport = new ScreenViewport(camera);
        this.spriteBatch = new SpriteBatch();
        this.interactionSubProcess = new InteractionSubProcess(this.camera, this.viewport);
        this.renderSpriteSubSystem = new RenderSpriteSubSystem(this.viewport, new TextureLoader(atlasFilePaths),
                this.camera, this.spriteBatch, this.interactionSubProcess);
        this.renderTextSubSystem = new RenderTextSubSystem(this.viewport, this.camera, this.spriteBatch,
                this.interactionSubProcess);

        // create the PriorityQueue with the custom comparator and an initial
        // size
        this.sortedRenderComponents = new PriorityQueue<RenderData>(sortedRenderComponentsInitialSize,
                new RenderDataComparator());
    }

    private void handleRenderDataChannel() {
        RenderData data = null;

        while ((data = this.renderDataChannel.poll()) != null) {
            this.sortedRenderComponents.add(data);
        }
    }

    private void handleApplicationEventChannel() {
        ApplicationEvent event = null;

        while ((event = this.applicationEventChannel.poll()) != null) {
            if (event.getApplicationEventType() == ApplicationEventType.RESIZE) {
                ResizeApplicationEvent resizeEvent = (ResizeApplicationEvent) event;

                // save camera settings
                final float preResizeZoom = this.camera.zoom;
                final Vector3 preResizePosition = new Vector3(this.camera.position);

                switch (this.resizeBehavior) {
                case KeepResolution:
                    // do nothing
                    this.viewport.setWorldSize(this.initalCameraWidth, this.initalCameraHeight);
                    this.viewport.setScreenSize(resizeEvent.getWidth(), resizeEvent.getHeight());
                    this.viewport.apply(true);
                    break;
                case ChangeResolution:
                    // change resolution
                    this.viewport.setWorldSize(resizeEvent.getWidth(), resizeEvent.getHeight());
                    this.viewport.update(resizeEvent.getWidth(), resizeEvent.getHeight());
                    this.viewport.apply(true);
                    break;
                case KeepResolutionKeepAspect:
                    // keep resolution but also keep the aspect
                    Vector2 size = Scaling.fit.apply(this.initalCameraWidth, this.initalCameraHeight,
                            resizeEvent.getWidth(), resizeEvent.getHeight());
                    int viewportX = (int) (resizeEvent.getWidth() - size.x) / 2;
                    int viewportY = (int) (resizeEvent.getHeight() - size.y) / 2;
                    int viewportWidth = (int) size.x;
                    int viewportHeight = (int) size.y;

                    this.viewport.setScreenPosition(viewportX, viewportY);
                    this.viewport.setScreenSize(viewportWidth, viewportHeight);
                    this.viewport.setWorldSize(this.initalCameraWidth, this.initalCameraHeight);
                    this.viewport.apply(true);
                    break;
                case ChangeResolutionKeepAspect:
                    // change resolution but also keep the aspect
                    Vector2 size2 = Scaling.fit.apply(this.initalCameraWidth, this.initalCameraHeight,
                            resizeEvent.getWidth(), resizeEvent.getHeight());
                    int viewportX2 = (int) (resizeEvent.getWidth() - size2.x) / 2;
                    int viewportY2 = (int) (resizeEvent.getHeight() - size2.y) / 2;
                    int viewportWidth2 = (int) size2.x;
                    int viewportHeight2 = (int) size2.y;

                    this.viewport.setScreenPosition(viewportX2, viewportY2);
                    this.viewport.setScreenSize(viewportWidth2, viewportHeight2);
                    this.viewport.setWorldSize(viewportWidth2, viewportHeight2);
                    this.viewport.apply(true);
                    break;
                default:
                    break;
                }

                // restore camera settings
                this.camera.position.set(preResizePosition);
                this.camera.zoom = preResizeZoom;
                this.camera.update();
            }
        }
    }

    private void handleCameraChangeEventChannel() {
        CameraChangeEvent event = null;

        while ((event = this.cameraChangeEventChannel.poll()) != null) {
            this.camera.translate(event.changeCameraXBy, event.changeCameraYBy);
            this.camera.zoom += event.changeZoomBy;
            this.camera.update();

            this.cameraStatusEventChannel.offer(new CameraStatusEvent((int) this.camera.position.x,
                    (int) this.camera.position.y, this.camera.zoom));
        }
    }

    @Override
    protected void execute() {
        this.handleRenderDataChannel();
        this.handleApplicationEventChannel();
        this.handleCameraChangeEventChannel();
    }

    @Override
    protected void preIteration() {
        this.sortedRenderComponents.clear();
        this.renderTextSubSystem.preIteration();

        if (this.createInputTouchEvents) {
            this.interactionSubProcess.preIteration();
        }
    }

    @Override
    protected void postIteration() {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        final float originalZoom = this.camera.zoom;
        this.spriteBatch.begin();
        this.changeProjection(true, originalZoom);
        boolean lastProjectedValue = true;

        // iterate over the keys in order (that's what the treemap is for)
        while (!this.sortedRenderComponents.isEmpty()) {
            RenderData baseRenderComponent = this.sortedRenderComponents.poll();
            final boolean projected = baseRenderComponent.isRenderProjected();

            if (lastProjectedValue != projected) {
                lastProjectedValue = projected;

                this.changeProjection(projected, originalZoom);
            }

            if (baseRenderComponent instanceof SpriteRenderData) {
                this.renderSpriteSubSystem.drawSprite((SpriteRenderData) baseRenderComponent);
                continue;
            }

            if (baseRenderComponent instanceof TextRenderData) {
                this.renderTextSubSystem.drawText((TextRenderData) baseRenderComponent);
                continue;
            }

            throw new NullPointerException("Unknown specialization of the BaseRenderComponent !");
        }

        this.spriteBatch.end();

        this.camera.zoom = originalZoom;
        this.camera.update();

        if (this.createInputTouchEvents) {
            this.interactionSubProcess.postIteration(this.inputTouchEventDataChannel);
        }
    }

    private void changeProjection(boolean renderProjected, float originalZoom) {
        // end
        this.spriteBatch.end();

        if (renderProjected) {
            this.camera.zoom = originalZoom;
            this.camera.update();

            this.spriteBatch.setProjectionMatrix(this.camera.combined);
        } else {
            this.camera.zoom = 1;
            this.camera.update();

            this.spriteBatch.setProjectionMatrix(this.camera.projection);
        }

        // start new batch
        this.spriteBatch.begin();
    }
}
