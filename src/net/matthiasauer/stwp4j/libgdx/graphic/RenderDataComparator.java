package net.matthiasauer.stwp4j.libgdx.graphic;

import java.util.Comparator;

/**
 * Order according primarily according to the renderOrder, if a draw - then 
 * using renderProjected
 */
final class RenderDataComparator implements Comparator<RenderData> {
	@Override
	public int compare(final RenderData o1, final RenderData o2) {
		final int result = Integer.compare(o1.getRenderOrder(), o2.getRenderOrder());
		
		if (result != 0) {
			// if comparing the order was enough
			return result;
		}
		
		// otherwise they have the same order !
		return Boolean.compare(o1.isRenderProjected(), o2.isRenderProjected());
	}
}
