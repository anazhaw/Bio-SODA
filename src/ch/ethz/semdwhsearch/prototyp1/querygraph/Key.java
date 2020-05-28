package ch.ethz.semdwhsearch.prototyp1.querygraph;

import ch.ethz.rdf.dag.RdfDagNode;

/**
 * A key.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Key {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	private final Column column;

	public Key(Column column) {
		this(QueryGraph.URI_KEY_PREFIX + (++counter), column);
	}

	private Key(String uri, Column column) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		this.uri = uri;
		this.column = column;
	}

	// ----------------------------------------------------------------- column

	public Column getColumn() {
		return column;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode keyNode = dag.getByUniqueId(uri);
		if (keyNode == null) {
			keyNode = dag.addNode(uri);
		}
		column.toDag(dag);
		keyNode.addEdge(QueryGraph.EDGE_KEY, uri, column.uri);
	}

	public static Key fromDag(RdfDagNode keyNode) {
		String uri = keyNode.getUniqueId();
		RdfDagNode colNode = keyNode.getOutputs(QueryGraph.EDGE_KEY).iterator().next().getOtherEnd(keyNode);
		Column column = Column.fromDag(colNode);
		Key key = new Key(uri, column);
		return key;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Key) {
			Key key = (Key) obj;
			return uri.equals(key.uri);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return uri;
	}

}
