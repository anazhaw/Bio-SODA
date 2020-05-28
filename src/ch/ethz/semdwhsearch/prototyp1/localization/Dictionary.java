package ch.ethz.semdwhsearch.prototyp1.localization;

/**
 * Dictionary for this application.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface Dictionary extends DictionaryDatabase {

	// ----------------------------------------------------------- common verbs

	String login();

	String logout();

	String save();

	String add();

	String go();

	String reload();
	
	String append();

	String redo();

	String goon();

	String iMFeelingLucky();

	String abort();

	// ----------------------------------------------------------- common terms

	String yes();

	String no();

	String and();

	String or();

	String hello();

	String thanks();

	String done();

	String notDone();

	String combined();

	String showAllEntryPoints();

	String operator();

	String businessObject();

	String value();

	String unknown();

	String infos();

	String tableName();

	String tableNames();

	String columnName();

	String columnNames();

	String filterValues();

	String columnFilters();

	String foreignKeys();

	String entryPoints();

	String queryGraphs();

	String nodeInfos();

	String results();

	String result();

	String count();

	String size();

	String downloadNTriples();

	String input();

	String process();

	String output();

	String score();

	String averageScore();

	String selectAlgorithmPhase();

	// ---------------------------------------------------- result query graphs

	String queryGraphsClassifiedTerms();

	String queryGraphsRanked();

	String queryGraphsRelated();

	String queryGraphsComplete();

	// ----------------------------------------------------------------- header

	String langCode();

	String langName();

	String welcome();

	String mainTitle();

	// ------------------------------------------------------------ page titles

	String home();

	String graph();

	String entities();

	String metadata();

	String metadataBrowser();

	String metadataStructure();

	String queryInput();

	String queryClassification();

	String queryLineage();

	String queryGraphLineage();

	String sql();

	String domainOntologyViewer();

	String schemaViewer();

	String config();

	String admin();

	String pagetitleLucky();

	String pagetitleLookup();

	String pagetitleRank();

	String pagetitleTables();

	String pagetitleFilters();

	String pagetitleSql();

	// ----------------------------------------------------------------- config

	String dbconn();

	String dbvendor();

	String schema();

	String username();

	String password();

	String dbname();

	String hostname();

	String port();

	String sid();

	String configDirectory();
	
	String dataDirectory();

	String invertedIndex();
	
	String appendToIndex();

	// ------------------------------------------------------------------ admin

	String baseData();

	String propertyName();

	String storedValue();

	String newValue();

	String tableStatistics();

	String joinCardinalities();

	String join();

	String cardinality();

	String classification();

	String reloadBaseDataClassificationIndex();

	String reloadMetadataClassificationIndex();

	// --------------------------------------------------------------- problems

	String problems();

	String incompleteJoins();

	// ---------------------------------------------------------------- actions

	// ---------------------------------------------------------- info messages

	String dataLoadingCompleted();

	// --------------------------------------------------------- error messages

	// ------------------------------------------------------- warning messages

}
