package ch.ethz.semdwhsearch.prototyp1.querygraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.ethz.dag.DagEdge;
import ch.ethz.rdf.dag.RdfDagNode;

/**
 * A column.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Column {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	public final String name;

	public final String srcLink;

	/**
	 * A filter is valid, as long as its filter values can be fulfilled
	 */
	public boolean validFilter;

	/**
	 * Filter values
	 * <p>
	 * No filter values mean, that the column should be selected, but no filter
	 * values applied.
	 * <p>
	 * One or more filter values mean, that the column should be selected AND
	 * the filter values have to be fulfilled.
	 */
	private final List<String> filterValues;

	private boolean isPk;

	public Column(String name, String srcLink) {
		this(QueryGraph.URI_COLUMN_PREFIX + (++counter), name, srcLink);
	}

	private Column(String uri, String name, String srcLink) {
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
		this.validFilter = true;
		this.filterValues = new ArrayList<String>();
		this.isPk = false;
	}

	// ---------------------------------------------------------- filter values

	public List<String> getFilterValues() {
		return filterValues;
	}

	public void addFilterValue(String filterValue) {
		filterValues.add(filterValue);
	}

	/**
	 * 
	 * @param filterValues
	 *            given filter values to intersect with stored filter values.
	 *            not null and not empty.
	 */
	public void intersectFilterValues(List<String> filterValues) {

		// ignore invalid filters
		if (!validFilter) {
			return;
		}

		// valid filters
		// legend:
		// - = empty

		// switch on empty
		if (this.filterValues.isEmpty()) {
			// - && * = -
			// - && *,X,...,Y = -
			// - && X,...,Y = X,...,Y
			if (!filterValues.contains("*")) {
				this.filterValues.addAll(filterValues);
			}
		} else {

			// TODO this should be really well described and tested.
			// TODO what do we do here?

			if (!filterValues.contains("*")) {

				// X,Y && Y = Y
				// X && Y = invalid
				this.filterValues.retainAll(filterValues);
				if (this.filterValues.isEmpty()) {
					validFilter = false;
				}
			}
		}
	}

	public void removeStarFilterValue() {
		// TODO there are no stars left here!
		filterValues.remove("*");
	}

	// ------------------------------------------------------------ primary key

	public void setPk(boolean isPk) {
		this.isPk = isPk;
	}

	public boolean isPk() {
		return isPk;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode colNode = dag.getByUniqueId(uri);
		if (colNode == null) {
			colNode = dag.addNode(uri);
		}
		colNode.addEdge(QueryGraph.EDGE_SRCLINK, uri, srcLink);
		colNode.addLiteral(QueryGraph.EDGE_COLUMNNAME, name);
		for (String filterValue : filterValues) {
			colNode.addLiteral(QueryGraph.EDGE_FILTERVALUE, filterValue);
		}
		if (isPk) {
			colNode.addEdge(QueryGraph.EDGE_ISPK, uri, QueryGraph.URI_PK_PREFIX + "type");
		}
	}

	public static Column fromDag(RdfDagNode colNode) {
		String uri = colNode.getUniqueId();
		String name = colNode.getLiteralValue(QueryGraph.EDGE_COLUMNNAME);
		RdfDagNode srcLinkNode = colNode.getOutputs(QueryGraph.EDGE_SRCLINK).iterator().next().getOtherEnd(colNode);
		String srcLink = srcLinkNode.getUniqueId();
		List<String> filterValues = colNode.getLiteralValues(QueryGraph.EDGE_FILTERVALUE);
		boolean isPk = false;
		Collection<DagEdge<RdfDagNode>> coll = colNode.getOutputs(QueryGraph.EDGE_ISPK);
		if (coll.size() > 0) {
			isPk = true;
		}
		Column column = new Column(uri, name, srcLink);
		column.setPk(isPk);
		for (String filterValue : filterValues) {
			column.addFilterValue(filterValue);
		}
		return column;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Column) {
			Column column = (Column) obj;
			return uri.equals(column.uri);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return uri + " name=" + name;
	}

}
