package ch.ethz.semdwhsearch.prototyp1.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.dag.DagEdge;
import ch.ethz.dag.DagEdgeImpl;
import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.html5.dag.Parameter;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.data.Data;
import ch.ethz.semdwhsearch.prototyp1.data.DataSingleton;
import ch.ethz.semdwhsearch.prototyp1.metadata.mapping.MetadataMapping;
import ch.ethz.semdwhsearch.prototyp1.tools.StopWatch;
import ch.ethz.semdwhsearch.prototyp1.tools.Tokenizer;
import ch.zhaw.biosoda.FederatedSummaryGraph;
import ch.zhaw.biosoda.SPARQLUtilsRemote;
import ch.zhaw.biosoda.SummaryRDFGraph;


/**
 * Information about a model.
 * 
 * @author Lukas Blunschi, Ana Sima
 * 
 */
public class ModelInfo {

	private static final Logger logger = LoggerFactory.getLogger(ModelInfo.class);

	private final String modelName;

	private final Model model;

	private final int type;

	/**
	 * All node URIs contained in this model.
	 */
	private final Set<String> uris;

	/**
	 * All edges between two nodes in this model.
	 */
	private final Collection<DagEdge<String>> edges;

	/**
	 * A map from node URI to caption.
	 */
	private final Map<String, StringBuffer> captionMap;

	/**
	 * A map from node URI to list of parameters (additional text information on
	 * this node).
	 */
	private final Map<String, List<Parameter>> parameterMap;

	/**
	 * A map from literal to node URI.
	 */
	private final Map<String, Set<String>> literalMap;

	private Map<String, String> classMap = new HashMap<String, String>();

	private SummaryRDFGraph summaryGraph;

	//todo: fix this
	private FederatedSummaryGraph federatedSummaryGraph;

	public ModelInfo(Model model, String modelName, int type, MetadataMapping mapping, boolean reloadIndex, boolean isRemote) {

		// init members
		this.modelName = modelName;
		this.type = type;
		this.uris = new HashSet<String>();
		this.edges = new ArrayList<DagEdge<String>>();
		this.captionMap = new HashMap<String, StringBuffer>();
		this.parameterMap = new HashMap<String, List<Parameter>>();
		this.literalMap = new HashMap<String, Set<String>>();
		this.model = model;

		if(reloadIndex){
			if(!isRemote) {
				// parse model from disk
				parseModel(model, mapping);
			} else {
				// parse from remote sparql endpoint
				String endpoint = Constants.REMOTE_REPO;
				parseModelFromRemote(endpoint, mapping);
			}
		}

		// logging info output
		int nU = uris.size();
		int nE = edges.size();
		int nC = captionMap.size();
		int nP = getParameterValues().size();
		String info = "";
		info += "model info: " + modelName + ": ";
		info += "#uris=" + nU + ", #edges=" + nE + ", #captions=" + nC + ", #params=" + nP;
		logger.info(info);
	}

	public Model getModel() {
		return model;
	}

	public String getModelName() {
		return modelName;
	}

	public int getType() {
		return type;
	}

	public void setSummaryGraph(SummaryRDFGraph summaryGraph){
		this.summaryGraph = summaryGraph;
	}

	public SummaryRDFGraph getSummaryGraph(){
		return this.summaryGraph;
	}

	public void setFederatedSummaryGraph(FederatedSummaryGraph summaryGraph){
		this.federatedSummaryGraph = summaryGraph;
	}

	public FederatedSummaryGraph getFederatedSummaryGraph(){
		return this.federatedSummaryGraph;
	}

	public Map<String, StringBuffer> getCaptions() {
		/*List<String> captions = new ArrayList<String>();
		for (String buf : captionMap.values()) {
			captions.add(buf.toString());
		}*/
		return captionMap;
	}

	public Collection<String> getParameterValues() {
		List<String> values = new ArrayList<String>();
		for (List<Parameter> params : parameterMap.values()) {
			for (Parameter param : params) {
				values.add(param.value);
			}
		}
		return values;
	}

	public Collection<String> getParameterValues(String paramName) {
		List<String> values = new ArrayList<String>();
		for (List<Parameter> params : parameterMap.values()) {
			for (Parameter param : params) {
				if (param.key.equals(paramName)) {
					values.add(param.value);
				}
			}
		}
		return values;
	}

	public Collection<String> getUris() {
		return uris;
	}

	public void addLiteral(String key, String URI) {
		HashSet<String> uriList = (HashSet<String>) literalMap.get(key);
		if(uriList == null){
			uriList = new HashSet<String>();
		}
		uriList.add(URI);
		literalMap.put(key, uriList);
	}

	/**
	 * Lookup URIs by literal.
	 * 
	 * @param literal
	 * @return list of URIs (maybe empty, but never null).
	 */
	public List<String> getUriByLiteral(String literal) {
		List<String> result = new ArrayList<String>();

		if(literalMap.get(literal) != null) {
			result.addAll(literalMap.get(literal));
		}
		else{
			logger.warn("NOT FOUND: " + literal +  "! in: "+ literalMap);
		}
		return result;
	}

	// ------------------------------------------------------------- operations

	/**
	 * Parse the given model from a local (in-memory) model object.
	 * <p>
	 * This method loop over all statements in the given model and extracts text
	 * values (captions, parameters), edges and uris.
	 */
	private void parseModel(Model model, MetadataMapping mapping) {

		// property names caption
		Set<String> pnsCaptionSet = mapping.getGeneralPropNamesCaptionSet();

		//TODO: implement STREAMING over statements instead of this
		// ALSO, index incrementally, not in a full list
		Data data = DataSingleton.getInstance().getData();
		//Index bigDictionary = new DbTableIndex("metadata", data, true);
		Tokenizer tokenizer = new Tokenizer();
		HashSet<String> labelDefinedForURI = new HashSet<String>();

		// loop over all statements
		StmtIterator iter = model.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.next();

			// get statement content
			Resource subject = stmt.getSubject();
			Property property = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			// ignore statements without subject URI
			String subjectUri = subject.getURI();
			if (subjectUri == null) {
				continue;
			}

			String propertyURI = property.getURI();

			//TODO: save to originName as <class>:<property> or <uri>: <"uri">

			// switch on object class
			if (object.isLiteral()) {
				String literal = object.asLiteral().getLexicalForm();
				//don't index numbers
				if(literal.matches("-?\\d+(\\.\\d+)?"))
					continue;
				
				// switch on property name
				String localName = property.getLocalName();
				//index literals as indicated by the mapping properties, e.g. label (or everything if the set is empty)
				if (pnsCaptionSet.size() == 0 || pnsCaptionSet.contains(localName.toLowerCase())) {

					//here also get CLASS of subject
					String className = classMap.get(subjectUri);
					if(className == null){
						className = SPARQLUtilsRemote.getTypeOfResource(Constants.REMOTE_REPO, subjectUri);
						classMap.put(subjectUri, className);
					}

					// caption
					String captionMapKey = subjectUri + "###" + className + "###" + "<"+propertyURI+">";

					
					StringBuffer buf = captionMap.get(captionMapKey);

					if (buf == null) {
						buf = new StringBuffer();
						captionMap.put(captionMapKey, buf);
					} else {
						//logger.warn("two captions found on node " + subjectUri);
					}
					if (buf.length() > 0) {
						buf.append(" ");
					}
					


					String[] splits = literal.split(Constants.PUNCTUATION_FOR_SPLITS);

					for(String word : splits) {		
						if(!buf.toString().contains(word)) {
							buf.append(word);
							buf.append(" ");
						}
					}

					if(!buf.toString().contains(object.asLiteral().getLexicalForm())){
						buf.append(object.asLiteral().getLexicalForm());
						buf.append(" ");
					}

					captionMap.put(captionMapKey, buf);
					labelDefinedForURI.add(subjectUri);

				}

				// additional information
				List<Parameter> list = parameterMap.get(subjectUri);
				if (list == null) {
					list = new ArrayList<Parameter>();
					parameterMap.put(subjectUri, list);
				}
				list.add(new Parameter(localName, object.asLiteral().getLexicalForm()));
			} else {


				// edges
				String edgeName = property.getLocalName();
				String objectUri = null;

				if (object.isAnon()) {
					objectUri = object.asNode().getBlankNodeLabel();
				} else {
					objectUri = object.as(Resource.class).getURI();
				}

				edges.add(new DagEdgeImpl<String>(edgeName, subjectUri, objectUri));

				// uris
				uris.add(subjectUri);
				uris.add(objectUri);
			}
		}

		//iterate through all keys in the captionMap, camel case split and add as indexed
		if(Constants.indexURIFragments) {
			for(Map.Entry<String, StringBuffer> entry : captionMap.entrySet()) {
				String uriString = entry.getKey().split("###")[0];
				//HERE CAMEL CASE SPLIT

				StringBuffer buf = captionMap.get(entry.getKey());
				if (buf == null) {
					buf = new StringBuffer();
					captionMap.put(entry.getKey(), buf);
				}
				String localName = SPARQLUtilsRemote.getLiteralFromString(uriString);
				//TODO: if only numbers, DON't INDEX
				if(localName == null)
					continue;
				String[] splits = localName.split(Constants.PUNCTUATION_FOR_SPLITS);

				for(String word : splits) {	
					//also split camelCase
					String[] camelCaseSplit = word.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
					for(String camelCaseSplittedWord : camelCaseSplit){	
						if(!buf.toString().contains(camelCaseSplittedWord)) {
							buf.append(camelCaseSplittedWord);
							buf.append(" ");
						}
					}
				}

				if(!buf.toString().contains(localName)) {
					buf.append(" ");
					buf.append(localName);
				}
			}
		}

		//do a final pass through uris that do not have anything indexed (in caption map) yet

		StmtIterator iter2 = model.listStatements();
		while (iter2.hasNext()) {
			Statement stmt = iter2.next();

			// get statement content
			Resource subject = stmt.getSubject();
			Property property = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			// get uris, check captionmap

			//subject
			String subjUri = subject.getURI();
			String className = classMap.get(subjUri);
                        if(className == null) {
				className = SPARQLUtilsRemote.getTypeOfResource(Constants.REMOTE_REPO, subjUri);
				classMap.put(subjUri, className);
                        }
			String key =  subjUri + "###" + className + "###" + ""; //property is the URI
			//todo: decide if we want to only do this for URIs which have no label assigned
			if(subjUri != null && 
					(Constants.indexURIFragments || (!(labelDefinedForURI.contains(subjUri))))) {
				StringBuffer buf = new StringBuffer();
				String localName = SPARQLUtilsRemote.getLiteralFromString(subjUri);
				if(localName == null)
					continue;
				//TODO: if only numbers, DON't INDEX
				String[] splits = localName.split(Constants.PUNCTUATION_FOR_SPLITS);

				for(String word : splits) {		
					//also split camelCase
					String[] camelCaseSplit = word.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
					for(String camelCaseSplittedWord : camelCaseSplit){
						if(!buf.toString().contains(camelCaseSplittedWord)) {
							buf.append(camelCaseSplittedWord);
							buf.append(" ");
						}
					}
				}
				if(!buf.toString().contains(localName)) {
					buf.append(localName);
				}
				captionMap.put(key, buf);
			}

			//predicate
			String propUri = property.getURI();
			className = classMap.get(propUri);
                        if(className == null){
				className = SPARQLUtilsRemote.getTypeOfResource(Constants.REMOTE_REPO, propUri);
                                classMap.put(propUri, className);
                        }
			key =  propUri + "###" + className + "###" + "";
			if(propUri != null  && 
				(Constants.indexURIFragments 
					|| (!(labelDefinedForURI.contains(propUri))))) {
				StringBuffer buf = new StringBuffer();
				String localName = SPARQLUtilsRemote.getLiteralFromString(propUri);
				if(localName == null)
					continue;
				//TODO: if only numbers, DON't INDEX
				String[] splits = localName.split(Constants.PUNCTUATION_FOR_SPLITS);

				for(String word : splits) {		
					//also split camelCase
					String[] camelCaseSplit = word.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
					for(String camelCaseSplittedWord : camelCaseSplit){
						if(!buf.toString().contains(camelCaseSplittedWord)) {
							buf.append(camelCaseSplittedWord);
							buf.append(" ");
						}
					}
				}
				if(!buf.toString().contains(localName)) {
					buf.append(localName);
				}
				captionMap.put(key, buf);
			}
			//object
			if (!object.isLiteral() && !object.isAnon()) {
				String objUri = object.as(Resource.class).getURI();
				className = classMap.get(objUri);
                                if(className == null){
                                        className = SPARQLUtilsRemote.getTypeOfResource(Constants.REMOTE_REPO, objUri);
                                        classMap.put(objUri, className);
                                }
				key =  objUri + "###" + className + "###" + "";
				if(objUri != null && (Constants.indexURIFragments 
						|| (!(labelDefinedForURI.contains(objUri))))) {
					StringBuffer buf = new StringBuffer();
					String localName = SPARQLUtilsRemote.getLiteralFromString(objUri);
					if(localName == null)
						continue;
					//TODO: if only numbers, DON't INDEX
					String[] splits = localName.split(Constants.PUNCTUATION_FOR_SPLITS);

					for(String word : splits) {
						if(!buf.toString().contains(word)) {	
							buf.append(word);
							buf.append(" ");
						}
					}
					if(!buf.toString().contains(localName)) {	
						buf.append(localName);
						buf.append(" ");
					}
					captionMap.put(key, buf);
				}
			}
		}
	}

	public Html5DagGenericNode toDag() {
		Html5DagGenericNode dag = null;
		if (edges.size() > 0) {
			DagEdge<String> edge = edges.iterator().next();
			dag = new Html5DagGenericNode(edge.getInput());
			addToDag(dag);
			dag.styleByPrefix("", Metadata.getStyleByType(type));
		}
		return dag;
	}

	private void addToLiteralMap(String uri, String literal) {
		Set<String> uris = literalMap.get(literal);
		if (uris == null) {
			uris = new HashSet<String>();
			literalMap.put(literal, uris);
		}
		uris.add(uri);
	}

	/**
	 * Add this model information (uris, edges, captions, additional text info)
	 * to the given DAG.
	 * 
	 * @param dag
	 */
	public void addToDag(Html5DagGenericNode dag) {
		StopWatch watch = new StopWatch("^");

		// add edges
		// - nodes can only be added through edges.
		for (DagEdge<String> edge : edges) {
			//logger.info("Added edge" +  edge.getName() + " edge "+ edge);
			dag.addEdge(edge.getName(), edge.getInput(), edge.getOutput());
		}

		logger.info("Added edges");

		// set captions
		// - captions can only be set after the nodes have been added.
		for (Map.Entry<String, StringBuffer> entry : captionMap.entrySet()) {
			Html5DagGenericNode node = dag.getByUniqueId(entry.getKey());
			if (node != null) {
				node.setCaption(entry.getValue().toString());
			}
		}

		logger.info("Added captions");

		// add parameters
		for (Map.Entry<String, List<Parameter>> entry : parameterMap.entrySet()) {
			Html5DagGenericNode node = dag.getByUniqueId(entry.getKey());
			if (node != null) {
				node.getParameters().addAll(entry.getValue());
			}
		}

		logger.info("Added paramMap");
		// report time
		watch.stopAndReport("Adding model to DAG");
	}
	
	/**
	 * Parse the given model from a remote SPARQL object.
	 * <p>
	 * This method loops over all *** datatype properties *** in the endpoint and extracts text
	 * values (captions, parameters), edges and uris.
	 * NOTE: if captions list specified, will only request those properties
	 */
	private void parseModelFromRemote(String endpoint, MetadataMapping mapping) {
		// 1. get all properties with literals in the endpoint
		String query = "select distinct ?prop where { ?x ?prop ?y . filter(isLiteral(?y)) } "; 
		
		ArrayList<String> propertiesToIndex = (ArrayList<String>) SPARQLUtilsRemote.execRemoteQuery(query, endpoint);
		
		System.out.println("Will index: "+ propertiesToIndex);

		// 2. iterate through all and associate literal with subject as done above

		// property names caption configured by user
		Set<String> pnsCaptionSet = mapping.getGeneralPropNamesCaptionSet();

		for(String propertyURI : propertiesToIndex) {
			
			// get only the fragment name, e.g. "label", "identifier", "name" etc - to be compliant with how these are configured in the metadata file
			String propName = SPARQLUtilsRemote.getLiteralFromString(propertyURI);
			//index literals as indicated by the mapping properties, e.g. label (or everything if the set is empty)
			if (pnsCaptionSet.size() == 0 || pnsCaptionSet.contains(propName.toLowerCase())) {
				// paginate! LIMIT + OFFSET
				int current_batch = 0;
				
				while (true) { // break when no new results available
					int offset = current_batch * Constants.SPARQL_INDEXING_BATCH_SIZE;
					int limit = Constants.SPARQL_INDEXING_BATCH_SIZE;
					
					HashSet<Pair<String, String>> results = SPARQLUtilsRemote.execQueryWithOffsetandLimit(null, propertyURI, null, offset, limit, endpoint);
					//if still has results, get results (subj, obj) and index them, else break
					if(results.size() == 0) {
						break;
					}
					
					current_batch += 1;
					for(Pair<String, String> subj_obj_pair : results) {
						
						String literal = subj_obj_pair.getRight();
						String subjectUri = subj_obj_pair.getLeft();
						
						//don't index numbers
						if(literal.matches("-?\\d+(\\.\\d+)?"))
							continue;
						
				
						//here also get CLASS of subject
						//add to map to avoid redundant queries
						String className = classMap.get(subjectUri);
						if(className == null){
							className = SPARQLUtilsRemote.getTypeOfResource(Constants.REMOTE_REPO, subjectUri);
							classMap.put(subjectUri, className);
						}
						// caption
						String captionMapKey = subjectUri + "###" + className + "###" + propertyURI;
			
						
						StringBuffer buf = captionMap.get(captionMapKey);
			
						if (buf == null) {
							buf = new StringBuffer();
							captionMap.put(captionMapKey, buf);
						} else {
							//logger.warn("two captions found on node " + subjectUri);
						}
						if (buf.length() > 0) {
							buf.append(" ");
						}
						
						String[] splits = literal.split(Constants.PUNCTUATION_FOR_SPLITS);
			
						for(String word : splits) {		
							if(!buf.toString().contains(word)) {
								buf.append(word);
								buf.append(" ");
							}
						}
			
						if(!buf.toString().contains(literal)){
							buf.append(literal);
							buf.append(" ");
						}
			
						captionMap.put(captionMapKey, buf);
				
					}
				}
			}
		}
		
		//iterate through all keys in the captionMap, camel case split and add as indexed
		if(Constants.indexURIFragments) {
			for(Map.Entry<String, StringBuffer> entry : captionMap.entrySet()) {
				String uriString = entry.getKey().split("###")[0];

				StringBuffer buf = captionMap.get(entry.getKey());
				if (buf == null) {
					buf = new StringBuffer();
					captionMap.put(entry.getKey(), buf);
				}
				String localName = SPARQLUtilsRemote.getLiteralFromString(uriString);
				//TODO: if only numbers, DON't INDEX
				if(localName == null)
					continue;
				String[] splits = localName.split(Constants.PUNCTUATION_FOR_SPLITS);

				for(String word : splits) {	
					//also split camelCase
					String[] camelCaseSplit = word.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
					for(String camelCaseSplittedWord : camelCaseSplit){	
						if(!buf.toString().contains(camelCaseSplittedWord)) {
							buf.append(camelCaseSplittedWord);
							buf.append(" ");
						}
					}
				}

				if(!buf.toString().contains(localName)) {
					buf.append(" ");
					buf.append(localName);
				}
			}
		}

		//TODO: should we still do a final pass through uris that do not have anything indexed (in caption map) yet??
	}

}
