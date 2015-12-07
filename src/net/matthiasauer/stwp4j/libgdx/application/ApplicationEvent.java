package net.matthiasauer.stwp4j.libgdx.application;

public abstract class ApplicationEvent {
    private ApplicationEventType applicationEventType;
    
    public ApplicationEventType getApplicationEventType() {
        return this.applicationEventType;
    }
    
    protected void setInternal(ApplicationEventType eventType) {
        this.applicationEventType = eventType;
    }
}
