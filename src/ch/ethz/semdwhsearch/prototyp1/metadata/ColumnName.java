package ch.ethz.semdwhsearch.prototyp1.metadata;

/**
 * A struct to hold a column name.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ColumnName implements Comparable<ColumnName> {

	/**
	 * The name of the column
	 * <p>
	 * E.g. options.shortName
	 */
	public final String name;

	private final String colNodeSrcLink;

	private final String tableNodeSrcLink;

	public ColumnName(String tableName, String columnName, String colNodeSrcLink, String tableNodeSrcLink) {

		// ensure source links are present
		if (colNodeSrcLink == null || tableNodeSrcLink == null) {
			throw new RuntimeException("src links in column name must not be null.");
		}

		// init members
		this.name = tableName + "." + columnName;
		this.colNodeSrcLink = colNodeSrcLink;
		this.tableNodeSrcLink = tableNodeSrcLink;
	}

	public ColumnName(String columnNameLong, String colNodeSrcLink, String tableNodeSrcLink) {

		// ensure source links are present
		if (colNodeSrcLink == null || tableNodeSrcLink == null) {
			throw new RuntimeException("null as src link passed to column name object (2).");
		}

		// init members
		this.name = columnNameLong;
		this.colNodeSrcLink = colNodeSrcLink;
		this.tableNodeSrcLink = tableNodeSrcLink;
	}

	// -------------------------------------------------------------- accessors

	public String getColumnNameShort() {
		int pos = name.indexOf(".");
		return name.substring(pos + 1);
	}

	public TableName getTableName() {
		int pos = name.indexOf(".");
		return new TableName(name.substring(0, pos), tableNodeSrcLink);
	}

	// -------------------------------------------------------------- src links

	public String getColNodeSrcLink() {
		return colNodeSrcLink;
	}

	public String getTableNodeSrcLink() {
		return tableNodeSrcLink;
	}

	// --------------------------------------------------- comparable interface

	public int compareTo(ColumnName o) {
		return name.compareTo(o.name);
	}

	// ------------------------------------------------------- object overrides

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ColumnName) {
			ColumnName columnName = (ColumnName) obj;
			return name.equals(columnName.name);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

}
