package net.matthiasauer.stwp4j.libgdx.application;

public class RenderApplicationEvent extends ApplicationEvent {
    private double deltaTime;
    
    public RenderApplicationEvent set(double deltaTime) {
        this.setInternal(ApplicationEventType.RENDER);
        this.deltaTime = deltaTime;
        
        return this;
    }
    
    public double getDeltaTime() {
        return this.deltaTime;
    }
}
