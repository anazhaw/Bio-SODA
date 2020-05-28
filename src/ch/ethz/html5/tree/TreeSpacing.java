package ch.ethz.html5.tree;

/**
 * Spacing for our tree.
 * 
 * @author Lukas Blunschi
 * 
 */
public class TreeSpacing {

	public final int radius;

	public final int padding;

	public final int grid;

	public TreeSpacing(int radius) {
		this.radius = radius;

		// computed values
		this.padding = radius / 2;
		this.grid = (radius + padding) * 2;
	}

}
