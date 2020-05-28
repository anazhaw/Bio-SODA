package ch.ethz.dag;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A generic DAG node.
 * 
 * @author Lukas Blunschi
 * 
 */
public class DagGenericNode extends DagAbstractNode<DagGenericNode> {

	// ----------------------------------------------------------- construction

	/**
	 * Creates a new DAG.
	 * <p>
	 * Use this only once per DAG.
	 * 
	 * @param uniqueId
	 *            of first node.
	 */
	public DagGenericNode(String uniqueId) {
		super(new TreeMap<String, DagGenericNode>(), uniqueId);
		addNode(this);
	}

	private DagGenericNode(SortedMap<String, DagGenericNode> idMap, String uniqueId) {
		super(idMap, uniqueId);
		addNode(this);
	}

	@Override
	protected final DagGenericNode getNewNode(SortedMap<String, DagGenericNode> idMap, String uniqueId) {
		return new DagGenericNode(idMap, uniqueId);
	}

	// ------------------------------------------------------------------- copy

	protected void copyNodeMembers(DagGenericNode src, DagGenericNode dst) {
		// no local members
	}

}
