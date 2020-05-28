package ch.ethz.semdwhsearch.prototyp1.metadata;

/**
 * A struct to hold a table name.
 * 
 * @author Lukas Blunschi
 * 
 */
public class TableName implements Comparable<TableName> {

	/**
	 * The name of the table
	 * <p>
	 * E.g. options
	 */
	public final String name;

	public final String srcLink;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            name of table, e.g. 'options'
	 * @param srcLink
	 *            uri of table node in meta data.
	 */
	public TableName(String name, String srcLink) {

		// ensure source link is present
		if (srcLink == null) {
			throw new RuntimeException("src link in table name must not be null.");
		}

		this.name = name;
		this.srcLink = srcLink;
	}

	// --------------------------------------------------- comparable interface

	public int compareTo(TableName o) {
		return name.compareTo(o.name);
	}

	// ------------------------------------------------------- object overrides

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TableName) {
			TableName tableName = (TableName) obj;
			return name.equals(tableName.name);
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
