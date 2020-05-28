package ch.ethz.semdwhsearch.prototyp1.querygraph;

import ch.ethz.dag.DagEdge;
import ch.ethz.rdf.dag.RdfDagNode;

/**
 * A join.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Join {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	private final Table table1;

	private final Table table2;

	private final JoinCondition joinCondition;

	public Join(Table table1, Table table2, JoinCondition joinCondition) {
		this(QueryGraph.URI_JOIN_PREFIX + (++counter), table1, table2, joinCondition);
	}

	public Join(String uri, Table table1, Table table2, JoinCondition joinCondition) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		this.uri = uri;
		this.table1 = table1;
		this.table2 = table2;
		this.joinCondition = joinCondition;
	}

	// ---------------------------------------------------------------- members

	public Table getTable1() {
		return table1;
	}

	public Table getTable2() {
		return table2;
	}

	public JoinCondition getJoinCondition() {
		return joinCondition;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode joinNode = dag.getByUniqueId(uri);
		if (joinNode == null) {
			joinNode = dag.addNode(uri);
		}
		table1.toDag(joinNode);
		table2.toDag(joinNode);
		joinCondition.toDag(joinNode);
		joinNode.addEdge(QueryGraph.EDGE_TABLE1, uri, table1.uri);
		joinNode.addEdge(QueryGraph.EDGE_TABLE2, uri, table2.uri);
		joinNode.addEdge(QueryGraph.EDGE_JOINCONDITION, uri, joinCondition.uri);
	}

	public static Join fromDag(RdfDagNode joinNode) {
		String uri = joinNode.getUniqueId();
		DagEdge<RdfDagNode> edge = null;

		// table 1
		edge = joinNode.getOutputs(QueryGraph.EDGE_TABLE1).iterator().next();
		RdfDagNode table1Node = edge.getOtherEnd(joinNode);
		Table table1 = Table.fromDag(table1Node);

		// table 2
		edge = joinNode.getOutputs(QueryGraph.EDGE_TABLE2).iterator().next();
		RdfDagNode table2Node = edge.getOtherEnd(joinNode);
		Table table2 = Table.fromDag(table2Node);

		// join condition
		edge = joinNode.getOutputs(QueryGraph.EDGE_JOINCONDITION).iterator().next();
		RdfDagNode jcNode = edge.getOtherEnd(joinNode);
		JoinCondition joinCondition = JoinCondition.fromDag(jcNode);

		Join join = new Join(uri, table1, table2, joinCondition);
		return join;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public int hashCode() {
		return table1.hashCode() + table2.hashCode() + joinCondition.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Join) {
			Join join = (Join) obj;
			boolean matchingT1 = join.table1.equals(table1);
			boolean matchingT2 = join.table2.equals(table2);
			boolean matchingJC = join.joinCondition.equals(joinCondition);
			return matchingT1 && matchingT2 && matchingJC;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return uri + " t1=" + table1 + ", t2=" + table2 + ", jc=" + joinCondition;
	}

}
