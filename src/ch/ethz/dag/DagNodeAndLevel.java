package ch.ethz.dag;

/**
 * Struct to hold a DAG node and a level.
 * 
 * @author Lukas Blunschi
 * 
 * @param <T>
 *            type of DAG node.
 */
public class DagNodeAndLevel<T> {

	public final T node;

	public int level;

	public DagNodeAndLevel(T node, int level) {
		this.node = node;
		this.level = level;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public String toString() {
		return node + ":" + level;
	}

}
