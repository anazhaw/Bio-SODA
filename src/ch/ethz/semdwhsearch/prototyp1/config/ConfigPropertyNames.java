package ch.ethz.semdwhsearch.prototyp1.config;

/**
 * Config property names.
 * 
 * @author Lukas Blunschi, Ana Sima
 * 
 */
public abstract class ConfigPropertyNames {

	/* Note: this class contains only constants which are used in the code. */

	public static final String DS_INDEX_TOKENIZE_KEY = "ds.index.tokenizeKey";

	public static final String ALGO_LOOKUP_SUBSTRING_MATCHING = "algo.lookup.substringMatching";

	public static final String ALGO_LOOKUP_PREFIX_MATCHING = "algo.lookup.prefixMatching";

	public static final String ALGO_FILTERS_USE_LOOKUP_KEY = "algo.filters.useLookupKey";

	public static final String ALGO_SPARQL_SUBSTRING_MATCHING = "algo.sparql.substringMatching";
	
	public static final String ALGO_FUZZY_MATCHING = "algo.lookup.fuzzyMatching";

	public static final String ALGO_SPARQL_PREFIX_MATCHING = "algo.sparql.prefixMatching";

}
