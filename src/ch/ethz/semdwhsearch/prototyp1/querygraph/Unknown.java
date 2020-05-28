package ch.ethz.semdwhsearch.prototyp1.querygraph;

import ch.ethz.rdf.dag.RdfDagNode;

/**
 * An unknown.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Unknown {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	public final String value;

	public Unknown(String value) {
		this(QueryGraph.URI_UNKNOWN_PREFIX + (++counter), value);
	}

	private Unknown(String uri, String value) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		if (value == null) {
			throw new RuntimeException("value must not be null!");
		}
		this.uri = uri;
		this.value = value;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode ukNode = dag.getByUniqueId(uri);
		if (ukNode == null) {
			ukNode = dag.addNode(uri);
		}
		ukNode.addEdge(QueryGraph.EDGE_ISTYPE, uri, QueryGraph.URI_UNKNOWN_PREFIX + "type");
		ukNode.addLiteral(QueryGraph.EDGE_VALUE, value);
	}

	public static Unknown fromDag(RdfDagNode ukNode) {
		String uri = ukNode.getUniqueId();
		String value = ukNode.getLiteralValue(QueryGraph.EDGE_VALUE);
		return new Unknown(uri, value);
	}

	// ------------------------------------------------------- object overrides

	public String toString() {
		return uri + " value=" + value;
	}

}
