package net.matthiasauer.stwp4j.libgdx.application;

public class SimpleApplicationEvent extends ApplicationEvent {
    public SimpleApplicationEvent set(ApplicationEventType eventType) {
        super.setInternal(eventType);
        
        return this;
    }
}
