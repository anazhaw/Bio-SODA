package ch.ethz.semdwhsearch.prototyp1.data;

import java.util.List;

/**
 * A SQL query result.
 * 
 * @author Lukas Blunschi
 * 
 */
public class SqlResult {

	/**
	 * SQL statement which was executed to get this query result.
	 */
	private final String sql;

	/**
	 * number of columns in result
	 * <p>
	 * -1 means not computed yet
	 */
	private int numCols;

	/**
	 * column names of this result
	 * <p>
	 * null means not computed yet
	 */
	private String[] columnNames;

	/**
	 * object table of this result
	 * <p>
	 * null means not computed yet
	 */
	private List<Object[]> objectTable;

	/**
	 * number of rows in result
	 * <p>
	 * -1 means not computed yet
	 */
	private int numRows;

	/**
	 * Precision of query result
	 * <p>
	 * &lt; 0 if not computed yet
	 */
	private double precision;

	/**
	 * Recall of query result
	 * <p>
	 * &lt; 0 if not computed yet
	 */
	private double recall;

	// ----------------------------------------------------------- construction

	public SqlResult(String sql) {
		this.sql = sql;
		this.numCols = -1;
		this.columnNames = null;
		this.objectTable = null;
		this.numRows = -1;
		this.precision = -1.0;
		this.recall = -1.0;
	}

	// --------------------------------------------------------------- clean-up

	public void releaseObjectTableData() {
		this.objectTable = null;
	}

	// ---------------------------------------------------- getters and setters

	public String getSql() {
		return sql;
	}

	public int getNumCols() {
		return numCols;
	}

	void setNumCols(int numCols) {
		this.numCols = numCols;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public List<Object[]> getObjectTable() {
		return objectTable;
	}

	void setObjectTable(List<Object[]> objectTable) {
		this.objectTable = objectTable;
		if (objectTable == null) {
			this.numRows = -1;
		} else {
			this.numRows = objectTable.size();
		}
	}

	public int getNumRows() {
		return numRows;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	// ----------------------------------------------------- additional methods

	public boolean isValid() {
		return numRows >= 0;
	}

}
