package net.matthiasauer.stwp4j.libgdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

public class InputTools {
    public static void addInputProcessor(InputProcessor processor) {
        InputProcessor original = Gdx.input.getInputProcessor();
        
        if (original == null) {
            // no other input processor registered !
            Gdx.input.setInputProcessor(processor);
        } else {
            // there was a input processor already registerd !
            // use a multiplexer to use the existing and the new one !
            InputMultiplexer inputMultiplexer = new InputMultiplexer();
            
            inputMultiplexer.addProcessor(original);
            inputMultiplexer.addProcessor(processor);
            
            Gdx.input.setInputProcessor(inputMultiplexer);
        }
    }
}
