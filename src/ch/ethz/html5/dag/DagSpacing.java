package ch.ethz.html5.dag;

/**
 * Spacing for our DAG.
 * 
 * @author Lukas Blunschi
 * 
 */
public class DagSpacing {

	public final int radius;

	public final int padding;

	public final int gridX;

	public final int gridY;

	public final int textShift;

	public DagSpacing(int radius) {
		this.radius = radius;

		// computed values
		this.padding = radius / 2;
		this.gridX = (radius + padding) * 2;
		this.gridY = (radius + 2) * 2;
		this.textShift = radius * 3 / 4 + 1;
	}

}
