package ch.ethz.semdwhsearch.prototyp1.data;

/**
 * A value and the column it comes from.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ValueAndColumn {

	public String value;

	public String columnName;

	public ValueAndColumn(String value, String columnName) {
		this.value = value;
		this.columnName = columnName;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public String toString() {
		return columnName + "=" + value;
	}

}
