package ch.ethz.dag;

import java.util.SortedSet;

/**
 * Interface for all nodes in a DAG.
 * 
 * @author Lukas Blunschi
 * 
 * @see http://en.wikipedia.org/wiki/Directed_acyclic_graph
 * 
 * @param <T>
 *            concrete type.
 */
public interface DagNode<T> {

	/**
	 * 
	 * @return id of this node which is unique in this DAG.
	 */
	String getUniqueId();

	/**
	 * 
	 * @return list of input DAG nodes, maybe empty, but never null.
	 */
	SortedSet<DagEdge<T>> getInputs();

	/**
	 * 
	 * @return list of output DAG nodes, maybe empty, but never null.
	 */
	SortedSet<DagEdge<T>> getOutputs();

}
