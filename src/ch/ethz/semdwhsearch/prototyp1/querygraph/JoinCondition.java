package ch.ethz.semdwhsearch.prototyp1.querygraph;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.dag.DagEdge;
import ch.ethz.rdf.dag.RdfDagNode;

/**
 * A join condition.
 * 
 * @author Lukas Blunschi
 * 
 */
public class JoinCondition {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	private final Key pk;

	private final Key fk;

	public JoinCondition(Key pk, Key fk) {
		this(QueryGraph.URI_JOINCONDITION_PREFIX + (++counter), pk, fk);
	}

	private JoinCondition(String uri, Key pk, Key fk) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		this.uri = uri;
		this.pk = pk;
		this.fk = fk;
	}

	// ------------------------------------------------------------------- keys

	public Key getPk() {
		return pk;
	}

	public Key getFk() {
		return fk;
	}

	public List<Key> getKeys() {
		List<Key> keys = new ArrayList<Key>();
		keys.add(pk);
		keys.add(fk);
		return keys;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode jcNode = dag.getByUniqueId(uri);
		if (jcNode == null) {
			jcNode = dag.addNode(uri);
		}
		pk.toDag(jcNode);
		fk.toDag(jcNode);
		jcNode.addEdge(QueryGraph.EDGE_KEY, uri, pk.uri);
		jcNode.addEdge(QueryGraph.EDGE_KEY, uri, fk.uri);
	}

	public static JoinCondition fromDag(RdfDagNode jcNode) {
		String uri = jcNode.getUniqueId();
		Key pk = null;
		Key fk = null;
		for (DagEdge<RdfDagNode> edge : jcNode.getOutputs(QueryGraph.EDGE_KEY)) {
			RdfDagNode keyNode = edge.getOtherEnd(jcNode);
			Key tmp = Key.fromDag(keyNode);
			if (tmp.getColumn().isPk()) {

				// if we have two inheritances on top of each other,
				// one column might be used for PK and FK
				if (pk == null) {
					pk = tmp;
				} else {
					fk = tmp;
				}
			} else {
				fk = tmp;
			}
		}
		if (pk == null || fk == null) {
			throw new RuntimeException("pk and/or fk not set!");
		}
		JoinCondition jc = new JoinCondition(uri, pk, fk);
		return jc;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JoinCondition) {
			JoinCondition jc = (JoinCondition) obj;
			return uri.equals(jc.uri);
		} else {
			return false;
		}
	}

	public String toString() {
		return uri;
	}

}
