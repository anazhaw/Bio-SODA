package ch.ethz.semdwhsearch.prototyp1.constants;
/**
 * Constants used in this app.
 * 
 * @author Lukas Blunschi, Ana Sima
 * 
 */
public class Constants {

	// ----------------------------------------------------------------- values

	public static final double thresholdWord2VecSimilarity = 0.3;

	public static boolean useWordEmbeddings = false;

	public static final String word2VecPath = "PMC-w2v.bin";

	public static final String VERSION = "1.0.0";

	public static final String CONTRIBUTORS = "Lukas Blunschi, Ana Sima";

	public static final int DISPLAY_COUNT = 100;

	public static final int MAX_FIELD_LENGTH = 255;

	public static final int MAX_MATCHES_COUNT = 5;

	public static final int MAX_RESULT_COUNT = 5;

	public static final int MAX_TERM_COMBINATIONS = 300;

	public static final int MAX_HOPS_SHORTEST_PATH = 4;

	public static final int MAX_ALTERNATIVES_COUNT = 10;

	public static final int MAX_LOOKUP_COUNT = 1000;

	public static final int MAX_SHORTEST_PATHS_ALTERNATIVES_COUNT = 2;

	public static final int RANKED_RESULT_COUNT = 10;

	public static final int MAX_SPARQL_RESULTS_COUNT = 100;

	public static final int EXECUTED_QUERIES = 50;

	public static final boolean ENABLE_ORDER_BY = false;

	public static final double INITIAL_SCORE = -1.0;

	public static final boolean ADD_DESCRIPTIONS = false;

	public static boolean usePageRanks = true;

	public static final double DEFAULT_PAGERANK = 0.1;  //0.15;

	public static final double PROPERTY_PAGERANK = 70;

	public static boolean indexURIFragments = false;

	public static final String PUNCTUATION_FOR_SPLITS = "[-_]";

	public static final String NEW = "new";

	public static final String NAME = "name";

	public static final String TYPE = "type";

	public static final String CHILD = "child";

	// -------------------------------------------------------------- filenames

	public static final String FN_CONFIG = "semdwhsearch.properties";

	//public static final String FN_INIT = "";

	public static final String FN_AUTO_INIT = "";
	public static final String FN_METADATA_MAPPING = "metadata-mapping.txt";
	public static final boolean ONDISK_MODEL = false;
	public static final boolean REMOTE_INDEXING = true;
	public static final int SPARQL_INDEXING_BATCH_SIZE = 1000;
	public static final String TBD_DATA_DIR = "";

	public static final String MODEL_NAME = "sample";

	public static final boolean USE_REMOTE = true;
	public static final String REMOTE_REPO = "http://biosoda.expasy.org:7200/repositories/CompanyEmployeeSampleData";
	public static final String PAGERANK_REPO = "http://biosoda.expasy.org:7200/repositories/sample_page_ranks";

	// -------------------------------------------------------- parameter names

	public static final String P_ACTION = "action";

	public static final String P_MSG = "msg";

	public static final String P_SPARQL = "sparql";

	public static final String P_URI = "uri";

	public static final String P_METADATA_INDEX = "metadataIndex";

	// ----------------------------------------------------- session attributes

	/**
	 * [String, 2 chars, e.g. 'en']
	 */
	public static final String A_LANG_CODE = "langCode";

	// ------------------------------------------------------------- db vendors

	public static final String DBV_ORACLE = "oracle";

	public static final String DBV_MYSQL = "mysql";

	public static final String DBV_DERBY = "derby";

}
