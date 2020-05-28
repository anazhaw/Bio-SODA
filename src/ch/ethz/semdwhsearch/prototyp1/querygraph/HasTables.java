package ch.ethz.semdwhsearch.prototyp1.querygraph;

import java.util.List;

/**
 * An interface which is implemented by all objects which may have tables.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface HasTables extends HasURI {

	/**
	 * Add given table to this object. If this table already exists, then the
	 * existing table object is returned.
	 * 
	 * @param table
	 * @return existing table if table already existed or given table.
	 */
	Table addTable(Table table);

	/**
	 * Get table.
	 * 
	 * @param name
	 *            name of table, e.g. 'options'.
	 * @return table or null if table not found.
	 */
	Table getTable(String name);

	/**
	 * Get all tables attached to this object.
	 * 
	 * @return list of tables (never null).
	 */
	List<Table> getTables();

}
