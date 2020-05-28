package ch.ethz.semdwhsearch.prototyp1.querygraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.dag.DagEdge;
import ch.ethz.rdf.dag.RdfDagNode;

/**
 * A value.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Value implements HasTables, HasScore {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	public final String key;

	public final String value;

	public final String srcLink;

	private double score;

	private final List<Table> tables;

	public Value(String key, String value, String srcLink) {
		this(QueryGraph.URI_VALUE_PREFIX + (++counter), key, value, srcLink);
	}

	private Value(String uri, String key, String value, String srcLink) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		if (key == null) {
			throw new RuntimeException("key must not be null!");
		}
		if (value == null) {
			throw new RuntimeException("value must not be null!");
		}
		if (srcLink == null) {
			throw new RuntimeException("srcLink must not be null!");
		}
		this.uri = uri;
		this.key = key;
		this.value = value;
		this.srcLink = srcLink;
		this.score = -1.0;
		this.tables = new ArrayList<Table>();
	}

	// -------------------------------------------------------------------- uri

	public String getUri() {
		return uri;
	}

	// ------------------------------------------------------------------ score

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	// ------------------------------------------------------------------ table

	public Table addTable(Table table) {
		for (Table tableCur : tables) {
			if (tableCur.equals(table)) {
				return tableCur;
			}
		}
		// add
		this.tables.add(table);
		return table;
	}

	public void replaceTable(Table tableNew) {
		for (int i = 0; i < tables.size(); i++) {
			if (tables.get(i).equals(tableNew)) {
				tables.set(i, tableNew);
			}
		}
	}

	public Table getTable(String name) {
		for (Table tableCur : tables) {
			if (tableCur.name.equals(name)) {
				return tableCur;
			}
		}
		return null;
	}

	public List<Table> getTables() {
		return tables;
	}

	// ---------------------------------------------------------- relationships

	public Set<Relationship> getRelationships() {
		Set<Relationship> result = new HashSet<Relationship>();
		for (Table tableCur : tables) {
			result.addAll(tableCur.getRelationships());
		}
		return result;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode valNode = dag.getByUniqueId(uri);
		if (valNode == null) {
			valNode = dag.addNode(uri);
		}
		valNode.addEdge(QueryGraph.EDGE_ISTYPE, uri, QueryGraph.URI_VALUE_PREFIX + "type");
		valNode.addEdge(QueryGraph.EDGE_SRCLINK, uri, srcLink);
		valNode.addLiteral(QueryGraph.EDGE_KEY, key);
		valNode.addLiteral(QueryGraph.EDGE_VALUE, value);
		if (score >= 0.0) {
			valNode.addLiteral(QueryGraph.EDGE_HASRANKING, String.valueOf(score));
		}
		for (Table table : tables) {
			table.toDag(valNode);
			valNode.addEdge(QueryGraph.EDGE_TABLE, uri, table.uri);
		}
	}

	public static Value fromDag(RdfDagNode valNode) {
		String uri = valNode.getUniqueId();
		String key = valNode.getLiteralValue(QueryGraph.EDGE_KEY);
		String value = valNode.getLiteralValue(QueryGraph.EDGE_VALUE);
		RdfDagNode srcLinkNode = valNode.getOutputs(QueryGraph.EDGE_SRCLINK).iterator().next().getOtherEnd(valNode);
		String srcLink = srcLinkNode.getUniqueId();
		Value val = new Value(uri, key, value, srcLink);
		String scoreText = valNode.getLiteralValue(QueryGraph.EDGE_HASRANKING);
		if (scoreText != null) {
			val.setScore(Double.parseDouble(scoreText));
		}
		for (DagEdge<RdfDagNode> edge : valNode.getOutputs(QueryGraph.EDGE_TABLE)) {
			RdfDagNode tableNode = edge.getOtherEnd(valNode);
			Table table = Table.fromDag(tableNode);
			val.addTable(table);
		}
		return val;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public int hashCode() {
		return (value + srcLink).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Value) {
			Value val = (Value) obj;
			return val.key.equals(key) && val.value.equals(value) && val.srcLink.equals(srcLink);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return uri + " value=" + value + ", srcLink=" + srcLink;
	}

}
