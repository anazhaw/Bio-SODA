package ch.ethz.semdwhsearch.prototyp1.querygraph;

import ch.ethz.rdf.dag.RdfDagNode;

/**
 * A problem.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Problem {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	public final String message;

	public Problem(String message) {
		this(QueryGraph.URI_PROBLEM_PREFIX + (++counter), message);
	}

	public Problem(String uri, String message) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		if (message == null) {
			throw new RuntimeException("message must not be null!");
		}
		this.uri = uri;
		this.message = message;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode prNode = dag.getByUniqueId(uri);
		if (prNode == null) {
			prNode = dag.addNode(uri);
		}
		prNode.addLiteral(QueryGraph.EDGE_MESSAGE, message);
	}

	public static Problem fromDag(RdfDagNode prNode) {
		String uri = prNode.getUniqueId();
		String message = prNode.getLiteralValue(QueryGraph.EDGE_MESSAGE);
		return new Problem(uri, message);
	}

	// ------------------------------------------------------- object overrides

	public String toString() {
		return uri + " message=" + message;
	}

}
