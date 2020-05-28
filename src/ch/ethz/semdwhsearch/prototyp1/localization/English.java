package ch.ethz.semdwhsearch.prototyp1.localization;

/**
 * English dictionary.
 * 
 * @author Lukas Blunschi
 * 
 */
public class English extends EnglishDatabase implements Dictionary {

	public static final String LANGCODE = "en";

	// ----------------------------------------------------------- common verbs

	public String login() {
		return "Login";
	}

	public String logout() {
		return "Logout";
	}

	public String save() {
		return "Save";
	}

	public String add() {
		return "Add";
	}

	public String go() {
		return "Go";
	}

	public String reload() {
		return "Reload";
	}
	
	public String append() {
		return "Append";
	}

	public String appendToIndex() {
		return "Append To Existing Index";
	}

	public String redo() {
		return "Redo";
	}

	public String goon() {
		return "Continue";
	}

	public String iMFeelingLucky() {
		return "I&apos;m Feeling Lucky";
	}

	public String abort() {
		return "Abort";
	}

	// ----------------------------------------------------------- common terms

	public String yes() {
		return "Yes";
	}

	public String no() {
		return "No";
	}

	public String and() {
		return "and";
	}

	public String or() {
		return "or";
	}

	public String hello() {
		return "Hi";
	}

	public String thanks() {
		return "Thanks";
	}

	public String done() {
		return "Done";
	}

	public String notDone() {
		return "Not Done";
	}

	public String combined() {
		return "Combined";
	}

	public String showAllEntryPoints() {
		return "Show All Entry Points";
	}

	public String operator() {
		return "Operator";
	}

	public String businessObject() {
		return "Business Object";
	}

	public String value() {
		return "Value";
	}

	public String unknown() {
		return "Unknown";
	}

	public String infos() {
		return "Infos";
	}

	public String tableName() {
		return "Table Name";
	}

	public String tableNames() {
		return "Table Names";
	}

	public String columnName() {
		return "Column Name";
	}

	public String columnNames() {
		return "Column Names";
	}

	public String filterValues() {
		return "Filter Values";
	}

	public String columnFilters() {
		return "Column Filters";
	}

	public String foreignKeys() {
		return "Foreign Keys";
	}

	public String entryPoints() {
		return "Entry Points";
	}

	public String queryGraphs() {
		return "Query Graphs";
	}

	public String nodeInfos() {
		return "Node Infos";
	}

	public String results() {
		return "Results";
	}

	public String result() {
		return "Result";
	}

	public String count() {
		return "Count";
	}

	public String size() {
		return "Size";
	}

	public String downloadNTriples() {
		return "Download N-Triples";
	}

	public String input() {
		return "Input";
	}

	public String process() {
		return "Process";
	}

	public String output() {
		return "Output";
	}

	public String score() {
		return "Score";
	}

	public String averageScore() {
		return "Average Score";
	}

	public String selectAlgorithmPhase() {
		return "Select Algorithm Phase";
	}

	// ---------------------------------------------------- result query graphs

	public String queryGraphsClassifiedTerms() {
		return "Query Graphs - Classified Terms";
	}

	public String queryGraphsRanked() {
		return "Query Graphs - Ranked";
	}

	public String queryGraphsRelated() {
		return "Query Graphs - Related";
	}

	public String queryGraphsComplete() {
		return "Query Graphs - Complete";
	}

	// ----------------------------------------------------------------- header

	public String langCode() {
		return LANGCODE;
	}

	public String langName() {
		return "English";
	}

	public String welcome() {
		return "Welcome";
	}

	public String mainTitle() {
		return "\nBioSODA (question answering over domain knowledge graphs)";
	}

	// ------------------------------------------------------------ page titles

	public String home() {
		return "Home";
	}

	public String graph() {
		return "Graph";
	}

	public String entities() {
		return "Entities";
	}

	public String metadata() {
		return "Metadata";
	}

	public String metadataBrowser() {
		return "Metadata Browser";
	}

	public String metadataStructure() {
		return "Metadata Structure";
	}

	public String queryInput() {
		return "Query Input";
	}

	public String queryClassification() {
		return "Query Classification";
	}

	public String queryLineage() {
		return "Query Lineage";
	}

	public String queryGraphLineage() {
		return "Query Graph Lineage";
	}

	public String sql() {
		return "SQL";
	}

	public String domainOntologyViewer() {
		return "Domain Ontology Viewer";
	}

	public String schemaViewer() {
		return "Schema Viewer";
	}

	public String config() {
		return "Configuration";
	}

	public String admin() {
		return "Admin";
	}

	public String pagetitleLucky() {
		return "I'm Feeling Lucky";
	}

	public String pagetitleLookup() {
		return "Lookup";
	}

	public String pagetitleRank() {
		return "Rank and Top-N";
	}

	public String pagetitleTables() {
		return "Tables and Joins";
	}

	public String pagetitleFilters() {
		return "Filters";
	}

	public String pagetitleSql() {
		return "SQL";
	}

	// ----------------------------------------------------------------- config

	public String dbconn() {
		return "Database Connection";
	}

	public String dbvendor() {
		return "Database Vendor";
	}

	public String schema() {
		return "Schema";
	}

	public String username() {
		return "Username";
	}

	public String password() {
		return "Password";
	}

	public String dbname() {
		return "Database Name";
	}

	public String hostname() {
		return "Hostname";
	}

	public String port() {
		return "Port";
	}

	public String sid() {
		return "SID";
	}
	
	public String dataDirectory() {
		return "RDF Data Directory";
	}

	public String configDirectory() {
		return "Configuration Directory";
	}

	public String invertedIndex() {
		return "Inverted Index";
	}

	// ------------------------------------------------------------------ admin

	public String baseData() {
		return "Base Data";
	}

	public String propertyName() {
		return "Property Name";
	}

	public String storedValue() {
		return "Stored Value";
	}

	public String newValue() {
		return "New Value";
	}

	public String tableStatistics() {
		return "Table Statistics";
	}

	public String joinCardinalities() {
		return "Join Cardinalities";
	}

	public String join() {
		return "Join";
	}

	public String cardinality() {
		return "Cardinality";
	}

	public String classification() {
		return "Classification";
	}

	public String reloadBaseDataClassificationIndex() {
		return "Reload Base Data Classification Index";
	}

	public String reloadMetadataClassificationIndex() {
		return "Reload Metadata Classification Index";
	}

	// --------------------------------------------------------------- problems

	public String problems() {
		return "Problems";
	}

	public String incompleteJoins() {
		return "Incomplete Joins";
	}

	// ---------------------------------------------------------------- actions

	// ---------------------------------------------------------- info messages

	public String dataLoadingCompleted() {
		return "RDF data, index and classification loaded.";
	}

	// --------------------------------------------------------- error messages

	// ------------------------------------------------------- warning messages

}
