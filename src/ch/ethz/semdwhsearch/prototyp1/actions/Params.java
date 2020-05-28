package ch.ethz.semdwhsearch.prototyp1.actions;

/**
 * Parameters which are used between pages.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface Params {

	/**
	 * Query (list of words) [string]
	 */
	public static final String Q = "q";

	/**
	 * Query graphs (list of query graphs) [n-triple string]
	 */
	public static final String QGRAPHS = "qgraphs";

	public static final String DBVENDOR = "dbvendor";

	public static final String SCHEMA = "schema";

	public static final String USERNAME = "username";

	public static final String PASSWORD = "password";

	public static final String DBNAME = "dbname";

	public static final String HOSTNAME = "hostname";

	public static final String PORT = "port";

	public static final String SID = "sid";

	public static final String CONFIGDIR = "configDir";
	
	public static final String DATADIR = "dataDir";

	public static final String INVERTEDINDEX = "invertedIndex";
	
	public static final String APPENDTOINDEX = "appendToIndex";

}
