package ch.ethz.dag;

import java.util.Comparator;

/**
 * A comparator which compares DAG nodes by unique id.
 * 
 * @author Lukas Blunschi
 * 
 */
public class DagNodeComparator implements Comparator<DagNode<?>> {

	public int compare(DagNode<?> o1, DagNode<?> o2) {
		return o1.getUniqueId().compareTo(o2.getUniqueId());
	}

}
