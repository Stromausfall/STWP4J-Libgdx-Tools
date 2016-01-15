package net.matthiasauer.stwp4j.libgdx.ui;

public class ButtonClickEvent {
    private String id;
    
    public ButtonClickEvent set(String id) {
        this.id = id;
        
        return this;
    }
    
    public String getId() {
        return this.id;
    }
}
