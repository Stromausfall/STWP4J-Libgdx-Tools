package net.matthiasauer.stwp4j.libgdx.application;

import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.ApplicationListener;

import net.matthiasauer.stwp4j.ChannelOutPort;
import net.matthiasauer.stwp4j.ChannelPortsCreated;
import net.matthiasauer.stwp4j.ChannelPortsRequest;
import net.matthiasauer.stwp4j.ExecutionState;
import net.matthiasauer.stwp4j.LightweightProcess;
import net.matthiasauer.stwp4j.PortType;
import net.matthiasauer.stwp4j.Scheduler;

public abstract class ApplicationEntryPointProcess extends LightweightProcess implements ApplicationListener {
    public static final String APPLICATION_EVENT_CHANNEL = "applicationevent-channel";
    protected final Scheduler scheduler = new Scheduler();
    private final Queue<ApplicationEvent> occuredEvents = new LinkedList<ApplicationEvent>();
    private long lastTimestep = System.currentTimeMillis();
    private ChannelOutPort<ApplicationEvent> applicationEventChannel;

    protected ApplicationEntryPointProcess() {
        super(
                new ChannelPortsRequest<ApplicationEvent>(
                        APPLICATION_EVENT_CHANNEL,
                        PortType.OutputExclusive,
                        ApplicationEvent.class));
        
        this.scheduler.addProcess(this);
    }

    @Override
    public final void resize(int width, int height) {
        this.occuredEvents.add(new ResizeApplicationEvent().set(width, height));
    }

    @Override
    public final void render() {
        long current = System.currentTimeMillis();
        long difference = current - lastTimestep;
        lastTimestep = current;
        
        this.occuredEvents.add(new RenderApplicationEvent().set(difference / 1000d));
        
        this.scheduler.performIteration();
    }

    @Override
    public final void pause() {
        this.occuredEvents.add(new SimpleApplicationEvent().set(ApplicationEventType.PAUSE));
    }

    @Override
    public final void resume() {
        this.occuredEvents.add(new SimpleApplicationEvent().set(ApplicationEventType.RESUME));
    }

    @Override
    public final void dispose() {
        this.occuredEvents.add(new SimpleApplicationEvent().set(ApplicationEventType.DISPOSE));
    }

    @Override
    protected final ExecutionState execute() {
        // forward ALL events
        for (ApplicationEvent applicationEvent : this.occuredEvents) {
            applicationEventChannel.offer(applicationEvent);
        }
        
        return ExecutionState.Finished;
    }

    @Override
    protected final void initialize(ChannelPortsCreated createdChannelPorts) {
        this.applicationEventChannel = createdChannelPorts.getChannelOutPort(APPLICATION_EVENT_CHANNEL, ApplicationEvent.class);
    }
}
