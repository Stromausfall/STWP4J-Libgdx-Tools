package net.matthiasauer.stwp4j.libgdx.application;

public class ResizeApplicationEvent extends ApplicationEvent {
    private int width;
    private int height;
    
    public ResizeApplicationEvent set(int width, int height) {
        super.setInternal(ApplicationEventType.RESIZE);
        
        this.width = width;
        this.height = height;
        
        return this;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }
}
