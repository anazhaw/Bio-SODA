package ch.ethz.dag;

import java.util.Comparator;

/**
 * A DAG edge comparator which compares the edges by comparing the unique IDs of
 * the nodes on one side of the edge. The side is choosen to be the one which is
 * not the one where the given node appears.
 * 
 * @author Lukas Blunschi
 * 
 */
public class DagEdgeComparator implements Comparator<DagEdge<? extends DagNode<?>>> {

	private final DagNode<?> node;

	public DagEdgeComparator(DagNode<?> node) {
		this.node = node;
	}

	public int compare(DagEdge<? extends DagNode<?>> edge1, DagEdge<? extends DagNode<?>> edge2) {
		if (edge1.getInput() == node) {
			return edge1.getOutput().getUniqueId().compareTo(edge2.getOutput().getUniqueId());
		} else {
			return edge1.getInput().getUniqueId().compareTo(edge2.getInput().getUniqueId());
		}
	}

}
