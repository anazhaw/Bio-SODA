package ch.ethz.semdwhsearch.prototyp1.data;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * Code to setup the data source.
 * 
 * @author Lukas Blunschi
 * 
 */
public class DataSourceSetup {

	public static DataSource getDsExampleWorld1(String dbvendor) {
		return getDs(dbvendor, "semdwhsearch", "123", "semdwhsearch_prototyp1", "localhost", null, null);
	}

	public static DataSource getDsExampleWorld2(String dbvendor) {
		return getDs(dbvendor, "example_world2", "123", "example_world2", "localhost", null, null);
	}

	/**
	 * Create data source.
	 * 
	 * @param dbvendor
	 * @param username
	 * @param password
	 * @param dbname
	 * @param hostname
	 * @param port
	 * @param sid
	 * @return data source or null if unkown arguments.
	 */
	public static DataSource getDs(String dbvendor, String username, String password, String dbname, String hostname,
			String port, String sid) {

		// create driver class name and URL according to DBMS vendor
		String driverClassName = null;
		String url = null;
		if (dbvendor != null && dbvendor.equalsIgnoreCase("mysql")) {
			driverClassName = "com.mysql.jdbc.Driver";
			url = "jdbc:mysql://" + hostname + "/" + dbname;
		} else if (dbvendor != null && dbvendor.equalsIgnoreCase("derby")) {
			driverClassName = "org.apache.derby.jdbc.EmbeddedDriver";
			username = "";
			password = "";
			url = "jdbc:derby:derby/" + dbname + ";create=true";
		} else if (dbvendor != null && dbvendor.equalsIgnoreCase("oracle")) {
			driverClassName = "oracle.jdbc.OracleDriver";
			url = "jdbc:oracle:thin:@" + hostname + ":" + port + ":" + sid;
		}

		// create datasource if driver class identified
		if (driverClassName == null) {
			return null;
		} else {
			BasicDataSource ds = new BasicDataSource();
			ds.setDriverClassName(driverClassName);
			ds.setUsername(username);
			ds.setPassword(password);
			ds.setUrl(url);
		    ds.setValidationQuery("SELECT * from biosoda_idx_metadata_values limit 1");
			ds.setTestOnBorrow(true);
			ds.setRemoveAbandoned(true);	
			return ds;
		}
	}

}
