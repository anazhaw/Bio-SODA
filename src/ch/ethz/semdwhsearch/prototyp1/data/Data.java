package ch.ethz.semdwhsearch.prototyp1.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.lib.org.json.JSONArray;
import com.hp.hpl.jena.sparql.lib.org.json.JSONException;

import ch.ethz.semdwhsearch.prototyp1.classification.index.Index;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermOrigin;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.ethz.semdwhsearch.prototyp1.config.Config;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.tools.Escape;
import ch.ethz.semdwhsearch.prototyp1.tools.Tokenizer;
import ch.zhaw.biosoda.SPARQLUtilsRemote;

/**
 * Data Access.
 * <p>
 * TODO check that all references to this class check the connected flag, before
 * using this.
 * 
 * @author Lukas Blunschi, Ana Sima
 * 
 */
public class Data {

	private final static Logger logger = LoggerFactory.getLogger(Data.class);

	// ---------------------------------------------------------------- members

	private final boolean connected;

	private final DataSource ds;

	private final String dbvendor;

	private final String schema;

	private final String username;

	private final Config config;

	// ------------------------------------------------------------ constructor

	public Data(DataSource ds, String dbvendor, String schema, String username, Config config) {
		this.connected = ds != null;
		this.ds = ds;
		this.dbvendor = dbvendor;
		this.schema = schema;
		this.username = username;
		this.config = config;

		// print connection info
		if (connected) {
			Connection conn = null;
			try {
				conn = ds.getConnection();
				logConnectionInfo(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (Exception e) {
					}
				}
			}
		}
	}

	// ---------------------------------------------------------------- getters

	public boolean isConnected() {
		return connected;
	}

	public DataSource getDs() {
		return ds;
	}

	public String getDbvendor() {
		return dbvendor;
	}

	public String getSchema() {
		return schema;
	}

	public String getUsername() {
		return username;
	}

	public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	// ---------------------------------------------------------- functionality

	private void logConnectionInfo(Connection conn) throws SQLException {

		logger.info("Catalog: " + conn.getCatalog());
		// logger.info("Client info: " + conn.getClientInfo());

		// get database metadata
		DatabaseMetaData dmd = ds.getConnection().getMetaData();

		// display database info
		logger.info("Database Product Name: " + dmd.getDatabaseProductName());
		logger.info("Database Product Version: " + dmd.getDatabaseProductVersion());
		logger.info("Maximum Table Name length : " + dmd.getMaxTableNameLength());
		logger.info("User Name: " + dmd.getUserName());
		logger.info("URL: " + dmd.getURL());
		if (dmd.usesLocalFiles()) {
			logger.info("The Database uses Local Files");
		} else {
			logger.info("The Database does not use Local Files");
		}
		StringBuffer buf = null;
		ResultSet rsCatalogs = dmd.getCatalogs();
		buf = new StringBuffer();
		while (rsCatalogs.next()) {
			buf.append(rsCatalogs.getString(1)).append(" ");
		}
		logger.info("The following catalogs are available in the database: " + buf.toString());
		ResultSet rsSchemas = dmd.getSchemas();
		buf = new StringBuffer();
		while (rsSchemas.next()) {
			buf.append(rsSchemas.getString(1)).append(" ");
		}
		logger.info("The following schemas are available in the database: " + buf.toString());
	}

	/**
	 * Get table names.
	 * 
	 * @param useUserSchema
	 *            if true AND (schema not null and username not null), then use
	 *            the upper-case username as schema prefix for tables.
	 * @param hideIdxTables
	 *            true to remove index tables from result.
	 * @return table names.
	 */
	public List<String> getTableNames(boolean useUserSchema, boolean hideIdxTables) {
		logger.info("Getting table names...");

		// ensure connected
		if (!connected) {
			logger.info("Not connected to database.");
			return new ArrayList<String>();
		}

		// try
		Connection conn = null;
		List<String> tableNames = new ArrayList<String>();
		try {
			conn = ds.getConnection();

			// get database metadata
			DatabaseMetaData dmd = conn.getMetaData();

			// schema pattern
			// - oracle only works with schema names in upper case!
			String schemaPattern = null;
			if (schema != null && username != null) {
				if (useUserSchema) {
					schemaPattern = username.toUpperCase();
				} else {
					schemaPattern = schema.toUpperCase();
				}
				logger.info("Using schema pattern: " + schemaPattern);
			}

			// get table names
			String catalog = null;
			String tableNamePattern = "%";
			String[] types = null;
			ResultSet rs = dmd.getTables(catalog, schemaPattern, tableNamePattern, types);
			Map<String, List<String>> typeToTablesMap = new HashMap<String, List<String>>();
			while (rs.next()) {
				String tableName = rs.getString(3);
				String tableType = rs.getString(4);

				// create type to table names map
				List<String> tableNamesCur = typeToTablesMap.get(tableType);
				if (tableNamesCur == null) {
					tableNamesCur = new ArrayList<String>();
					typeToTablesMap.put(tableType, tableNamesCur);
				}
				tableNamesCur.add(tableName);
				if (tableType.equals("TABLE") && !tableName.contains("$")) {
					tableNames.add(tableName.toLowerCase());
				}
			}
			for (Map.Entry<String, List<String>> entry : typeToTablesMap.entrySet()) {
				String tableType = entry.getKey();
				List<String> tableNamesCur = entry.getValue();
				logger.info("Table type " + tableType + ": " + tableNamesCur);
			}

			// use inclusive and exclusive table name patterns
			// - include:
			List<String> tableNamesAdd = new ArrayList<String>();
			String inclStr = ".*";
			String[] inclPatterns = inclStr.split(",");
			for (String inclPattern : inclPatterns) {
				inclPattern = inclPattern.trim();
				if (inclPattern.length() > 0) {
					logger.info("Applying table name inclusive pattern: " + inclPattern);
					for (String tableName : tableNames) {
						if (tableName.matches(inclPattern)) {
							tableNamesAdd.add(tableName);
						}
					}
				}
			}
			logger.info("Table names include: " + tableNamesAdd);
			// - exclude:
			List<String> tableNamesDel = new ArrayList<String>();
			String exclStr = "";
			String[] exclPatterns = exclStr.split(",");
			for (String exclPattern : exclPatterns) {
				exclPattern = exclPattern.trim();
				if (exclPattern.length() > 0) {
					logger.info("Applying table name exclusive pattern: " + exclPattern);
					for (String tableName : tableNamesAdd) {
						if (tableName.matches(exclPattern)) {
							tableNamesDel.add(tableName);
						}
					}
				}
			}
			logger.info("Table names exclude: " + tableNamesDel);
			tableNamesAdd.removeAll(tableNamesDel);
			tableNames = tableNamesAdd;

		} catch (SQLException e) {
			e.printStackTrace();
			tableNames.clear();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
			}
		}

		// hide idx tables if requested
		if (hideIdxTables) {
			List<String> toRemove = new ArrayList<String>();
			for (String tableName : tableNames) {
				if (tableName.startsWith("biosoda_idx_")) {
					toRemove.add(tableName);
				}
			}
			for (String tableName : toRemove) {
				tableNames.remove(tableName);
			}
		}

		// sort and print
		Collections.sort(tableNames);
		logger.info(tableNames.size() + " table names: " + tableNames);
		return tableNames;
	}

	/**
	 * Safely execute given SQL update statement.
	 * 
	 * @param sql
	 *            SQL update statement, e.g. CREATE TABLE xyz ...
	 * @return true if successful, false otherwise.
	 */
	public boolean executeSqlUpdate(String sql) {
		boolean result = true;

		// try
		Connection conn = null;
		Statement stmt = null;
		try {

			// execute SQL
			conn = ds.getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Failure executing SQL Update: " + e.getMessage() + " original "+ sql);
			result = false;
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * Safely execute given SQL query statement.
	 * 
	 * @param sql
	 *            SQL query, e.g. SELECT * ...
	 * @return query result (never null).
	 */
	public SqlResult executeSqlQuery(String sql) {
		SqlResult result = new SqlResult(sql);

		// if not connected, return result with SQL statement only
		if (!connected) {
			return result;
		}

		// try
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {

			// execute SQL
			conn = ds.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			// fetch result metadata
			ResultSetMetaData rsMeta = rs.getMetaData();
			int numCols = rsMeta.getColumnCount();
			result.setNumCols(numCols);
			String[] columnNames = new String[numCols];
			for (int i = 0; i < numCols; i++) {
				String tableName = rsMeta.getTableName(i + 1);
				String columnName = rsMeta.getColumnName(i + 1);
				columnNames[i] = tableName + "." + columnName;
			}
			result.setColumnNames(columnNames);

			// create object list
			List<Object[]> objectTable = new ArrayList<Object[]>();
			while (rs.next()) {
				Object[] row = new Object[numCols];
				for (int i = 0; i < numCols; i++) {
					row[i] = rs.getObject(i + 1);
				}
				objectTable.add(row);
			}
			result.setObjectTable(objectTable);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Failure executing SQL Query: " + e.getMessage());
			result.setObjectTable(null);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
			}
		}
		return result;
	}

	public void appendSPARQLResultTable(StringBuffer html, String sparqlQuery, final int limit) {
		JSONArray ja = null;
		final Query query = QueryFactory.create(sparqlQuery);
		if(Constants.USE_REMOTE) {
			ja =  SPARQLUtilsRemote.execQueryToJson(sparqlQuery, Constants.REMOTE_REPO);
		}
		else {
			//TODO
		}

		html.append("<table class='list'>\n");
		html.append("<tr>");

		for (String selectedVar: query.getResultVars()) {
			html.append("<th>");
			html.append(Escape.safeXml("?"+selectedVar));
			html.append("</th>");
		}

		html.append("</tr>");

		for(int index = 0; index < ja.length(); index++){
			com.hp.hpl.jena.sparql.lib.org.json.JSONObject sparqlResult = null;
			try {
				sparqlResult = (com.hp.hpl.jena.sparql.lib.org.json.JSONObject) ja.get(index);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			html.append("<tr>");
			for (String selectedVar: query.getResultVars()) {
				html.append("<td>");
				String text = null;
				try {
					text = sparqlResult.get("?"+selectedVar).toString();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(text == null)
					continue;
				if(text.startsWith("http")) {
					text = "<a href=\"" + text + "\"> "+ Escape.safeXml(text) + "</a>";
				}
				else {
					text = Escape.safeXml(text);
				}
				html.append(text);
				html.append("</td>");
			}
			html.append("</tr>");
		}
		html.append("</table>");
	}
	public void appendResultTable(StringBuffer html, String sqlQuery, final int limit) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		// ensure we are connected
		if (!connected) {
			html.append("no connection to database.\n");
			return;
		}

		// try to run query
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);

			// result metadata
			ResultSetMetaData meta = rs.getMetaData();
			int numcols = meta.getColumnCount();
			List<String> colNames = new ArrayList<String>();
			for (int i = 1; i <= numcols; i++) {
				colNames.add(meta.getColumnName(i));
			}

			// create html
			html.append("<table class='list'>\n");

			// header row
			html.append("<tr>");
			for (String colName : colNames) {
				html.append("<td>").append(colName).append("</td>");
			}
			html.append("</tr>\n");

			// one row per result row
			int count = 0;
			while (count < limit && rs.next()) {
				count++;
				html.append("<tr>");
				for (int i = 1; i <= numcols; i++) {
					html.append("<td>").append(rs.getString(i)).append("</td>");
				}
				html.append("</tr>\n");
			}

			// count row
			html.append("<tr>");
			html.append("<td colspan='" + numcols + "'>");
			html.append(count);
			if (count == limit) {
				html.append(" (limit reached)");
			}
			html.append("");
			html.append("</td>");
			html.append("</tr>\n");

			html.append("</table>\n");

		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
			}
		}
	}

}
