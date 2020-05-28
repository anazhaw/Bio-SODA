package ch.ethz.dag;

/**
 * Interface for all edges in a DAG.
 * 
 * @author Lukas Blunschi
 * 
 * @param <T>
 *            concrete type of nodes.
 */
public interface DagEdge<T> {

	String getName();

	T getInput();

	T getOutput();

	T getOtherEnd(T node);

}
