package net.matthiasauer.stwp4j.libgdx.application;

import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.ApplicationListener;

import net.matthiasauer.stwp4j.ChannelOutPort;
import net.matthiasauer.stwp4j.LightweightProcess;
import net.matthiasauer.stwp4j.Scheduler;

public abstract class ApplicationEntryPointProcess extends LightweightProcess implements ApplicationListener {
    public static final String APPLICATION_EVENT_CHANNEL = "applicationevent-channel";
    protected final Scheduler scheduler = new Scheduler();
    private final Queue<ApplicationEvent> occuredEvents = new LinkedList<ApplicationEvent>();
    private final ChannelOutPort<ApplicationEvent> applicationEventChannel;
    private long lastTimestep = System.currentTimeMillis();

    protected ApplicationEntryPointProcess(ChannelOutPort<ApplicationEvent> applicationEventChannel) {
        this.applicationEventChannel = applicationEventChannel;
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
    protected final void execute() {
        // forward ALL events
        for (ApplicationEvent applicationEvent : this.occuredEvents) {
            applicationEventChannel.offer(applicationEvent);
        }
        
        this.occuredEvents.clear();
    }
}
