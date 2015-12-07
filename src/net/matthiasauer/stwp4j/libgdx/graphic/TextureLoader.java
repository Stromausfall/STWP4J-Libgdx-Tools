package net.matthiasauer.stwp4j.libgdx.graphic;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

final class TextureLoader {
    private final Map<String, AtlasRegion> archive;
    private final List<TextureAtlas> textureAtlases;
    
	private List<TextureAtlas> create(List<String> atlasFilePaths) {
		List<TextureAtlas> result = new LinkedList<TextureAtlas>();
		
		for (String atlasFilePath : atlasFilePaths) {
			FileHandle atlasFile = Gdx.files.internal(atlasFilePath);
			TextureAtlas textureAtlas = new TextureAtlas(atlasFile);
			result.add(textureAtlas);
		}
		
		return result;
	}
	
	TextureLoader(List<String> atlasFilePaths) {
		this.archive = new HashMap<String, AtlasRegion>();
		this.textureAtlases =
				Collections.unmodifiableList(create(atlasFilePaths));
	}
	
	/**
	 * returns the AtlasRegion with the given name,
	 * Note : it is expected that the name is unique across ALL used TextureAtlases
	 * @param name
	 * @return
	 */
	public AtlasRegion getTexture(String name) {
		AtlasRegion texture = null;
		
		if (name == null) {
			throw new NullPointerException("name mustn't be null !");
		}
		
		// if there is no entry - load it and save it
		if (!archive.containsKey(name)) {
			for (TextureAtlas textureAtlas : this.textureAtlases) {
				texture = textureAtlas.findRegion(name);
				
				if (texture != null) {
					archive.put(name, texture);
					break;
				}
			}
		}
		
		texture = archive.get(name);
		
		if (texture == null) {
			throw new NullPointerException(
					"found no region '" + name + "' in the TextureAtlases");
		}
		
		return texture;
	}
}
