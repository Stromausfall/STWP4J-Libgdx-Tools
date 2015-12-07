package net.matthiasauer.stwp4j.libgdx.graphic;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

class RenderTextureArchiveSystem {
    private final Map<Texture, Pixmap> data =
            new HashMap<Texture, Pixmap>();
	
	public Pixmap getPixmap(Texture texture) {
	    if (!this.data.containsKey(texture)) {
	        this.add(texture);
	    }
	    
	    return this.data.get(texture);
	}
	
	public void add(Texture texture) {
        // http://gamedev.stackexchange.com/questions/43943/how-to-detect-a-touch-on-transparent-area-of-an-image-in-a-libgdx-stage
        texture.getTextureData().prepare();
        
        Pixmap pixmap =
                texture.getTextureData().consumePixmap();
        
	    this.data.put(texture, pixmap);
	}
	
	public void delete(Texture texture) {
	    Pixmap pixmap = this.getPixmap(texture);

        pixmap.dispose();

		this.data.remove(texture);
	}
}
