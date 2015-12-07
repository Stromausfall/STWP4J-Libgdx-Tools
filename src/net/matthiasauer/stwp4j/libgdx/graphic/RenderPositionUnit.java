package net.matthiasauer.stwp4j.libgdx.graphic;

import com.badlogic.gdx.Gdx;

public enum RenderPositionUnit {
	/*
	 * 0,0 is middle
	 * 100,100 is right upper corner
	 * -100, -100 left lower corner
	 */
	Percent,
	/*
	 * 0,0 is middle
	 */
	Pixels;
    
    public float translateX(float x, float y) {
        float result = x;
        
        if (this == Percent) {
            result =
                    x * Gdx.graphics.getWidth() / 200;
        }
        
        return result;
    }
    
    public float translateY(float x, float y) {
        float result = y;
        
        if (this == Percent) {
            result =
                    y * Gdx.graphics.getHeight() / 200;
        }
        
        return result;
    }
}
