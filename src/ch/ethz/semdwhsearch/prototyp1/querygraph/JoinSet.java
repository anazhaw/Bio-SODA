package ch.ethz.semdwhsearch.prototyp1.querygraph;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.dag.DagEdge;
import ch.ethz.rdf.dag.RdfDagNode;

/**
 * A join set.
 * 
 * @author Lukas Blunschi
 * 
 */
public class JoinSet {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	private final List<Join> joins;

	private boolean reduced;

	public JoinSet() {
		this(QueryGraph.URI_JOINSET_PREFIX + (++counter), false);
	}

	public JoinSet(String uri, boolean reduced) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		this.uri = uri;
		this.joins = new ArrayList<Join>();
		this.reduced = reduced;
	}

	// -------------------------------------------------------------------- uri

	public String getUri() {
		return uri;
	}

	// ------------------------------------------------------------------ joins

	public Join addJoin(Join join) {
		for (Join joinCur : joins) {
			if (joinCur.equals(join)) {
				return joinCur;
			}
		}
		// add
		this.joins.add(join);
		return join;
	}

	public List<Join> getJoins() {
		return joins;
	}

	// ----------------------------------------------------------- reduced flag

	public void setReduced(boolean reduced) {
		this.reduced = reduced;
	}

	public boolean isReduced() {
		return reduced;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {

		// uri
		RdfDagNode jsNode = dag.getByUniqueId(uri);
		if (jsNode == null) {
			jsNode = dag.addNode(uri);
		}

		// joins
		jsNode.addEdge(QueryGraph.EDGE_ISTYPE, uri, QueryGraph.URI_JOINSET_PREFIX + "type");
		for (Join join : joins) {
			join.toDag(jsNode);
			jsNode.addEdge(QueryGraph.EDGE_JOIN, uri, join.uri);
		}

		// reduced flag
		if (reduced) {
			jsNode.addLiteral(QueryGraph.EDGE_ISREDUCED, "jupp, this joinset is reduced.");
		}
	}

	public static JoinSet fromDag(RdfDagNode joinsetNode) {

		// uri
		String uri = joinsetNode.getUniqueId();

		// reduced flag
		String isReducedStr = joinsetNode.getLiteralValue(QueryGraph.EDGE_ISREDUCED);
		boolean isReduced = isReducedStr == null ? false : true;

		// joins
		JoinSet joinset = new JoinSet(uri, isReduced);
		for (DagEdge<RdfDagNode> edge : joinsetNode.getOutputs(QueryGraph.EDGE_JOIN)) {
			RdfDagNode joinNode = edge.getOtherEnd(joinsetNode);
			Join join = Join.fromDag(joinNode);
			joinset.addJoin(join);
		}
		return joinset;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public int hashCode() {
		int value = 0;
		for (Join join : joins) {
			value += join.hashCode();
		}
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JoinSet) {
			JoinSet joinset = (JoinSet) obj;
			return joinset.joins.size() == joins.size() && joinset.joins.containsAll(joins);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return uri;
	}

}
