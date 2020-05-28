package ch.ethz.semdwhsearch.prototyp1.classification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.classification.index.Index;
import ch.ethz.semdwhsearch.prototyp1.classification.index.impl.DbTableIndex_SPARQL;
import ch.ethz.semdwhsearch.prototyp1.classification.index.impl.HashMapIndex;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermOrigin;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.ethz.semdwhsearch.prototyp1.config.Config;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.data.Data;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.metadata.ModelInfo;
//import ch.zhaw.nlp.Tokenizer;
import ch.ethz.semdwhsearch.prototyp1.tools.Tokenizer;
import ch.zhaw.nlp.NlpPipeline;
import ch.zhaw.nlp.TokenList;

/**
 * Main class to perform classification of a query.
 * <p>
 * The classification currently consists of a dictionary from key to list of
 * terms.
 * 
 * @author Lukas Blunschi, Ana Sima
 * 
 */
public class Classification {

	private static final Logger logger = LoggerFactory.getLogger(Classification.class);

	// ---------------------------------------------------------------- members

	private final Config config;

	private final Tokenizer tokenizer;

	private final NlpPipeline nlpPipeline;

	private Index indexDictionary;

	// ----------------------------------------------------------- construction

	public Classification(Metadata metadata, Data data, Config config, boolean reloadIdx) {
		this(metadata, data, config, reloadIdx, false);
    }
	
    public Classification(Metadata metadata, Data data, Config config, boolean reloadIdx, boolean appendIdx) {
		this.config = config;
		this.tokenizer = new Tokenizer();
		this.nlpPipeline = new NlpPipeline(config);
		if (data.isConnected() && config.isDsUseDbIndex(false)) {
			this.indexDictionary = new DbTableIndex_SPARQL("metadata", data, reloadIdx, appendIdx);
		} else {
			this.indexDictionary = new HashMapIndex();
			// make sure the index is reloaded
			// (since hashmaps are initially empty)
			reloadIdx = true;
		}
		// TODO this reload flag might be not needed
		if (reloadIdx || appendIdx) {
			logger.info("Creating classification index...");
			reloadIndex(true, metadata, data, appendIdx);
			reloadIndex(false, metadata, data, appendIdx);
			String numStr = " (containing " + indexDictionary.size() + " mappings from metadata";
			logger.info("Classification index created" + numStr + ".");
		} else {
			logger.info("Using persistent index!");
		}
	}

	public NlpPipeline getNlpPipeline() {
		return nlpPipeline;
	}

	// ---------------------------------------------------------------- loading

	public void reloadIndex(boolean metadataIndex, Metadata metadata, Data data, boolean appendIdx) {

		// vars
		Map<String, StringBuffer> values = null;

		// switch on metadata or base data index
		if (metadataIndex) {

			// re-init index
			if (data.isConnected() && config.isDsUseDbIndex(false)) {
				indexDictionary = new DbTableIndex_SPARQL("metadata", data, false, appendIdx);
			} else {
				indexDictionary = new HashMapIndex();
			}

			// domain ontologies
			int count = 0;
			for (ModelInfo info : metadata.getModelInfos(Metadata.TYPE_DO)) {
				logger.info("\n\n ##### ADDING MODEL "+ info.getModelName());
				values = info.getCaptions();
				logger.info("\n\n ##### CAPTIONS "+ values.size());
				for(Entry<String, StringBuffer> kv: values.entrySet()) {
					String[] uriProp = kv.getKey().split("###"); // split by "."
					String uri = uriProp[0];
					String className = null;
					String propName = null;
					if(uriProp.length > 1)
						className = uriProp[1];
					if(uriProp.length > 2)
						propName = uriProp[2];

					//also get pageRank
					Double pageRank = Constants.DEFAULT_PAGERANK;
					if(Constants.usePageRanks) {
						if(className != null && className.contains("Property")){
							pageRank = Constants.PROPERTY_PAGERANK;
						}
						else {
							//if multiple datasets are integrated, several ranks may co-exist, always take max
							String queryString1 = "SELECT (max(?rank) as ?max_rank) where { <" + uri + "> <http://purl.org/voc/vrank#pagerank> ?rank. } ";
							final Query query = QueryFactory.create(queryString1);
							QueryExecution qexec = QueryExecutionFactory.sparqlService(Constants.PAGERANK_REPO, query);

							// Create a single execution of this query, apply to a model
							// which is wrapped up as a Dataset
							try {
								// Assumption: it’s a SELECT query.
								final ResultSet rs = qexec.execSelect();
								if(!rs.hasNext()){
									logger.info("PageRank: NO SOLUTIONS.");
								}
								// The order of results is undefined.
								for (; rs.hasNext();) {
									final QuerySolution rb = rs.nextSolution();
									if(rb.get("?max_rank") != null)
										pageRank = (double)rb.get("?max_rank").asLiteral().getFloat();

								}
							} catch (Exception e){
								e.printStackTrace();
							} finally {
								qexec.close();
							}
						}
					}
					addTerm(kv.getValue().toString(), TermType.RDF_MATCH, TermOrigin.DOMAIN_ONTOLOGY, uri, className, propName, pageRank, false);
					count ++;
				}
			}
			logger.info(count + " terms added to classification.");
		} 
	}

	public void addTerms(Collection<String> values, TermType type, TermOrigin origin, String originName) {
		for (String value : values) {
			addTerm(value, type, origin, originName);
		}
	}

	/**
	 * Adds a new term to the dictionary.
	 * 
	 * @param value
	 *            the value of the new term and the key under which it is stored
	 *            in the dictionary.
	 * @param type
	 *            the type of the new term.
	 * @param origin
	 *            the origin of the new term.
	 * @param originName
	 *            the name of the origin of the new term.
	 */
	public void addTerm(String value, TermType type, TermOrigin origin, String originName) {

		// ignore null values
		if (value == null) {
			return;
		}

		// tokenize
		Set<String> lookupKeys = new HashSet<String>();
		lookupKeys.addAll(nlpPipeline.getLookupKeys(value));

		// add to dictionary
		Index dictionary = indexDictionary;
		for (String key : lookupKeys) {
			dictionary.add(key, new Term(type, key, value, origin, originName));
		}
	}

	public void addTerm(String value, TermType type, TermOrigin origin, String originName, String className, String propName, Double pageRank, boolean big) {

		// ignore null values
		if (value == null) {
			return;
		}

		// tokenize
		Set<String> lookupKeys = new HashSet<String>();
		lookupKeys.addAll(nlpPipeline.getLookupKeys(value));

		// add to dictionary
		Index dictionary = indexDictionary;
		for (String key : lookupKeys) {
			Term term = new Term(type, key, value, origin, originName, className, propName, pageRank);
			dictionary.add(key, term);
		}
	}


	/**
	 * Removes a term from the dictionary.
	 * 
	 * @param value
	 *            the value of the term and the key under which it is stored in
	 *            the dictionary.
	 * @param type
	 *            the type of the term to remove.
	 * @param origin
	 *            the origin of the term to remove.
	 * @param originName
	 *            the name of the origin of the term to remove.
	 */
	public void removeTerm(String value, TermType type, TermOrigin origin, String originName) {

		// ignore null values
		if (value == null) {
			return;
		}

		// prepare lookup string
		String tmp = tokenizer.splitToKey(value);

		// add to dictionary
		Index dictionary = indexDictionary;
		dictionary.remove(tmp, new Term(type, tmp, value, origin, originName));
	}

	// ----------------------------------------------------------- index access

	public Index getIndex(boolean metadataIndex) {
		return indexDictionary;
	}

	// ------------------------------------------------------ query preparation

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	// ----------------------------------------------------------------- lookup

	/**
	 * Lookup terms for given key.
	 * 
	 * @param key
	 *            key to lookup terms (simplified version of value).
	 * @return all terms stored for given key (never null).
	 */
	public List<Term> lookup(TokenList tokens) {
		List<Term> result = new ArrayList<Term>();
		List<Term> resultSmall = indexDictionary.lookup(tokens);
		if (resultSmall != null) {
			result.addAll(resultSmall);
		}
		return result;
	}

	/**
	 * Lookup terms for given key prefix.
	 * 
	 * @param keyPrefix
	 *            prefix of key to lookup terms for (the key is a simplified
	 *            version of the value).
	 * @return all terms stored for matching keys (never null).
	 */
	public List<Term> lookupPrefix(TokenList tokens) {
		List<Term> result = new ArrayList<Term>();
		List<Term> resultSmall = indexDictionary.lookupPrefix(tokens);
		if (resultSmall != null) {
			result.addAll(resultSmall);
		}
		return result;
	}

	/**
	 * Lookup terms for given substring of key.
	 * 
	 * @param keySubstring
	 *            substring of key to lookup terms for (the key is a simplified
	 *            version of the value).
	 * @return all terms stored for matching keys (never null).
	 */
	public List<Term> lookupSubstring(TokenList tokens) {
		List<Term> result = new ArrayList<Term>();
		List<Term> resultSmall = indexDictionary.lookupSubstring(tokens);
		if (resultSmall != null) {
			result.addAll(resultSmall);
		}
		return result;
	}

	// ------------------------------------------------------- helper functions

	// ------------------------------------------------------- object overrides

}
