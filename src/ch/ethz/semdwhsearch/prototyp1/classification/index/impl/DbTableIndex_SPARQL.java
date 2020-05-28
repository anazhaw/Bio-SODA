package ch.ethz.semdwhsearch.prototyp1.classification.index.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.classification.index.Index;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermOrigin;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.data.Data;
import ch.ethz.semdwhsearch.prototyp1.data.SqlResult;
import ch.ethz.semdwhsearch.prototyp1.tools.Escape;
import ch.zhaw.biosoda.SPARQLUtilsRemote;
import ch.zhaw.nlp.TokenList;

/**
 * The index for RDF instances
 * 
 * @author Ana Sima, Lukas Blunschi
 * 
 */
public class DbTableIndex_SPARQL implements Index {

	private static final Logger logger = LoggerFactory.getLogger(DbTableIndex_SPARQL.class);

	private final String name;

	private final String idxTableNameValues;

	private final String idxTableNameOrigins;

	private SQLTableStructure valuesTable;

	private final Data data;

	/*
	 * the origins table is held in memory the first time we insert any term.
	 */
	private Map<String, Integer> cachedOrigins;

	private int maxValueId;

	private int maxOriginId;

	private Connection fastConn;

	private PreparedStatement insertOrigin;

	private PreparedStatement insertValue;

	// ----------------------------------------------------------- construction

	public DbTableIndex_SPARQL(String name, Data data, boolean reloadIdx, boolean appendIdx) {
		this.name = name;
		this.idxTableNameValues = "biosoda_idx_" + name + "_values";
		this.idxTableNameOrigins = "biosoda_idx_" + name + "_origins";
		this.data = data;
		this.cachedOrigins = null;
		this.maxValueId = -1;
		this.maxOriginId = -1;
		if(appendIdx) {
			assertMaxOriginId();
			assertMaxValueId();
		}
		if (this.data.isConnected()) {
			logger.info("Using DbTableIndex for " + name);
		} else {
			logger.error("DbTableIndex needs a database connection to work.");
		}
		init(reloadIdx, appendIdx);
	}

	private void init(boolean reloadIdx, boolean appendIdx) {

		// vars
		String[] idxTableNames = new String[] { idxTableNameValues, idxTableNameOrigins };

		// get existing table names
		Set<String> tableNames = new HashSet<String>(data.getTableNames(true, false));

		// drop existing index tables if reload requested
		if ((appendIdx == false) && reloadIdx) {
			for (String idxTableName : idxTableNames) {
				if (tableNames.contains(idxTableName)) {
					String sql = "DROP TABLE " + idxTableName;
					if (data.executeSqlUpdate(sql)) {
						tableNames.remove(idxTableName);
						logger.info("Index table " + idxTableName + " successfully dropped.");
					} else {
						logger.warn("Dropping index table " + idxTableName + " failed.");
					}
				}
			}
		}

		// create non-existing tables
		for (String idxTableName : idxTableNames) {
			if (!tableNames.contains(idxTableName)) {
				String sql = idxTableName.endsWith("values") ? getSqlCreateT1() : getSqlCreateT2();
				if (data.executeSqlUpdate(sql)) {
					logger.info("Index table " + idxTableName + " successfully (re)created.");

					if (idxTableName.endsWith("values")) {
						maxValueId = 0;
						//CREATE INDEX
						data.executeSqlUpdate("CREATE INDEX idx_lookup on "+ idxTableName + "(lookupKey);");
					} else {
						maxOriginId = 0;
					}
				} else {
					logger.warn("Index table " + idxTableName + " could not be created.");
				}
			}
		}
	}

	private String getSqlCreateT1() {
		// TODO add index on key column
		String intType = data.getDbvendor().equalsIgnoreCase("oracle") ? "number(4)" : "int";

		ArrayList<SQLFieldStructure> fields = new ArrayList<SQLFieldStructure>();
		fields.add(new SQLFieldStructure("id", "integer", "NOT NULL"));
		fields.add(new SQLFieldStructure("lookupKey", "varchar("+ Constants.MAX_FIELD_LENGTH + ")", ""));
		fields.add(new SQLFieldStructure("value", "varchar("+ Constants.MAX_FIELD_LENGTH + ")", ""));
		fields.add(new SQLFieldStructure("type", intType, "NOT NULL"));
		fields.add(new SQLFieldStructure("origin", intType, "NOT NULL"));
		fields.add(new SQLFieldStructure("originNameId", "integer", "NOT NULL"));

		valuesTable = new SQLTableStructure(idxTableNameValues, fields, "id", " UNIQUE (id)");
		
		return valuesTable.toString();
	}

	private String getSqlCreateT2() {
		StringBuffer sql = new StringBuffer();
		sql.append("create table " + idxTableNameOrigins + " (");
		sql.append("  id          integer NOT NULL,");
		sql.append("  originName  varchar(" + Constants.MAX_FIELD_LENGTH +"),");
		sql.append("  originClass  varchar(" + Constants.MAX_FIELD_LENGTH +"),");
		sql.append("  originProp  varchar(" + Constants.MAX_FIELD_LENGTH +"),");
		sql.append(" pageRank float NOT NULL, ");
		sql.append("  PRIMARY KEY (id)");
		sql.append(")");
		return sql.toString();
	}

	// -------------------------------------------------------- index interface

	public List<Term> lookup(TokenList tokens) {
		List<Term> result = new ArrayList<Term>();
		result.addAll(executeAndMaterialize(tokens, getLookupSql(tokens.getTokenizedText())));
		result.addAll(executeAndMaterialize(tokens, getLookupSql(tokens.getLemmatizedText())));
		return result;
	}

	public List<Term> lookupPrefix(TokenList tokens) {
		List<Term> result = new ArrayList<Term>();
		result.addAll(executeAndMaterialize(tokens, getLookupPrefixSql(tokens.getTokenizedText())));
		result.addAll(executeAndMaterialize(tokens, getLookupPrefixSql(tokens.getLemmatizedText())));
		return result;
	}

	public List<Term> lookupSubstring(TokenList tokens) {
		List<Term> result = new ArrayList<Term>();
		result.addAll(executeAndMaterialize(tokens, getLookupTokensSql(tokens.getTokenizedText().replace("%", "\\%"))));
		result.addAll(executeAndMaterialize(tokens, getLookupTokensSql(tokens.getLemmatizedText().replace("%", "\\%"))));
		if(tokens.getLemmatizedText().contains("-"))
			result.addAll(executeAndMaterialize(tokens, getLookupTokensSql(tokens.getLemmatizedText().replace("%", "\\%").replace("-", " "))));
		return result;
	}

	private String getSqlPrefix() {
		String sql = "";
		sql += "SELECT *";
		sql += "  FROM " + idxTableNameValues + ", " + idxTableNameOrigins;
		sql += "  WHERE";
		sql += "    " + idxTableNameValues + ".originNameId=" + idxTableNameOrigins + ".id";
		sql += "    AND";
		sql += "    " + idxTableNameValues + ".lookupKey ";
		return sql;
	}

	private String getLookupSql(String lookupString) {
		String sql = getSqlPrefix();
		sql += "= '" + lookupString + "' order by pageRank DESC";
		return sql;
	}

	private String getLookupPrefixSql(String lookupString) {
		String sql = getSqlPrefix();
		sql += "like '" + lookupString + "%' order by pageRank DESC";
		return sql;
	}

	private String getLookupTokensSql(String lookupString) {
		String sql = getSqlPrefix();
		sql += "like '%" + lookupString + "%' order by pageRank DESC"; //'%" + lookupString + "%'";//NOTE: TOO SLOW! order by CHAR_LENGTH(lookupKey)";
		logger.info("EXECUTING SQL LOOKUP" + sql);
		return sql;
	}

	public int size() {
		int size = 0;

		// query db
		// TODO if SQL statement is wrong, this will throw a null pointer
		// exception...
		String sqlCount = "SELECT count(id) FROM " + idxTableNameValues;
		SqlResult sqlResult = data.executeSqlQuery(sqlCount);
		List<Object[]> rows = sqlResult.getObjectTable();
		if (rows.size() > 0) {
			Object value = rows.iterator().next()[0];
			if (value instanceof BigDecimal) {
				size = ((BigDecimal) value).intValue();
			} else if (value instanceof Long) {
				size = ((Long) value).intValue();
			} else {
				size = ((Integer) value).intValue();
			}
		}

		return size;
	}

	public void add(String key, Term term) {
		add(key, term.type, term.value, term.origin, term.originName, term.filteredClass, term.filteredProp, term.pageRank);
	}

	public void add(String key, TermType type, String value, TermOrigin origin, String originName, String originClass, String originProp, Double pageRank) {
		if (cachedOrigins == null) {
			initOrigins();
		}

		// Integer originId = new Integer(term.origin.toInt());
		Integer originId = cachedOrigins.get(originName+originClass+originProp);
		if (originId == null) {

			// new origin
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO " + idxTableNameOrigins);
			sql.append("  (id, originName, originClass, originProp, pageRank)");
			sql.append("    VALUES");
			sql.append("  (" + (maxOriginId + 1) + ", '" + originName + "', '" + originClass + "', '" + originProp +"', " + pageRank+  " )");
			if (!data.executeSqlUpdate(sql.toString())) {
				logger.error("Inserting origin into index failed.");
			} else {
				maxOriginId++;
				cachedOrigins.put(originName+originClass+originProp, maxOriginId);
				originId = maxOriginId;
			}
		}

		// assert max value id
		assertMaxValueId();
		// insert tuple
		StringBuffer sql = new StringBuffer();
		if(valuesTable == null) {
			getSqlCreateT1();
		}
		sql.append(valuesTable.generateInsertStatement());
		
		sql.append("  (" + (maxValueId + 1)+ ", '" + Escape.safeSql(key) + "', '" + Escape.safeSql(value) + "', ");
		sql.append(type.toInt() + ", " + origin.toInt() + ", " + originId + ")");
		if (data.executeSqlUpdate(sql.toString())) {
			maxValueId++;
		} else {
			logger.error("Inserting tuple failed.");
		}
	}

	public void remove(String key, Term term) {
		// TODO implement remove on db table index
	}

	public void beginFast() throws SQLException {

		// open connection and
		// disable auto-commit
		if (fastConn != null) {
			throw new SQLException("Fast connection is already established.");
		}
		fastConn = data.getConnection();
		fastConn.setAutoCommit(false);

		// create prepared statements
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO " + idxTableNameOrigins);
		sql.append("  (id, originName, originClass, originProp, pageRank)");
		sql.append("    VALUES");
		sql.append("  (?, ?, ?, ?)");
		insertOrigin = fastConn.prepareStatement(sql.toString());
		sql = new StringBuffer();
		sql.append("INSERT INTO " + idxTableNameValues);
		sql.append("  (id, lookupKey, value, type, origin, originNameId)");
		sql.append("    VALUES");
		sql.append("  (?, ?, ?, ?, ?, ?)");
		insertValue = fastConn.prepareStatement(sql.toString());
	}

	public void fastAdd(String key, TermType type, String value, TermOrigin origin, String originName) throws SQLException {

		// get origin
		if (cachedOrigins == null) {
			initOrigins();
		}
		Integer originId = cachedOrigins.get(originName);
		if (originId == null) {

			// new origin
			insertOrigin.setInt(1, maxOriginId + 1);
			insertOrigin.setString(2, originName);
			insertOrigin.addBatch();

			// update state
			maxOriginId++;
			cachedOrigins.put(originName, maxOriginId);
			originId = maxOriginId;
		}

		// assert max value id
		assertMaxValueId();

		// insert tuple
		insertValue.setInt(1, maxValueId);
		insertValue.setString(2, key);
		insertValue.setString(3, value);
		insertValue.setInt(4, type.toInt());
		insertValue.setInt(5, origin.toInt());
		insertValue.setInt(6, originId.intValue());
		insertValue.addBatch();

		// update state
		maxValueId++;
	}

	public void fastExecuteBatch() throws SQLException {
		insertOrigin.executeBatch();
		insertValue.executeBatch();
	}

	public void fastCommit() throws SQLException {
		insertOrigin.executeBatch();
		insertValue.executeBatch();
		fastConn.commit();
	}

	public void endFast() throws SQLException {

		// commit
		insertOrigin.executeBatch();
		insertValue.executeBatch();
		fastConn.commit();

		// close
		insertOrigin.close();
		insertValue.close();

		// re-enable auto-commit and close
		fastConn.setAutoCommit(true);
		fastConn.close();

		// set to null
		insertOrigin = null;
		insertValue = null;
		fastConn = null;
	}
	
	private void assertMaxOriginId() {
        if (maxOriginId < 0) {
            String sqlMax = "SELECT max(id) FROM " + idxTableNameOrigins;
            // TODO if SQL statement is wrong, this will throw a null pointer
            // exception...
            SqlResult sqlResult = data.executeSqlQuery(sqlMax);
            List<Object[]> rows = sqlResult.getObjectTable();
            if (rows.size() > 0) {
                Object idObj = rows.iterator().next()[0];
                if (idObj instanceof BigDecimal) {
                		maxOriginId = ((BigDecimal) idObj).intValue() + 1;
                } else if (idObj instanceof Long) {
                		maxOriginId = ((Long) idObj).intValue() + 1;
                } else {
                		maxOriginId = ((Integer) idObj).intValue() + 1;
                }
            } else {
            		maxOriginId = 0;
            }
        }
    }

	private void assertMaxValueId() {
		if (maxValueId < 0) {
			String sqlMax = "SELECT max(id) FROM " + idxTableNameValues;
			// TODO if SQL statement is wrong, this will throw a null pointer
			// exception...
			SqlResult sqlResult = data.executeSqlQuery(sqlMax);
			List<Object[]> rows = sqlResult.getObjectTable();
			if(rows == null || rows.size() == 0){
				maxValueId =0;
			}
			else {
				Object idObj = rows.iterator().next()[0];
				if(idObj == null){
					maxValueId = 0;
				}
				else if (idObj instanceof BigDecimal) {
					maxValueId = ((BigDecimal) idObj).intValue() + 1;
				} else if (idObj instanceof Long) {
					maxValueId = ((Long) idObj).intValue() + 1;
				} else {
					maxValueId = ((Integer) idObj).intValue() + 1;
				}
			}
		}
	}

	private List<Term> executeAndMaterialize(TokenList tokens, String sql) {

		// execute
		SqlResult sqlResult = data.executeSqlQuery(sql);
		List<Object[]> rows = sqlResult.getObjectTable();

		// materialize! :-)
		// TODO if SQL statement is wrong, this will throw a null pointer
		// exception...
		List<Term> result = new ArrayList<Term>();
		for (Object[] row : rows) {
			TermType type = null;
			if (row[3] instanceof BigDecimal) {
				type = TermType.fromInt(((BigDecimal) row[3]).intValue());
			} else {
				type = TermType.fromInt((Integer) row[3]);
			}
			String value = (String) row[2];
			TermOrigin origin = null;
			if (row[4] instanceof BigDecimal) {
				origin = TermOrigin.fromInt(((BigDecimal) row[4]).intValue());
			} else {
				origin = TermOrigin.fromInt((Integer) row[4]);
			}
			String originName = (String) row[7];
			
			String className = (String) row[8];
			
			String propName = (String) row[9];
			
			double pageRank = (float) row[10];
			
			Term term = new Term(type, tokens.getTokenizedText(), value, origin, originName, pageRank);
			term.setClassProp(className, propName);
			
			if(className != null &&(!className.contains("Property")) && (!className.contains("#Class")) && (propName != null) && (!propName.isEmpty()) && (!propName.contains("#equivalentClass"))) {
				term.setFilter();
			}
			else {
				term.setExactMatch();
			}
			result.add(term);
		}

		return result;
	}

	private void initOrigins() {
		cachedOrigins = new HashMap<String, Integer>();
		// execute SQL
		String sql = "SELECT * FROM " + idxTableNameOrigins + " ORDER BY id DESC";
		SqlResult sqlResult = data.executeSqlQuery(sql);
		List<Object[]> rows = sqlResult.getObjectTable();

		// map
		// TODO if SQL statement is wrong, this will throw a null pointer
		// exception...
		for (Object[] row : rows) {

			// id
			Integer id = null;
			Object idObj = row[0];
			if (idObj instanceof BigDecimal) {
				id = new Integer(((BigDecimal) idObj).intValue());
			} else if (idObj instanceof Long) {
				id = new Integer(((Long) idObj).intValue());
			} else {
				id = (Integer) idObj;
			}

			// name
			String name = (String) row[1];

			// put into map
			cachedOrigins.put(name, id);
		}

		// max id
		maxOriginId = 0;
		if (rows.size() > 0) {
			Object idObj = rows.iterator().next()[0];
			if (idObj instanceof BigDecimal) {
				maxOriginId = ((BigDecimal) idObj).intValue();
			} else if (idObj instanceof Long) {
				maxOriginId = ((Long) idObj).intValue();
			} else {
				maxOriginId = ((Integer) idObj).intValue();
			}
		}
	}

	// ------------------------------------------------------- object overrides

	public String toString() {
		return name;
	}

	@Override
	public void add(String key, TermType type, String value, TermOrigin origin, String originName) {
		// get class and prop that matched
		String className = SPARQLUtilsRemote.getTypeOfResource(Constants.REMOTE_REPO, originName);
		String propThatMatched = Term.getPropertyThatMatchesRemote(Constants.REMOTE_REPO, originName, key);
		add(key, type, value, origin, originName, className, propThatMatched, Constants.DEFAULT_PAGERANK);
		
	}

}

