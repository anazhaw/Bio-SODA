package ch.ethz.semdwhsearch.prototyp1.querygraph;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.dag.DagEdge;
import ch.ethz.rdf.dag.RdfDagNode;

/**
 * A table.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Table {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	public final String name;

	public final String srcLink;

	private final List<Column> columns;

	private final List<Relationship> relationships;

	public Table(String name, String srcLink) {
		this(QueryGraph.URI_TABLE_PREFIX + (++counter), name, srcLink);
	}

	private Table(String uri, String name, String srcLink) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		if (name == null) {
			throw new RuntimeException("name must not be null!");
		}
		if (srcLink == null) {
			throw new RuntimeException("src link must not be null!");
		}
		this.uri = uri;
		this.name = name;
		this.srcLink = srcLink;
		this.columns = new ArrayList<Column>();
		this.relationships = new ArrayList<Relationship>();
	}

	// ---------------------------------------------------------------- columns

	public void addColumn(Column column) {
		if (!columns.contains(column)) {
			columns.add(column);
		}
	}

	/**
	 * Get column.
	 * 
	 * @param name
	 *            name of column, e.g. 'id'.
	 * @return column or null if column not found.
	 */
	public Column getColumn(String name) {
		for (Column columnCur : columns) {
			if (columnCur.name.equals(name)) {
				return columnCur;
			}
		}
		return null;
	}

	/**
	 * Get all columns attached to this table.
	 * 
	 * @return list of columns (never null).
	 */
	public List<Column> getColumns() {
		return columns;
	}

	/**
	 * Get columns which do not act as a key (primary or foreign key).
	 * 
	 * @return list of not-key columns (never null).
	 */
	public List<Column> getNotKeyColumns() {

		// copy all columns
		List<Column> notKeyColumns = new ArrayList<Column>(columns);

		// remove columns which take part in a relationship
		for (Relationship rel : relationships) {
			for (Key key : rel.getJoinCondition().getKeys()) {
				notKeyColumns.remove(key.getColumn());
			}
		}

		return notKeyColumns;
	}

	/**
	 * Get foreign key columns linked to this table.
	 * 
	 * @return foreign key columns (never null).
	 */
	public List<Column> getFkColumns() {
		List<Column> fkColumns = new ArrayList<Column>();
		for (Relationship rel : relationships) {
			Column col = rel.getJoinCondition().getFk().getColumn();
			if (columns.contains(col)) {
				fkColumns.add(col);
			}
		}
		return fkColumns;
	}

	// ---------------------------------------------------------- relationships

	public void addRelationship(Relationship relationship) {
		if (!relationships.contains(relationship)) {
			relationships.add(relationship);
		}
	}

	public List<Relationship> getRelationships() {
		return relationships;
	}

	public Relationship getRelationship(Table table) {
		for (Relationship rel1 : relationships) {
			for (Relationship rel2 : table.relationships) {
				if (rel1.equals(rel2)) {
					return rel1;
				}
			}
		}
		return null;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode tableNode = dag.getByUniqueId(uri);
		if (tableNode == null) {
			tableNode = dag.addNode(uri);
		}
		tableNode.addEdge(QueryGraph.EDGE_SRCLINK, uri, srcLink);
		tableNode.addLiteral(QueryGraph.EDGE_TABLENAME, name);
		for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
			Column column = columns.get(colIndex);
			column.toDag(tableNode);
			tableNode.addEdge(QueryGraph.EDGE_COLUMN, uri, column.uri);
		}
		for (int relIndex = 0; relIndex < relationships.size(); relIndex++) {
			Relationship relationship = relationships.get(relIndex);
			relationship.toDag(tableNode);
			tableNode.addEdge(QueryGraph.EDGE_RELATIONSHIP, uri, relationship.uri);
		}
	}

	public static Table fromDag(RdfDagNode tableNode) {
		String uri = tableNode.getUniqueId();
		String name = tableNode.getLiteralValue(QueryGraph.EDGE_TABLENAME);
		RdfDagNode srcLinkNode = tableNode.getOutputs(QueryGraph.EDGE_SRCLINK).iterator().next().getOtherEnd(tableNode);
		String srcLink = srcLinkNode.getUniqueId();
		Table table = new Table(uri, name, srcLink);
		for (DagEdge<RdfDagNode> edge : tableNode.getOutputs(QueryGraph.EDGE_COLUMN)) {
			RdfDagNode colNode = edge.getOtherEnd(tableNode);
			Column column = Column.fromDag(colNode);
			table.addColumn(column);
		}
		for (DagEdge<RdfDagNode> edge : tableNode.getOutputs(QueryGraph.EDGE_RELATIONSHIP)) {
			RdfDagNode relNode = edge.getOtherEnd(tableNode);
			Relationship relationship = Relationship.fromDag(relNode);
			table.addRelationship(relationship);
		}
		return table;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj instanceof Table) {
			Table table = (Table) obj;
			return table.name.equals(name);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return uri + " name=" + name;
	}

}
