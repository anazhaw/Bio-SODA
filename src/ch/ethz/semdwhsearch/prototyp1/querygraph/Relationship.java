package ch.ethz.semdwhsearch.prototyp1.querygraph;

import ch.ethz.rdf.dag.RdfDagNode;

/**
 * A relationship.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Relationship {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	private final JoinCondition joinCondition;

	public Relationship(JoinCondition joinCondition) {
		this.uri = QueryGraph.URI_RELATIONSHIP_PREFIX + (++counter);
		this.joinCondition = joinCondition;
	}

	private Relationship(String uri, JoinCondition joinCondition) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		this.uri = uri;
		this.joinCondition = joinCondition;
	}

	// --------------------------------------------------------- join condition

	public JoinCondition getJoinCondition() {
		return joinCondition;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode relNode = dag.getByUniqueId(uri);
		if (relNode == null) {
			relNode = dag.addNode(uri);
		}
		joinCondition.toDag(dag);
		relNode.addEdge(QueryGraph.EDGE_JOINCONDITION, uri, joinCondition.uri);
	}

	public static Relationship fromDag(RdfDagNode relNode) {
		String uri = relNode.getUniqueId();
		RdfDagNode jcNode = relNode.getOutputs(QueryGraph.EDGE_JOINCONDITION).iterator().next().getOtherEnd(relNode);
		JoinCondition joinCondition = JoinCondition.fromDag(jcNode);
		Relationship relationship = new Relationship(uri, joinCondition);
		return relationship;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Relationship) {
			Relationship rel = (Relationship) obj;
			return uri.equals(rel.uri);
		} else {
			return false;
		}
	}

	public String toString() {
		return uri;
	}

}
