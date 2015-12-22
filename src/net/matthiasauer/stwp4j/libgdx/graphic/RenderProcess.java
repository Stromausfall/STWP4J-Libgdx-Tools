package net.matthiasauer.stwp4j.libgdx.graphic;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.matthiasauer.stwp4j.ChannelInPort;
import net.matthiasauer.stwp4j.ChannelOutPort;
import net.matthiasauer.stwp4j.ChannelPortsCreated;
import net.matthiasauer.stwp4j.ChannelPortsRequest;
import net.matthiasauer.stwp4j.LightweightProcess;
import net.matthiasauer.stwp4j.PortType;
import net.matthiasauer.stwp4j.libgdx.application.ApplicationEntryPointProcess;
import net.matthiasauer.stwp4j.libgdx.application.ApplicationEvent;
import net.matthiasauer.stwp4j.libgdx.application.ApplicationEventType;
import net.matthiasauer.stwp4j.libgdx.application.ResizeApplicationEvent;

public final class RenderProcess extends LightweightProcess {
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
    private ChannelInPort<RenderData> renderDataChannel;
    private ChannelInPort<ApplicationEvent> applicationEventChannel;
    private ChannelOutPort<InputTouchEventData> inputTouchEventDataChannel;

    /**
     * Creates the RenderProcess
     * 
     * @param atlasFilePaths
     *            a list of file paths pointing to texture atlases
     * @param createInputTouchEvents
     *            indicates whether InputTouchEvents should be created submitted
     *            to the RENDERDATA_CHANNEL channel
     */
    public RenderProcess(List<String> atlasFilePaths, boolean createInputTouchEvents) {
        super(new ChannelPortsRequest<RenderData>(RENDERDATA_CHANNEL, PortType.InputExclusive, RenderData.class),
                new ChannelPortsRequest<ApplicationEvent>(ApplicationEntryPointProcess.APPLICATION_EVENT_CHANNEL,
                        PortType.InputMultiplex, ApplicationEvent.class),
                new ChannelPortsRequest<InputTouchEventData>(INPUTTOUCHEVENTDATA_CHANNEL, PortType.OutputExclusive,
                        InputTouchEventData.class));

        this.createInputTouchEvents = createInputTouchEvents;
        this.camera = new OrthographicCamera(800, 600);
        this.viewport = new ScreenViewport(camera);
        this.spriteBatch = new SpriteBatch();
        this.interactionSubProcess = new InteractionSubProcess(this.camera);
        this.renderSpriteSubSystem = new RenderSpriteSubSystem(new TextureLoader(atlasFilePaths), this.camera,
                this.spriteBatch, this.interactionSubProcess);
        this.renderTextSubSystem = new RenderTextSubSystem(this.camera, this.spriteBatch, this.interactionSubProcess);

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

                this.viewport.update(resizeEvent.getWidth(), resizeEvent.getHeight());
            }
        }
    }

    @Override
    protected void execute() {
        this.handleRenderDataChannel();
        this.handleApplicationEventChannel();
    }

    @Override
    protected void initialize(ChannelPortsCreated createdChannelPorts) {
        this.renderDataChannel = createdChannelPorts.getChannelInPort(RENDERDATA_CHANNEL, RenderData.class);
        this.applicationEventChannel = createdChannelPorts
                .getChannelInPort(ApplicationEntryPointProcess.APPLICATION_EVENT_CHANNEL, ApplicationEvent.class);
        this.inputTouchEventDataChannel = createdChannelPorts.getChannelOutPort(INPUTTOUCHEVENTDATA_CHANNEL,
                InputTouchEventData.class);
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

        this.spriteBatch.begin();
        this.changeProjection(true);
        boolean lastProjectedValue = true;
        final float originalZoom = this.camera.zoom;

        // iterate over the keys in order (that's what the treemap is for)
        while (!this.sortedRenderComponents.isEmpty()) {
            RenderData baseRenderComponent = this.sortedRenderComponents.poll();
            final boolean projected = baseRenderComponent.isRenderProjected();

            if (lastProjectedValue != projected) {
                lastProjectedValue = projected;

                this.changeProjection(projected);
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

    private void changeProjection(boolean renderProjected) {
        // end
        this.spriteBatch.end();

        if (renderProjected) {
            this.camera.zoom = this.camera.zoom;
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
