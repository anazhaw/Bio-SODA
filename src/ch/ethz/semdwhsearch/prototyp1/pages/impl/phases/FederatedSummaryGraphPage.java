package ch.ethz.semdwhsearch.prototyp1.pages.impl.phases;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Model;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.SpanningTree;
import org.jgrapht.alg.shortestpath.KShortestSimplePaths;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.html5.Canvas;
import ch.ethz.html5.Context2D;
import ch.ethz.html5.dag.DagState;
import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.rdf.dag.RdfDagNode;
import ch.ethz.semdwhsearch.prototyp1.actions.Params;
import ch.ethz.semdwhsearch.prototyp1.algorithms.queryclassification.LongestMatchClassify;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.SPARQLResultElement;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.AsyncDisplayOfSparqlQueries;
import ch.ethz.semdwhsearch.prototyp1.algorithms2.lookup.OperatorsParsing;
import ch.ethz.semdwhsearch.prototyp1.classification.Classification;
import ch.ethz.semdwhsearch.prototyp1.classification.ClassificationSingleton;
import ch.ethz.semdwhsearch.prototyp1.classification.Match;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.MatchType;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermOrigin;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.constants.PageNames;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.metadata.MetadataSingleton;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.FormElement;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.QueryClassificationElement;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.QueryElement;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.SubTitleElement;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.impl.TitleElement;
import ch.ethz.semdwhsearch.prototyp1.pages.impl.AbstractPage;
import ch.ethz.semdwhsearch.prototyp1.querygraph.BusinessObject;
import ch.ethz.semdwhsearch.prototyp1.querygraph.QueryGraph;
import ch.ethz.semdwhsearch.prototyp1.tools.Dates;
import ch.ethz.semdwhsearch.prototyp1.tools.ErrorTools;
import ch.ethz.semdwhsearch.prototyp1.tools.Escape;
import ch.ethz.semdwhsearch.prototyp1.tools.request.PostRequest;
import ch.zhaw.biosoda.FederatedSummaryGraph;
import ch.zhaw.biosoda.GraphPathSet;
import ch.zhaw.biosoda.SPARQLUtilsLocal;
import ch.zhaw.biosoda.SPARQLUtilsRemote;
import ch.zhaw.biosoda.SummaryEdge;
import ch.zhaw.biosoda.SummaryVertex;
import ch.zhaw.biosoda.VertexType;
import ch.zhaw.nlp.NlpPipeline;
import ch.zhaw.nlp.TokenList;

/**
 * Main Bio-SODA page, implements Steiner tree approximation
 * 
 * @author Ana Sima
 * 
 */
public class FederatedSummaryGraphPage extends AbstractPage {

	private static final String NAME = PageNames.PN_SGRAPH_FEDERATED;

	HashMap<String, String> classMap = new HashMap<String, String>();
	
	private final static Logger logger =
			LoggerFactory.getLogger(LongestMatchClassify.class);

	public String getInitJs() {
		return "document.getElementById(\"biosoda\").focus();";
	}

	public String getHtml(HttpServletRequest req, Dictionary dict) {
		String qStr = null;
		if (req.getMethod().equals("GET")) {
			qStr = req.getParameter(Params.Q);
		} else if (req.getMethod().equals("POST")) {
			PostRequest postReq = new PostRequest();
			try {
				postReq.parse(req, null, false);
			} catch (Exception e) {
				return ErrorTools.toDivWarn("Could not parse post request!");
			}
			qStr = postReq.getFormField(Params.Q);
		}

		StringBuffer html = new StringBuffer();
		new TitleElement("Summary Graph").appendHtml(html, dict);
		// form
		FormElement form = new FormElement(FederatedSummaryGraphPage.NAME);
		form.appendHtml(html, dict);

		// query
		new QueryElement(qStr).appendHtml(html, dict);

		// close form
		form.appendHtmlClose(html, dict);

		// use the SummaryRDFGraph class to construct the graph and show it in the page

		List<String> uris = new ArrayList<String>();
		uris.add(Constants.REMOTE_REPO);
		FederatedSummaryGraph g = null;

		if (g == null) {
			try {
				//get from checkpoint location (if graph is available there)
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(MetadataSingleton.getInstance().getMetadata().getDataDirPath() + "summarygraph.txt"));
				g = (FederatedSummaryGraph) in.readObject();
				in.close();
			}
			catch(Exception e){
				e.printStackTrace();
				//otherwise, reconstruct it and store it
				g = new FederatedSummaryGraph(uris);
				try {
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(MetadataSingleton.getInstance().getMetadata().getDataDirPath() + "summarygraph.txt"));
					out.writeObject(g);
					out.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		// we DON't want this in the summary graph!!
		g.getSummaryGraph().removeVertex("<http://www.w3.org/2000/01/rdf-schema#Class>");

		MetadataSingleton.getInstance().getMetadata().getModelInfo(Constants.MODEL_NAME).setFederatedSummaryGraph(g);

		Html5DagGenericNode node = null;

		for(SummaryEdge e : g.getSummaryGraph().edgeSet()){
			if(node == null){
				node = new Html5DagGenericNode(e.getSrc());
			}

			String src = e.getSrc();
			String dst =  e.getDest();
			if(!src.contains("Property") && !dst.contains("Property"))
				node.addEdge(src,  dst);

			//TODO: show a different color according to prefix, since multiple classes might have same name
			// e.g. http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugs and http://www4.wiwiss.fu-berlin.de/sider/resource/sider/drugs
			Html5DagGenericNode pkTypeSrc = node.getByUniqueId(src);
			if (pkTypeSrc != null) {
				pkTypeSrc.setStyle(Metadata.STYLE_LS);
				pkTypeSrc.setType(10);
			}
			Html5DagGenericNode pkTypeDest = node.getByUniqueId(dst);
			if (pkTypeDest != null) {
				pkTypeDest.setStyle(Metadata.STYLE_LS);
				pkTypeDest.setType(10);
			}
		}

		if(node == null){
			node = new Html5DagGenericNode("Thing");
		}


		// this will contain all the steiner Trees to display
		// for now, let the user choose (display sorted in order of weight)
		TreeMap<Double, List<RdfDagNode>> toDisplayTrees = new TreeMap<Double, List<RdfDagNode>>();

		/* parse the result to augment the graph */
		Classification classification = ClassificationSingleton.getInstance().getClassification();
		Metadata metadata = MetadataSingleton.getInstance().getMetadata();

		NlpPipeline nlpPipeline = classification.getNlpPipeline();

		OperatorsParsing lookup = new OperatorsParsing(classification, metadata);

		if (qStr != null) {
			// query log
			String date = Dates.timeFormatterSQL.format(new Date());
			String ip = req.getRemoteAddr();
			logger.info("query (lucky page): " + qStr + " (" + date + " from " + ip + ")");

			qStr = qStr.trim();

			// subtitle - input
			new SubTitleElement("Keyword Query: "+ qStr).appendHtml(html, dict);

			TokenList tokens = nlpPipeline.annotate(qStr);

			logger.info("Lookup for candidate matches...");
			List<RdfDagNode> lookupResult = lookup.lookup(tokens);
			logger.info("Done Index Lookup for candidate matches..." + Dates.timeFormatterSQL.format(new Date()));


			//TODO: fix this
			Model m = metadata.getModelInfo(Constants.MODEL_NAME).getModel();
			String endpoint = Constants.REMOTE_REPO;

			String ctxPath = req.getContextPath();

			// PUT HERE LIST OF TERMS INSTEAD OF string, string
			LinkedHashMap<String, HashSet<Term>> matches = new LinkedHashMap<String, HashSet<Term>>();
			LinkedList<Match> matchesList = new LinkedList<Match>();

			// ALSO TODO: group terms per filter and return matches like that!

			HashSet<String> negations = new HashSet<String>();
			HashMap<String, Integer> filters = new HashMap<String, Integer>();
			HashSet<String> filteredKws = new HashSet<String>();

			LinkedList<HashSet<Term>> termsList = new LinkedList<HashSet<Term>>();
			// GET LIST OF MATCHES per keyword
			// group terms together for each KW into class, prop, kw

			HashMap<Term, String> labelForTerm = new HashMap<Term,String>();			
			HashMap<String, String> originalSearchTerm = new HashMap<String, String>();	
			/** HERE, iterate through lookupresults and ALSO CONSTRUCT THE TERM groups
			 * e.g. <Disease, rdfs:label, "stroke"> => filter on label for disease class based on stroke keyword
			 *      <side_effect, rdfs:label, "arthrosis">  etc
			 */
			for (RdfDagNode dag : lookupResult) {
				QueryGraph qg = QueryGraph.parse(dag);

				ArrayList<Term> terms = new ArrayList<Term>();

				// Term is equal if has same class property and keyword filtered (see equals definition)

				//list of terms per KW here!!!
				//HashMap<String, Term> termsForKw = new HashMap<String, Term>();
				// if same class and property that matched, keep only 1 + filter

				// if query conriunknowns and if they are URIs, get the info on the URIs

				for (BusinessObject bo : qg.getBusinessObjects()) {
					String kw = nlpPipeline.annotate(bo.key).getLemmatizedText();
					originalSearchTerm.put(kw, bo.key);
					String uri = bo.srcLink;
					String resType = bo.className == null? "": bo.className;
					String propThatMatched = ((bo.propName == null) || (bo.propName.equals("null"))) ? "": bo.propName;
					String found = "uri";
					String label = "unknown";
					//TODO: HERE MAKE SURE TO GET THE VALUE OF LABEL THAT CORRESPONDS TO bo.key
					if(!propThatMatched.isEmpty()){
						found =  SPARQLUtilsLocal.getLiteralFromString(propThatMatched);
						// construct query to get the label that matched: could be 
						// a) the original text that the user searched: e.g. "big data"
						// b) the lemmatized version of the text: e.g. "coordinator" (but not "big datum")
						// c) for multi-word tokens, it might be a "_" union of the words => construct a UNION of like for each word

						String[] composingWords = bo.key.split(" ");
						String addedFilter = "";
						if(composingWords.length > 1) {
							addedFilter = "UNION { "; 
							for(String word: composingWords){
								addedFilter += " FILTER(contains(lcase(str(?label)), \"" + word.toLowerCase()  +  "\"))";
							}
							addedFilter +=  "} ";
						} 
						String queryLabel = "SELECT ?label where { " + "<" + uri + ">"+ " "+ propThatMatched + " " + " ?label. " + 
								"{ { FILTER(contains(lcase(str(?label)), \"" + bo.key.toLowerCase() + "\"))} UNION { FILTER(contains(lcase(str(?label)), \"" + kw.toLowerCase() + "\"))} " + addedFilter + " }}";
						List<String> labels = SPARQLUtilsRemote.getLabelRemote(queryLabel, Constants.REMOTE_REPO);
						if(labels != null && labels.size() != 0) {
							label = labels.get(0);
							if(label.toLowerCase().contains(kw.toLowerCase()) && !label.toLowerCase().contains(bo.key.toLowerCase()))
								originalSearchTerm.put(kw, kw);
						}
					}

					else {
						label = SPARQLUtilsRemote.getLiteralFromString(uri);
					}

					//THESE ARE THE URI MATCHES, TODO: decide if we use them
					//if(found.equals("uri") &&(!resType.contains("Property")) &&(!resType.contains("#Class")))
					//	continue;

					//String label = ""; //bo.value;
					if(bo.negated)
						negations.add(bo.srcLink);

					//HERE ALSO GET PAGE RANK FROM REMOTE 
					double pageRank = bo.pageRank;

					Term term = new Term(TermType.RDF_MATCH, kw, uri, TermOrigin.SPARQL, SPARQLUtilsLocal.getLiteralFromString(resType) + ": "+ found + " (\""+ label + "\")", pageRank);
					term.label = label;
					if(bo.operator != null && !(bo.operator.isEmpty()))
						try {
							term.setOperator(bo.operator);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					term.setClassProp(resType, propThatMatched);
					if(resType != null && (!resType.contains("#Class")) && (!resType.contains("http://www.w3.org/2002/07/owl")) &&(!resType.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns")) && (propThatMatched != null) && (!propThatMatched.isEmpty()) && (!found.equals("uri")) && (!propThatMatched.contains("#equivalentClass"))) {
						term.setFilter();
					}

					else{
						term.setExactMatch();
					}

					terms.add(term);

					HashSet<Term> matchesForKW = matches.get(kw);
					if(matchesForKW == null) {
						matchesForKW = new HashSet<Term>();
					}
					matchesForKW.add(term);
					matches.put(kw, matchesForKW);

					// here need LIST of terms for each KW
					// then ONE MATCH per list of terms per kw
					//termsForKw.put(kw, term);
					if(propThatMatched.isEmpty()&& (!resType.equals(""))){
						filters.put(resType + "###" + SPARQLUtilsRemote.getLiteralFromString(uri) + "###" + kw, 1);
					}
					if( (!propThatMatched.isEmpty()) && (!resType.equals(""))) { 
						if(resType.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns") || (resType.contains("http://www.w3.org/2002/07/owl") && (!resType.equals("http://www.w3.org/2002/07/owl#AnnotationProperty"))))
							continue;
						Integer prevCount = filters.get(resType + "###" + propThatMatched+ "###"+ kw);
						if(prevCount == null){
							if(resType.contains("http://www.w3.org/2002/07/owl#AnnotationProperty")){
								//make sure these are always used as filters
								prevCount = 1; 
							}	
							else
								prevCount = 0;
						}
						filters.put(resType + "###" + propThatMatched + "###" + kw, prevCount + 1);
						if((!resType.contains("http://www.w3.org/2002/07/owl#AnnotationProperty")) && prevCount > 0) {
							filteredKws.add(kw);
						}
					}
				}
			}

			//remove numerical filters from terms first
			List<Term> numericalTerms = new LinkedList<Term>();

			HashMap<String, Match> numerical = new HashMap<String, Match>();
			for(String keyword: matches.keySet()) {
				HashSet<Term> termsSet = new HashSet<Term>();
				// TODO: here, when multiple terms for a match have SAME class + property => 
				// COLLECT FILTER in sparql
				for(Term term: matches.get(keyword)) {
					if(term.key.matches("-?\\d+(\\.\\d+)?") && term.filteredClass.contains("Property")){
						term.label = term.key;
						numericalTerms.add(term);
						Match mat = numerical.get(term.key);
						if(mat == null)
							mat = new Match(term);
						else
							mat.addTerm(term);
						numerical.put(term.key, mat);
					}
					else {
						term.key = originalSearchTerm.get(keyword);
						termsSet.add(term);
					}
				}
				if(termsSet.size() > 0){
					Match match = new Match(new LinkedList<Term>(termsSet));
					match.sortByBestMatch();
					matchesList.add(match);
					logger.info("ADDING MATCH " + match + " with terms " + match.getTerms());

					// add the matches for this keyword to the termslist
					termsList.add(termsSet);
				}
			}

			// subtitle - output
			new SubTitleElement("Selected Matches (one example per class-property pair, limited to top " + Constants.MAX_MATCHES_COUNT + "):").appendHtml(html, dict);

			//BUILD MATCHES LIST
			for(Match numericalTermMatch : numerical.values()) {
				// add numerical matches for displaying 
				matchesList.add(numericalTermMatch);
			}

			new QueryClassificationElement(matchesList, qStr, NAME, ctxPath).appendHtml(html, dict);

			TreeMap<String, String> sparqlStmtsMap = new TreeMap<String, String>();
			if(matchesList.size() == 1) {
				int currentAnswer = 1;
				HashMap<String, String> labelsForClasses = SPARQLUtilsRemote.getLabelsForClasses(g);
				HashSet<String> alreadyDisplayed = new HashSet<String>();
				for(Term t : matchesList.get(0).getTerms()){
					if(alreadyDisplayed.contains(t.value))
						continue;
					// only 1 concept searched, return all info on that
					if(t.filteredClass != null) {
						html.append("<div class='clearer'>&#160;</div>\n");
						html.append("<ul>");
						html.append("<li>");
						html.append("RESULT "+ currentAnswer + ": "+ t.value + "<br>");
						String describe_query = "";
						if(t.filteredClass.contains("#Class")) {
							String varName = SPARQLUtilsRemote.getLiteralFromString(t.value).toLowerCase();
							String labelProperty = labelsForClasses.get(varName);
							String description = "";
							varName = "?" + varName;
							if(Constants.ADD_DESCRIPTIONS && labelProperty != null){
								String labelPropertyVarName = varName + "_"+ SPARQLUtilsRemote.getLiteralFromString(labelProperty).toLowerCase();
								description = varName + " "+ labelProperty + " " + labelPropertyVarName;
							}
							describe_query = "SELECT * where { "+ varName + " a <"+ t.value +">. "+ description + " } LIMIT " + Constants.MAX_SPARQL_RESULTS_COUNT;

						} else if(t.filteredClass.contains("Property")){
							describe_query = "SELECT * where { ?subject <"+ t.value + "> ?value } LIMIT " + Constants.MAX_SPARQL_RESULTS_COUNT; 
						} 
						else {
							describe_query = "SELECT * where { <" + t.value  +"> ?property ?value. } LIMIT " + Constants.MAX_SPARQL_RESULTS_COUNT;
						}
						alreadyDisplayed.add(t.value);
						html.append(Escape.safeXml("SPARQL query: " + describe_query) + "<br>");
						html.append("<br>");
						html.append("<br>");
						html.append("</li>");
						html.append("</ul>");
						html.append("");
						String resultDivId = currentAnswer + "_resultdiv";
						new SPARQLResultElement(currentAnswer).appendHtml(html, dict);
						currentAnswer ++;
						sparqlStmtsMap.put(resultDivId, describe_query);

					}}
				new AsyncDisplayOfSparqlQueries(sparqlStmtsMap, ctxPath).appendHtml(html, dict);
				return html.toString();
			}
			logger.info("BEGIN time: "+ new Timestamp(System.currentTimeMillis()));

			/** construct term combinations here **/
			// based on terms per keyword, make a cartesian product

			//logger.info("Computing cartesian from : "+ termsList);
			Set<List<Term>> termCombinations = Sets.cartesianProduct(termsList);
			//logger.info("ALL TERM COMBINATIONS ARE: "+  termCombinations);

			// TODO: also make sure to SORT by the page ranks again per term, so that first list has ALL highest ranked terms
			// <term1 with highest page rank, term2 with highest> ....<term N with (highest - 1)>
			// (basically because the cartesian product is not order preserving)

			HashMap<String, HashSet<String>> domainsMap = new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> rangesMap = new HashMap<String, HashSet<String>>();

			//sort the termcombinations by sum of page rank
			// comparator for ranking query graphs. Here, order by shortest length (i.e. smallest weight of steiner tree)
			class TermCombinationComparator implements Comparator<List<Term>> {

				public TermCombinationComparator(){
				}

				//TODO: add here COMPARE BY INFORMATION SCORE OF TABLES!
				@Override
				public int compare(List<Term> termsList1, List<Term> termsList2) {
					Double pageRank1 = 0.0;
					for(Term term: termsList1) {
						//a perfect match should score higher
						double factor = term.key.length()/(double)term.label.length();
						pageRank1 += factor * term.pageRank;
					}

					Double pageRank2 = 0.0;
					for(Term term: termsList2) {
						double factor = term.key.length()/(double)term.label.length();
						pageRank2 += factor * term.pageRank;
					}
					if(pageRank1 > pageRank2) {
						return -1;
					}
					else if(pageRank1 < pageRank2) {
						return 1;
					}

					return 0;
				}
			}

			LinkedList<List<Term>> termCombinationsSorted = new LinkedList<List<Term>>(termCombinations);
			Collections.sort(termCombinationsSorted, new TermCombinationComparator());	
			// looking for domains and ranges of properties identified in the lookup (required to attach properties to the summary graph)

			//TODO: instead of lookupResult, iterate through term combinations
			int iteration = 0;			
			for (List<Term> alternativeMatch : termCombinationsSorted) {
				logger.info("AT ITERATION " + iteration + " looking at " + alternativeMatch + "\n\n");
				iteration++;
				if(iteration > Constants.MAX_TERM_COMBINATIONS)
					break;
				String subj = null, prop = null, obj = null;
				// loop over BOs
				boolean isObjectProperty = false;
				for (Term term : alternativeMatch) {

					String resType = term.filteredClass;
					if(resType == null)
						if(Constants.USE_REMOTE) {
							resType = SPARQLUtilsRemote.getTypeOfResource(endpoint, term.value);	
							classMap.put(term.value, resType);
						}
						else {
							resType = SPARQLUtilsLocal.getTypeOfResource(m, term.value);	
						}
					//logger.info("Type of "+ term.value + " is " + resType);
					//here avoid union nodes and so on
					if (resType == null || !resType.contains("http")) {
						continue;
					} else if (resType.contains("http://www.w3.org/2002/07/owl#DatatypeProperty") 
							|| resType.contains("http://www.w3.org/2002/07/owl#SymmetricProperty")
							|| resType.contains("http://www.w3.org/2002/07/owl#FunctionalProperty")
							|| resType.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property")
							|| resType.contains("http://www.w3.org/2002/07/owl#AnnotationProperty")){
						prop = term.value;
					} else if (resType.contains("http://www.w3.org/2002/07/owl#ObjectProperty")){
						prop = term.value;
						isObjectProperty = true;
					}
					else {
						subj = term.value;
					}

					if(resType != null){
						if (isObjectProperty) {
							obj = subj;
							subj = null;
						}
						if (subj != null || obj != null) {
							String toSearch = subj == null? obj:subj;
							String toSearchType = classMap.get(toSearch);
							if(toSearchType == null)
								if(Constants.USE_REMOTE) {
									toSearchType = SPARQLUtilsRemote.getTypeOfResource(endpoint, toSearch);	
									classMap.put(term.value, resType);
								}
								else {
									toSearchType = SPARQLUtilsLocal.getTypeOfResource(m, toSearch);	
								}
							String className = toSearchType;
							toSearch = "<"+toSearch+">";

							//we don't want to add the classes as belonging to the generic "Class" type
							if(!className.contains("#Class")){
								Graph< String, SummaryEdge> orig = g.getSummaryGraph();
								orig.addVertex(toSearch);
								orig.addVertex(className);
								HashSet<String> abc = new HashSet<String>();
								abc.add("a");
								SummaryEdge edge = new SummaryEdge(new SummaryVertex(toSearch, VertexType.LITERAL),
										new SummaryVertex(className, VertexType.CLASS), abc);
								if(!orig.edgeSet().contains(edge)) {
									orig.addEdge(toSearch, className, edge);
								}

								g.setSummaryGraph(orig);

								className = className.substring(className.indexOf("#") + 1).replaceAll("[<>\\]]", "");
								toSearch = toSearch.substring(toSearch.indexOf("#") + 1).replaceAll("[<>\\]]", "");

								node.addEdge(className, toSearch);
								Html5DagGenericNode pkTypeSrc = node.getByUniqueId(toSearch);
								if (pkTypeSrc != null) {
									pkTypeSrc.setStyle(Metadata.STYLE_MATCH);
									pkTypeSrc.setType(10);
								}
								Html5DagGenericNode pkTypeDst = node.getByUniqueId(className);
								if (pkTypeDst != null) {
									pkTypeDst.setStyle(Metadata.STYLE_SELECTED);
									pkTypeDst.setType(10);
								}
							}
						}
						// here, if we identified a property as a match, we get all of its possible domains and ranges
						// such that these constitute possible "entry points" in the minimum subgraph
						//e.g. since the "isExpressedIn" property is not a node in the graph, in order to represent it,
						// we use its domain - a Gene, as well as its possible range classes (with 1 alternative tree per each)
						if (prop != null){
							if(!prop.startsWith("<")){
								prop = "<"+ prop+">";
							}

							//logger.info("SEARCHING FOR DOMAIN OF "+ prop);

							HashSet<String> domains = domainsMap.get(prop);
							if(domains == null) {
								domains = SPARQLUtilsRemote.getDomainOfPropertyRemote(prop, endpoint);
								domainsMap.put(prop, domains);
							}
							//TODO: if there is no domain, add text filter on this prop and create multiple SPARQL queries
							// where the property + filter is attached to different classes
							if(domains.size() == 0) {
								logger.info("No domain defined - assume this property can apply to any class;");
								//TODO
							}

							for(String className: domains) {
								if (className != null) {
									//logger.info("DOMAIN IS "+ className);
									//TODO: must be careful to check the origins of properties somehow...
									// would be a nice optimization (at least check they come from same model)
									Graph< String, SummaryEdge> orig = g.getSummaryGraph();

									orig.addVertex(prop);
									orig.addVertex(className);
									HashSet<String> abc = new HashSet<String>();
									abc.add("domainOf");

									SummaryEdge edge = new SummaryEdge(new SummaryVertex(className, VertexType.CLASS),
											new SummaryVertex(prop, VertexType.PROPERTY),
											abc);
									//TODO: INSTEAD OF "DOMAINOF", MATCH EACH DOMAIN, RANGE PAIR!
									if(!orig.edgeSet().contains(edge)) {
										orig.addEdge(prop, className,edge);
									}

									g.setSummaryGraph(orig);

									className = className.substring(className.indexOf("#") + 1).replaceAll("[<>\\]]", "");
									String shortprop = prop.substring(prop.indexOf("#") + 1).replaceAll("[<>\\]]", "");

									node.addEdge(className, shortprop);
									Html5DagGenericNode pkTypeSrc = node.getByUniqueId(shortprop);
									if (pkTypeSrc != null) {
										pkTypeSrc.setStyle(Metadata.STYLE_MATCH);
										pkTypeSrc.setType(10);
									}
									Html5DagGenericNode pkTypeDst = node.getByUniqueId(className);
									if (pkTypeDst != null) {
										pkTypeDst.setStyle(Metadata.STYLE_SELECTED);
										pkTypeDst.setType(10);
									}
								}
							}
							//logger.info("SEARCHING FOR RANGE OF "+ prop);

							HashSet<String> ranges = rangesMap.get(prop);
							if(ranges == null) {
								ranges = SPARQLUtilsRemote.getRangeOfPropertyRemote(prop, endpoint);
								// assumption: if range null, then it might be a string property
								if(ranges == null || ranges.size() == 0) {
									ranges.add("<http://www.w3.org/2001/XMLSchema#string>");
									//here, all numerical terms that contains this property should get the domain of this as class
								}
								rangesMap.put(prop, ranges);
							}
							//add properties to hashset, this also removes duplicates
							//logger.info("Getting most specific class from "+ res4 + " size "+ res4.size());
							//className = g.getMostSpecificClass(res4);
							// TODO: add properties from data structure (subClassOf), object properties
							for(String className: ranges)
								if (className != null) {
									Graph< String, SummaryEdge> orig = g.getSummaryGraph();
									orig.addVertex(prop);
									orig.addVertex(className);
									HashSet<String> abc = new HashSet<String>();
									abc.add("range");
									SummaryEdge edge = new SummaryEdge(new SummaryVertex(prop, VertexType.PROPERTY),
											new SummaryVertex(className, VertexType.CLASS),
											abc);
									if(!orig.edgeSet().contains(edge)) {
										orig.addEdge(prop, className, edge);
									}

									g.setSummaryGraph(orig);

									className = className.substring(className.indexOf("#") + 1).replaceAll("[<>\\]]", "");
									String shortProp = prop.substring(prop.indexOf("#") + 1).replaceAll("[<>\\]]", "");

									node.addEdge(className, shortProp);
									Html5DagGenericNode pkTypeSrc = node.getByUniqueId(shortProp);
									if (pkTypeSrc != null) {
										pkTypeSrc.setStyle(Metadata.STYLE_MATCH);
										pkTypeSrc.setType(10);
									}

									Html5DagGenericNode pkTypeDst = node.getByUniqueId(className);
									if (pkTypeDst != null) {
										pkTypeDst.setStyle(Metadata.STYLE_SELECTED);
										pkTypeDst.setType(10);
									}
								}
						}

					}
				}
			}


			// compute shortest paths between matched nodes
			// collect all vertices from the shortest paths in a new [sub]graph


			/**
			 * 1. compute the vertices to be included in the Steiner Tree
			 */
			double minWeight = Integer.MAX_VALUE;
			SpanningTree<SummaryEdge> minTree = null;
			//this maps steiner trees to the term combinations that generate them (so that we can display resuls after sorting etc)
			//note: multiple possible Term combinations can lead to the same tree, we want to save them in a list (of term combinations) 
			//in order to rank/select the optimal one at the end
			HashMap<Graph<String, SummaryEdge>, HashSet<List<Term>>> trees = new HashMap<Graph<String, SummaryEdge>, HashSet<List<Term>>>();
			HashMap<String, HashSet<GraphPath<String, SummaryEdge>>> pathsCache = new HashMap<>();

			HashMap<String, List<GraphPath<String, SummaryEdge>>> shortestPathsCache = new HashMap<>();

			// TODO: select the OVERALL MIN TREE from all results
			// LIMIT SEARCH SPACE (get answer within 1 minute max)

			//int lookedUp = 0;
			for (List<Term> alternativeMatch : termCombinationsSorted) {
				HashSet<String> sparqlFilters = new HashSet<String>();

				for(Term term : alternativeMatch) {
					//filter on terms that have string filters defined from index OR on numerical matches
					if(term.matchType == MatchType.FILTER){
						if(term.filteredClass.contains("Class") || term.filteredClass.contains("http://www.w3.org/2002/07/owl#AnnotationProperty")) 
							continue;
						String[] splits = term.label.toLowerCase().split(Constants.PUNCTUATION_FOR_SPLITS);
						if(splits.length > 1) {
							sparqlFilters.add(term.filteredClass + "###" + term.filteredProp + "###" +  term.label.toLowerCase());
						}    
						else {
							sparqlFilters.add(term.filteredClass + "###" + term.filteredProp + "###" + term.key);
						}
					}
					for(Term numericalTerm : numericalTerms) {
						if(numericalTerm.value.equals(term.value)) {
							sparqlFilters.add("NUMERICAL" + "###" + numericalTerm.value + "###" + numericalTerm.operator + "###" + numericalTerm.key);
						}
					}

				}
				if(trees.size() > Constants.MAX_RESULT_COUNT) {
					logger.info("REACHED MAX RESULT COUNT, WILL SKIP REST OF LOOKUP RESULTS "+ Constants.MAX_RESULT_COUNT);
					break;
				}
				// each lookup result (dag) contains an alternative solution (matches that cover all keywords)
				// create one summary graph for each 
				Graph<String, SummaryEdge> steinerTree = new SimpleGraph<String, SummaryEdge>(SummaryEdge.class);

				String resType = "";
				boolean skip = false;

				if (!skip) {
					for (Term term : alternativeMatch) {

						resType = classMap.get(term.value);
						if(resType == null)
							if(Constants.USE_REMOTE) {
								resType = SPARQLUtilsRemote.getTypeOfResource(endpoint, term.value);
								if(resType == null) {
									resType = "null";
								}			
								classMap.put(term.value, resType);
							}
							else {
								resType = SPARQLUtilsLocal.getTypeOfResource(m, term.value);
							}

						//TODO: we should still add it if it's a literal
						if (resType.equals("null") || !(resType.contains("http"))) {
							logger.info("The type of " + term.value + " is null, skipping...");
							continue;
						} 

						else {
							String toAdd = term.value;
							steinerTree.addVertex("<"+toAdd+ ">");
						}
					}

					/**
					 * 2. compute shortest paths between all vertices in the original graph, 
					 * add any new vertices collected from the paths
					 */
					HashSet<SummaryEdge> toAdd = new HashSet<>();
					//some shortest paths have multiple alternatives with the same cost, we need to build trees 
					//from every option, otherwise we might miss the minimal (total) cost spanning tree
					HashMap<Integer, HashSet<GraphPath<String, SummaryEdge>>> alternatives = new HashMap<Integer, HashSet<GraphPath<String, SummaryEdge>>>();
					int alternativeGroup = 0;


					//TODO: HERE NEED TO CHANGE SUMMARY GRAPH FIRST, CREATE DOMAIN RANGE PAIR RATHER THAN EXPLICIT
					//go through all edges in the graph
					HashSet<SummaryEdge> toAddEdges = new HashSet<SummaryEdge>();
					HashSet<SummaryEdge> toRemovEdges = new HashSet<SummaryEdge>();
					for(SummaryEdge edge : g.getSummaryGraph().edgeSet()) {
						if(edge.getLabels().contains("domainOf")) {
							// find matching range based on property name
							for(SummaryEdge edge2 : g.getSummaryGraph().edgeSet()) {
								//find matching property

								if(edge2.getLabels().contains("range") && edge.getDest().equals(edge2.getSrc()) && (!edge.getSrc().equals(edge2.getDest()))) {
									// add edge between edge.getSrc and edge2.getDst with label edge.getDest
									HashSet<String> props = new HashSet<String>();
									props.add(edge.getDest());
									logger.info("Adding edge based on property domain range " + edge.getSrc() + " " + edge2.getDest());
									toAddEdges.add(new SummaryEdge(new SummaryVertex(edge.getSrc(), VertexType.CLASS),
											new SummaryVertex(edge2.getDest(), VertexType.CLASS), props));

									toRemovEdges.add(edge);
									toRemovEdges.add(edge2);
								}
							}
						}
					}

					for(SummaryEdge edge : toAddEdges) {
						if(!edge.getSrc().toString().equals(edge.getDest().toString()) && !g.getSummaryGraph().edgeSet().contains(edge)){
							g.getSummaryGraph().addEdge(edge.getSrc(), edge.getDest(), edge);
						}
					}

					//Finally, remove all "range" and "domainOf" properties
					for(SummaryEdge edge : toRemovEdges) {
						g.getSummaryGraph().removeEdge(edge);
					}

					HashSet<String> toRemovePropertyVertices = new HashSet<String>();
					HashSet<String> visited = new HashSet<String>();
					boolean skipTree = false;
					//TODO: MARK options that were already visited (DON't visit same pair twice)
					for(String vertex: steinerTree.vertexSet()){
						HashSet<GraphPath<String, SummaryEdge>> paths = new HashSet<GraphPath<String, SummaryEdge>>();
						String resTypeVertex = classMap.get(vertex);
						if(resTypeVertex == null)
							if(Constants.USE_REMOTE) {
								resTypeVertex = SPARQLUtilsRemote.getTypeOfResource(endpoint, vertex);
								classMap.put(vertex, resTypeVertex);
							}
							else {
								resTypeVertex = SPARQLUtilsLocal.getTypeOfResource(m, vertex);
							}
						if (resTypeVertex.contains("Property")) {
							/*(resTypeVertex.contains("http://www.w3.org/2002/07/owl#DatatypeProperty") 
								|| resTypeVertex.contains("http://www.w3.org/2002/07/owl#SymmetricProperty")
								|| resTypeVertex.contains("http://www.w3.org/2002/07/owl#FunctionalProperty")
								|| resTypeVertex.contains("http://www.w3.org/2002/07/owl#ObjectProperty") 	
								|| resTypeVertex.contains("http://www.w3.org/2002/07/owl#AnnotationProperty")){
							 */
							HashSet<String> added = new HashSet<String>();
							for(SummaryEdge edge : g.getSummaryGraph().edgeSet()) {
								//TODO: create alternate paths for each case where "vertex" appears as label on edge
								if(edge.getLabels().contains(vertex) && !added.contains(edge.getSrc()+ edge.getDest())) {
									//put path in alternatives
									KShortestSimplePaths<String, SummaryEdge>		shortest = 
											new KShortestSimplePaths<String, SummaryEdge>(g.getSummaryGraph(), 1);

									//PROBLEM: NEED TO LINK THIS EDGE TO SOMETHING ELSE THAT EXISTS IN THE GRAPH! OTHERWISE WILL APPEAR AS A DISCONNECTED THING
									logger.info("Adding for prop " + vertex + " paths between " + edge.getSrc() + " and " + edge.getDest() + " based on label " + edge.getLabels());
									HashSet<GraphPath<String, SummaryEdge>> shortestPaths = new HashSet<GraphPath<String, SummaryEdge>>(shortest.getPaths(edge.getSrc(), edge.getDest(), Constants.MAX_SHORTEST_PATHS_ALTERNATIVES_COUNT));
									added.add(edge.getSrc() + edge.getDest());

									logger.info("ADDED PATH " + shortestPaths);

									int min_len = 0;
									for(GraphPath<String, SummaryEdge> gp: shortestPaths) {
										if(min_len == 0)
											min_len = gp.getLength();
										if(gp.getLength() > min_len)
											break;
										if(!gp.toString().contains(vertex))
											continue;
										if(!visited.contains(edge.getSrc() + edge.getDest())) {
											paths.add(gp);
											visited.add(edge.getSrc() + edge.getDest());
										}
									}
								}
							}

							//here need SINGLE alternative group per property
							if(alternatives.get(vertex.hashCode()) != null) {
								HashSet<GraphPath<String, SummaryEdge>> gps = alternatives.get(vertex.hashCode());
								paths.addAll(gps);
							}
							alternatives.put(vertex.hashCode(), paths);
							continue;
						}

						if(!skipTree)
							for(String otherVertex : steinerTree.vertexSet()){
								resTypeVertex = classMap.get(otherVertex);
								if(resTypeVertex == null)
									if(Constants.USE_REMOTE) {
										resTypeVertex = SPARQLUtilsRemote.getTypeOfResource(endpoint, otherVertex);
										classMap.put(otherVertex, resTypeVertex);
									}
									else {
										resTypeVertex = SPARQLUtilsLocal.getTypeOfResource(m, otherVertex);
									}

								if(resTypeVertex.contains("Property")){ 
									toRemovePropertyVertices.add(otherVertex);
									/*(resTypeVertex.contains("http://www.w3.org/2002/07/owl#DatatypeProperty") 
										|| resTypeVertex.contains("http://www.w3.org/2002/07/owl#SymmetricProperty")
										|| resTypeVertex.contains("http://www.w3.org/2002/07/owl#FunctionalProperty")
										|| resTypeVertex.contains("http://www.w3.org/2002/07/owl#ObjectProperty") 	
										|| resTypeVertex.contains("http://www.w3.org/2002/07/owl#AnnotationProperty")){

									 */
									HashSet<String> added = new HashSet<String>();
									for(SummaryEdge edge : g.getSummaryGraph().edgeSet()) {
										//TODO: create alternate paths for each case where "vertex" appears as label on edge
										if(edge.getLabels().contains(vertex)) {
											//put path in alternatives
											KShortestSimplePaths<String, SummaryEdge> shortest = 
													new KShortestSimplePaths<String, SummaryEdge>(g.getSummaryGraph(), 1);
											logger.info("Adding for prop " + vertex + " paths between " + edge.getSrc() + " and " + edge.getDest());
											HashSet<GraphPath<String, SummaryEdge>> shortestPaths = new HashSet<GraphPath<String, SummaryEdge>>(shortest.getPaths(edge.getSrc(), edge.getDest(), Constants.MAX_SHORTEST_PATHS_ALTERNATIVES_COUNT));
											added.add(edge.getSrc() + edge.getDest());
											logger.info("PATHS:" + shortestPaths);
											int min_len = 0;
											for(GraphPath<String, SummaryEdge> gp: shortestPaths) {
												if(min_len == 0)
													min_len = gp.getLength();
												if(gp.getLength() > min_len)
													break;
												if(!visited.contains(edge.getSrc() + edge.getDest())) {
													paths.add(gp);
													visited.add(edge.getSrc() + edge.getDest());
												}
											}
										}
									}

									//here need SINGLE alternative group per property
									if(alternatives.get(otherVertex.hashCode()) != null) {
										HashSet<GraphPath<String, SummaryEdge>> gps = alternatives.get(otherVertex.hashCode());
										paths.addAll(gps);
									}
									alternatives.put(otherVertex.hashCode(), paths);
									continue;
								}

								//if either vertex or otherVertex is actually a property name, then add all edges that contain it!

								if(!vertex.equals(otherVertex)){

									int minCost = Integer.MAX_VALUE;

									String class1 = classMap.get(vertex);
									if(class1.contains("#Class"))
										class1 = vertex;
									String class2 = classMap.get(otherVertex);
									if(class2.contains("#Class"))
										class2 = otherVertex;
									if(class1.equals(class2))
										continue;
									//if(class1.contains("Property") || class2.contains("Property"))
									//	continue;
									logger.info("SEARCHING FOR PATH BETWEEN "+ vertex + " and "+ otherVertex + " with classes " + class1  +" and " + class2);

									KShortestSimplePaths<String, SummaryEdge> shortest;
									HashSet<GraphPath<String, SummaryEdge>> iPaths;
									if(!pathsCache.containsKey(class1 + class2)) {
										// TODO: first ask for 1 path, to get the length of the shortest path
										// then ask for N paths, limiting the length to that
										shortest = 
												new KShortestSimplePaths<String, SummaryEdge>(g.getSummaryGraph(), Constants.MAX_HOPS_SHORTEST_PATH);

										if ((!g.getSummaryGraph().containsVertex(vertex) )|| (! g.getSummaryGraph().containsVertex(otherVertex))) {
											logger.info("Graph does not contain both "+ vertex + " or "+ otherVertex);
											boolean found = false;
											//IF otherVertex is actually an annotProp filter, attach it to vertex
											String filteredClass = null;
											for(Entry<String, Integer> entry: filters.entrySet()) {
												if(entry.getValue() > 1) {
													String cls = entry.getKey().split("###")[0];
													String propFilter = entry.getKey().split("###")[1];
													if(cls.contains("http://www.w3.org/2002/07/owl#AnnotationProperty")){
														if(propFilter.equals(otherVertex))
															filteredClass = vertex;
														else if(propFilter.equals(vertex))
															filteredClass = otherVertex;
														if(filteredClass == null)
															continue;
														if(!g.getSummaryGraph().containsVertex(filteredClass)) break;
														found = true;
														logger.info("ADDING LINK BETWEEN " + vertex + " and " + otherVertex);
														Graph< String, SummaryEdge> orig = g.getSummaryGraph();
														orig.addVertex(propFilter);
														orig.addVertex(filteredClass);
														HashSet<String> abc = new HashSet<String>();
														abc.add(propFilter);
														//is the filtered node a class or an instance?
														String vType = classMap.get(filteredClass);
														if(vType == null)
															if(Constants.USE_REMOTE) {
																vType = SPARQLUtilsRemote.getTypeOfResource(endpoint, filteredClass);	
																classMap.put(filteredClass, vType);
															}
															else {
																vType = SPARQLUtilsLocal.getTypeOfResource(m, filteredClass);
															}
														SummaryVertex vertexCls = null;
														if(vType.contains("#Class")) 
															vertexCls = new SummaryVertex(filteredClass, VertexType.CLASS);
														else
															vertexCls = new SummaryVertex(filteredClass, VertexType.LITERAL);
														orig.addEdge(filteredClass, propFilter,
																new SummaryEdge(vertexCls, 
																		new SummaryVertex(propFilter, VertexType.LITERAL),
																		abc));
														g.setSummaryGraph(orig);
													}	
												}
											}
											if(!found){							
												skipTree=true;
												continue;
											}
										}

										//here it is TOO EARLY TO COMPUTE THIS


										//if either vertex or otherVertex is actually a property name, then compute path from either end of it!
										boolean containsEdge = false;
										SummaryEdge toAddEdge = null;

										for(SummaryEdge s: g.getSummaryGraph().edgeSet()) {
											if(s.getEdge().contains(vertex)) {
												//change it to either one of the two ends
												//need to make sure that the path will contain the edge with the property too!
												if(!otherVertex.equals(s.getDest())) {
													containsEdge = true;
													vertex = s.getDest();
												}
												else {
													vertex = s.getSrc();
													containsEdge = false;
												}
												toAddEdge = s;
											}
											if(s.getEdge().contains(otherVertex)) {
												// change it here too
												if(!vertex.equals(s.getDest())){
													containsEdge = true;
													otherVertex = s.getDest();
												}
												else {
													otherVertex = s.getSrc();
													containsEdge = false;
												}
												toAddEdge = s;
											}
										}
										logger.info("Shortest Intermediate " + class1 + " " + class2);
										List<GraphPath<String, SummaryEdge>> shortestPathsIntermediate = shortest.getPaths(class1, class2, Constants.MAX_SHORTEST_PATHS_ALTERNATIVES_COUNT);

										List<GraphPath<String, SummaryEdge>> shortestPaths = null;										                                                                                        
										if(containsEdge)
											logger.info("ADDING EDGE " + toAddEdge + " " + vertex + " and " + otherVertex);

										if(containsEdge) { 
											shortestPaths = new LinkedList<GraphPath<String, SummaryEdge>>();
											for(GraphPath<String, SummaryEdge> gp: shortestPathsIntermediate) {
												List<String> vertexList = gp.getVertexList();
												if(!vertexList.contains(toAddEdge.getDest()))
													vertexList.add(toAddEdge.getDest());
												if(!vertexList.contains(toAddEdge.getSrc()))
													vertexList.add(toAddEdge.getSrc());
												List<SummaryEdge> edgeList = gp.getEdgeList();
												if(!edgeList.contains(toAddEdge))
													edgeList.add(toAddEdge);

												try {
													GraphPath<String, SummaryEdge> gpNew = new GraphWalk<String, SummaryEdge>(g.getSummaryGraph(), vertex, otherVertex, vertexList, edgeList, gp.getWeight());
													shortestPaths.add(gpNew);
												}
												catch (Exception e) {
													continue;
												}

											}
										}

										else {
											shortestPaths = shortestPathsIntermediate;
										}
										if(shortestPaths.size() == 0) {
											logger.info("Shortest path gives 0 length");
											logger.info("Graph: "+ g.getSummaryGraphString());

											boolean found = false;
											//IF otherVertex is actually an annotProp filter, attach it to vertex
											String filteredClass = null;
											for(Entry<String, Integer> entry: filters.entrySet()) {
												if(entry.getValue() > 1) {
													String cls = entry.getKey().split("###")[0];
													String propFilter = entry.getKey().split("###")[1];
													if(cls.contains("http://www.w3.org/2002/07/owl#AnnotationProperty")){
														if(propFilter.equals(otherVertex))
															filteredClass = vertex;
														else if(propFilter.equals(vertex))
															filteredClass = otherVertex;
														if(filteredClass == null)
															continue;
														if(!g.getSummaryGraph().containsVertex(filteredClass)) break;
														found = true;
														logger.info("ADDING LINK BETWEEN " + vertex + " and " + otherVertex);
														Graph< String, SummaryEdge> orig = g.getSummaryGraph();
														orig.addVertex(propFilter);
														orig.addVertex(filteredClass);
														HashSet<String> abc = new HashSet<String>();
														abc.add(propFilter);
														orig.addEdge(filteredClass, propFilter,
																new SummaryEdge(new SummaryVertex(filteredClass, VertexType.CLASS), 
																		new SummaryVertex(propFilter, VertexType.PROPERTY),
																		abc));
														g.setSummaryGraph(orig);
													}	
												}
											}
											if(!found){	
												logger.info("Skipping tree " + vertex + "  and " + otherVertex);						
												skipTree=true;
												continue;
											}
											shortest = 
													new KShortestSimplePaths<String, SummaryEdge>(g.getSummaryGraph(), Constants.MAX_HOPS_SHORTEST_PATH);

											shortestPaths = shortest.getPaths(class1, class2, Constants.MAX_SHORTEST_PATHS_ALTERNATIVES_COUNT);
										}
										if(shortestPaths.size() == 0) {
											logger.info("Shortest path again gives 0 length");
											skipTree=true;
											continue;
										}
										minCost = shortestPaths.get(0).getLength();
										iPaths = new GraphPathSet();
										for(int i = 0; i < shortestPaths.size(); i++) {
											GraphPath<String, SummaryEdge> currentPath = shortestPaths.get(i);

											if(currentPath.getLength() > minCost)
												break;
											if(!iPaths.contains(currentPath))
												iPaths.add(currentPath);
										}
										pathsCache.put(class1 + class2, iPaths);
									}
									else {
										iPaths = pathsCache.get(class1 + class2);
										logger.info("Found in cache: "+ iPaths);

									}
									if(iPaths.size() == 0){
										logger.info("WARN: no path between "+ vertex + " and "+ otherVertex);
										skipTree = true;
										continue;
									}

									if(iPaths.size() == 1){
										for(GraphPath<String, SummaryEdge> path : iPaths){
											// only consider shortest paths, build alternative trees when there are multiple	
											//TODO: FIGURE OUT IF EVEN HERE THERE ARE NOT MULTIPLE?
											for(SummaryEdge s : path.getEdgeList()){
												toAdd.add(s);
											}
										}
									} else {
										alternatives.put(alternativeGroup++, iPaths);
									}
								}
							}
					}
					if(skipTree) {
						continue;
					}

					/**
					 * 3. compute spanning trees with vertices added to the tree
					 */
					for (SummaryEdge s : toAdd){
						steinerTree.addVertex(s.getDest());
						steinerTree.addVertex(s.getSrc());
						if(!steinerTree.edgeSet().contains(s))
							steinerTree.addEdge(s.getSrc(), s.getDest(), s);
					}

					for(String propVertex: toRemovePropertyVertices){
						steinerTree.removeVertex(propVertex);
					}

					int num_results = 0;
					int size = 0;
					List<HashSet<GraphPath<String, SummaryEdge>>> result = new ArrayList<HashSet<GraphPath<String, SummaryEdge>>>();
					for(HashSet<GraphPath<String, SummaryEdge>> alternative : alternatives.values()){
						size += alternative.size();
						num_results += 1;
						//TODO here - if there is a long path - CHOOSE CAREFULLY what to add to cartesian product
						if(Math.pow(size, num_results) < Integer.MAX_VALUE) {
							result.add(new HashSet<GraphPath<String, SummaryEdge>>(alternative));
						}
						else
							break;
					}

					// TODO: make sure not to add same pair twice (e.g. if A and B are in two sets, don't add them twice

					/** required: the cartesian product of sets containing alternative shortest paths of same cost */
					//TODO: check if MORE THAN 1 property (e.g. inTaxon and isExpressed -> get added correctly)
					//logger.info("SETS FOR COMPUTING CARTESIAN: "+ result);
					Set<List<GraphPath<String, SummaryEdge>>> res = Sets.cartesianProduct(result);
					//logger.info("THE CARTESIAN PRODUCT HAS "+ res.size() + " results");
					//if(steinerTree.edgeSet().size() == 0)
					//	continue;
					int exploredOptions = 0;
					// construct cartesian product of alternatives
					for(List<GraphPath<String, SummaryEdge>> alternative : res) {
						// TODO: sort this according to some relevance order!!!
						if(exploredOptions > Constants.MAX_ALTERNATIVES_COUNT)
							break;
						Graph<String, SummaryEdge> alternateTree = new SimpleGraph<String, SummaryEdge>(SummaryEdge.class);
						if(steinerTree.edgeSet().size() != 0)
							Graphs.addGraph(alternateTree, steinerTree);
						boolean break_here = false;
						for(GraphPath<String, SummaryEdge> path : alternative) {
							//TODO: HERE THE POINT IS: SKIP OVER TREES THAT HAVE DISCONNECTED NODES

							//ALSO, CACHE SHORTEST PATHS!!! 
							if(break_here)
								break;
							for(SummaryEdge s : path.getEdgeList()){
								if(break_here)
									break;
								logger.info("ADDING ALTERNATIVE " + s.getSrc() + " and " + s.getDest() + " in tree " + alternateTree + "\n\n");
								alternateTree.addVertex(s.getDest());
								alternateTree.addVertex(s.getSrc());
								if(!alternateTree.edgeSet().contains(s)){
									alternateTree.addEdge(s.getSrc(), s.getDest(), s);
								}

								//HERE AGAIN SHORTEST PATHS NEED TO BE ADDED otherwise steiner tree will include disconnected edges

								Set<String> origVertices = new HashSet<String>(alternateTree.vertexSet());

								for(String vertex : origVertices) {
									//EXCLUDE PRIMITIVE TYPES here
									if(!vertex.equals(s.getDest()) && !vertex.contains("http://www.w3.org/2001/XMLSchema") && !s.getDest().contains("http://www.w3.org/2001/XMLSchema")) {
										KShortestSimplePaths<String, SummaryEdge> shortest = 
												new KShortestSimplePaths<String, SummaryEdge>(g.getSummaryGraph(), Constants.MAX_HOPS_SHORTEST_PATH);

										List<GraphPath<String, SummaryEdge>> shortestPaths = null;
										List<GraphPath<String, SummaryEdge>> minimalShortestPaths = new LinkedList<GraphPath<String, SummaryEdge>>();
										try { 
											logger.info("Computing shortest path between "+ vertex + " and " + s.getDest());
											if(shortestPathsCache.get(vertex + s.getDest()) == null) {
												shortestPaths = shortest.getPaths(vertex, s.getDest(), Constants.MAX_SHORTEST_PATHS_ALTERNATIVES_COUNT);
												int min_len = shortestPaths.get(0).getLength();
												for(GraphPath<String, SummaryEdge> gp : shortestPaths) {
													if(gp.getLength() > min_len)
														break;
													minimalShortestPaths.add(gp);
												}
												shortestPathsCache.put(vertex + s.getDest(), minimalShortestPaths);
											}
											else {
												logger.info("Found in cache for " + vertex + " and " + s.getDest());
												shortestPaths = shortestPathsCache.get(vertex + s.getDest());
											}
										} catch (Exception e) {
											logger.info("GOT ERROR Computing shortest path: ");
											e.printStackTrace();
											logger.info("GRAPH: "+ g.getSummaryGraphString());
											logger.info("Steiner: " + steinerTree);
											break_here = true;
											break;
										}
										if(shortestPaths.size() == 0) {
											//TODO : decide if we want to skip this tree altogether
											logger.info("Shortest path between "+ vertex + " and "+ s.getDest() + " gives 0 length"); //TODO: REMOVE
											break_here = true;
											break;
										}
										//int minCost = shortestPaths.get(0).getLength();

										for(SummaryEdge newEdge: shortestPaths.get(0).getEdgeList()) {
											//logger.info("ADDING ALTERNATIVE " + newEdge.getSrc() + " and " + newEdge.getDest() + " in tree " + alternateTree + "\n\n");
											alternateTree.addVertex(newEdge.getDest());
											alternateTree.addVertex(newEdge.getSrc());
											if(!alternateTree.edgeSet().contains(newEdge)){
												alternateTree.addEdge(newEdge.getSrc(), newEdge.getDest(), newEdge);
											}
										}

									}
								}

								for(String vertex: origVertices) {
									if(!vertex.equals(s.getSrc())&& !vertex.contains("http://www.w3.org/2001/XMLSchema") && !s.getSrc().contains("http://www.w3.org/2001/XMLSchema")) {
										KShortestSimplePaths<String, SummaryEdge> shortest = 
												new KShortestSimplePaths<String, SummaryEdge>(g.getSummaryGraph(), Constants.MAX_HOPS_SHORTEST_PATH);
										List<GraphPath<String, SummaryEdge>> shortestPaths = null;
										List<GraphPath<String, SummaryEdge>> minimalShortestPaths = new LinkedList<GraphPath<String, SummaryEdge>>();
										try { 
											if(shortestPathsCache.get(vertex + s.getSrc()) == null) {
												logger.info("Computing shortest between " + vertex + " and " + s.getSrc() + " line 1198");
												shortestPaths = shortest.getPaths(vertex, s.getSrc(), Constants.MAX_SHORTEST_PATHS_ALTERNATIVES_COUNT);
												int min_len = shortestPaths.get(0).getLength();
												for(GraphPath<String, SummaryEdge> gp : shortestPaths) {
													if(gp.getLength() > min_len)
														break;
													minimalShortestPaths.add(gp);
												}
												//TODO: remove all those with path higher than MIN
												logger.info("OBTAINED: " + minimalShortestPaths + "\n\n");
												shortestPathsCache.put(vertex + s.getSrc(), minimalShortestPaths);
											}
											else {
												logger.info("Found in cache for " + vertex + " " + s.getSrc());
												shortestPaths = shortestPathsCache.get(vertex + s.getSrc());
											}
										} catch (Exception e) {
											continue;
										}
										if(shortestPaths.size() == 0) {
											//TODO : decide if we want to skip this tree altogether
											logger.info("Shortest path HERE gives 0 length"); //TODO: REMOVE
											break_here = true;
											break;
											//TODO: CHECK IF THIS IS BETTER??? continue;
										}
										int minCost = shortestPaths.get(0).getLength();
										for(SummaryEdge newEdge: shortestPaths.get(0).getEdgeList()) {
											alternateTree.addVertex(newEdge.getDest());
											alternateTree.addVertex(newEdge.getSrc());
											if(!alternateTree.edgeSet().contains(newEdge))
												alternateTree.addEdge(newEdge.getSrc(), newEdge.getDest(), newEdge);
										}
									}
								}
							}
						}

						boolean skipTreeInvalid = false;
						// SKIP trees that don't contain all the matches
						// TODO: here need to still check sometimes filter doesn't appear but query is included
						for(Term term : alternativeMatch) {
							//if(term.matchType == MatchType.FILTER) {
							if(term.filteredClass.contains("http://www.w3.org/2002/07/owl#AnnotationProperty"))
								continue;
							if((term.matchType == MatchType.FILTER && !(alternateTree.toString().contains(term.filteredClass))) || (term.matchType != MatchType.FILTER && !alternateTree.toString().contains(term.value))) {
								logger.info("SKIPPING TREE " + alternateTree + " because it does not contain " + term);
								skipTreeInvalid = true;
								break;
							}
							//  }
						}

						if(skipTreeInvalid)
							continue;

						if(!break_here){
							HashSet<List<Term>> prev = trees.get(alternateTree);
							if(prev == null)
								prev = new HashSet<List<Term>>();
							prev.add(alternativeMatch);
							trees.put(alternateTree, prev);
						}
						exploredOptions += 1;
					}

					if(trees.size() == 0){
						HashSet<List<Term>> newList = new HashSet<List<Term>>();
						newList.add(alternativeMatch);
						trees.put(steinerTree, newList);
					}

					for(Graph<String, SummaryEdge> alternateTree : trees.keySet()){
						KruskalMinimumSpanningTree<String, SummaryEdge> kmst = new KruskalMinimumSpanningTree<String, SummaryEdge>(alternateTree);
						SpanningTree<SummaryEdge> spTree = kmst.getSpanningTree();
						logger.info("SPANNING TREE : " + spTree + " from alternative " + alternateTree);
						double weight = spTree.getWeight();
						if (weight == 0) {
							continue;
						}
						// this will require another pass through the trees,
						// because there might be MULTIPLE min weight ones
						if (weight < minWeight){
							minWeight = weight;
							minTree = spTree;
						}
					}
					logger.info("Min spanning tree weight: "+ minWeight + "\n"+ minTree);
				}
			}


			logger.info("****** TOTAL ALTERNATE TREES: "+ trees.size());


			HashSet<String> alreadyGenerated = new HashSet<String>();
			int currentAnswer = 0;

			class QueryGraphTreeComparator implements Comparator<Map.Entry> {

				public QueryGraphTreeComparator(){
				}

				//TODO: add here COMPARE BY INFORMATION SCORE OF TABLES!
				@Override
				public int compare(Map.Entry o1, Map.Entry o2) {
					Graph<String, SummaryEdge> alternateTree1 = (Graph<String, SummaryEdge>) o1.getKey();
					Graph<String, SummaryEdge> alternateTree2 = (Graph<String, SummaryEdge>) o2.getKey();
					KruskalMinimumSpanningTree<String, SummaryEdge> kmst1 = new KruskalMinimumSpanningTree<String, SummaryEdge>(alternateTree1);
					SpanningTree<SummaryEdge> spTree1 = kmst1.getSpanningTree();


					KruskalMinimumSpanningTree<String, SummaryEdge> kmst2 = new KruskalMinimumSpanningTree<String, SummaryEdge>(alternateTree2);
					SpanningTree<SummaryEdge> spTree2 = kmst2.getSpanningTree();

					/*if(spTree1.getWeight() > spTree2.getWeight())
						return 1;
					else if(spTree1.getWeight() < spTree2.getWeight())
						return -1;
					 */
					//on a tie, sort by sum of page ranks of terms
					//else {
					List<Term> terms1 = (List<Term>) o1.getValue();
					Double pageRank1 = 0.0;
					for(Term term: terms1) {
						double factor = term.key.length()/(double)term.label.length();
						pageRank1 += factor * term.pageRank;
					}

					List<Term> terms2 = (List<Term>) o2.getValue();
					Double pageRank2 = 0.0;
					for(Term term: terms2) {
						double factor = term.key.length()/(double)term.label.length();
						pageRank2 += factor * term.pageRank;
					}
					if(pageRank1 > pageRank2) {
						return -1;
					}
					else if(pageRank1 < pageRank2) {
						return 1;
					}
					else {
						if(spTree1.getWeight() > spTree2.getWeight())
							return 1;
						else if(spTree1.getWeight() < spTree2.getWeight())
							return -1;
					}

					//}
					return 0;
				}

			}


			// collect SPARQL statements to execute
			LinkedList<Map.Entry> sortedTrees = new LinkedList<Map.Entry>();
			for(Map.Entry<Graph<String,SummaryEdge>, HashSet<List<Term>>> treeTermsCombinations: trees.entrySet()){
				for(List<Term> terms : treeTermsCombinations.getValue()){
					Map.Entry<Graph<String,SummaryEdge>, List<Term>> treeTerms = new AbstractMap.SimpleEntry<Graph<String,SummaryEdge>, List<Term>>(treeTermsCombinations.getKey(), terms);
					sortedTrees.add(treeTerms);
				}
			}
			Collections.sort(sortedTrees, new QueryGraphTreeComparator());
			Iterator it = sortedTrees.iterator();
			while (it.hasNext()) {
				Map.Entry<Graph<String, SummaryEdge>, List<Term>> entry = (Map.Entry<Graph<String, SummaryEdge>, List<Term>>)it.next();
				Graph<String, SummaryEdge> alternateTree = entry.getKey();
				KruskalMinimumSpanningTree<String, SummaryEdge> kmst = new KruskalMinimumSpanningTree<String, SummaryEdge>(alternateTree);
				SpanningTree<SummaryEdge> spTree = kmst.getSpanningTree();

				double weight = spTree.getWeight();

				if (weight == 0) {
					continue;
				}

				List<Term> alternateMatches = entry.getValue();

				Double pageRank = 0.0;
				for(Term term : alternateMatches) {
					pageRank += term.pageRank;
				}
				
				logger.info("### SELECTED MATCH "+ alternateMatches + " with page rank "+ pageRank);

				if(spTree!= null && currentAnswer != Constants.MAX_RESULT_COUNT) {
					java.util.Map.Entry<String, TreeSet<String>> query = null;		                    
					HashSet<String> sparqlFilters = new HashSet<String>();

					boolean skipTree = false;
					for(Term term : alternateMatches) {
						if(term.matchType == MatchType.FILTER) {
							if(term.filteredClass.contains("Class") || term.filteredClass.contains("http://www.w3.org/2002/07/owl#AnnotationProperty")) 
								continue;
							String[] splits = term.label.toLowerCase().split(Constants.PUNCTUATION_FOR_SPLITS);

							if(splits.length > 1) {
								sparqlFilters.add(term.filteredClass + "###" + term.filteredProp + "###" +  term.label.toLowerCase());
							}
							else {
								sparqlFilters.add(term.filteredClass + "###" + term.filteredProp + "###" + term.key);
							}
						}
						for(Term numericalTerm : numericalTerms) {
							if(numericalTerm.value.equals(term.value)) {
								sparqlFilters.add("NUMERICAL" + "###" + numericalTerm.value + "###" + numericalTerm.operator  + "###" + numericalTerm.key);
							}       
						}
					}

					if(skipTree)
						continue;
					//TODO HERE reconstruct sparqlFilters from the terms!!!!
					query = SPARQLUtilsRemote.translateDAGtoSPARQL(spTree, g, sparqlFilters, negations);

					if(alreadyGenerated.contains(query.getKey()))
						continue;
					alreadyGenerated.add(query.getKey());
					currentAnswer += 1;

					html.append("<div class='clearer'>&#160;</div>\n");
					html.append("<ul>");
					html.append("<li>");
					html.append("RESULT "+ currentAnswer + "<br>");

					html.append(Escape.safeXml(spTree.toString()) + "<br>");
					html.append("<br>");
					html.append("<br>");
					html.append("");

					for(Term term : alternateMatches) {
						// GENERATE 1 GRAPH per result, showing the final matches + alternatives maybe at the end
						html.append("<b> Keyword: </b>"+ term.key);
						if(term.matchType == MatchType.FILTER) {
							html.append("<i> [Will be matched through a SPARQL filter on the class "+ Escape.safeXml(term.filteredClass)  + " property "+ Escape.safeXml(term.filteredProp) + "]</i>"+"<br>");
						}
						else if (term.key.matches("-?\\d+(\\.\\d+)?")) {
							html.append("<b> Match: </b>"+ "<i> Numerical filter on " + term.value +"</i><br>");
						}
						else {
							html.append("<b> Match: </b>"+ term.value +"<br>");
						}
					}

					if(query.getKey().isEmpty()) {
						html.append("<br><u><b> No query could be generated.  </b></u><br>");
						html.append("</li>");
						html.append("<br>");
						html.append("</ul>");
						continue;
					}

					html.append("<br><u><b> SPARQL query:  </b></u><br>");
					html.append(Escape.safeXml(query.getKey()).replaceAll("\\\n", "<br/>\\\n<p style=\"margin-left: 40px\">"));

					html.append("</li>");
					html.append("<br>");

					logger.info("\n\n\nSPARQL QUERY: "+ query.getKey());

					new SPARQLResultElement(currentAnswer).appendHtml(html, dict);

					// remember to execute SPARQL statement asynchronously
					String resultDivId = currentAnswer + "_resultdiv";
					sparqlStmtsMap.put(resultDivId, query.getKey());

					html.append("</ul>");
					html.append("<br>");

				}
			}
			//lookedUp += 1;

			// javascript to execute SPARQL statements asynchronously
			new AsyncDisplayOfSparqlQueries(sparqlStmtsMap, ctxPath).appendHtml(html, dict);
		}

		DagState state = node.getBounds(40, 100);
		int width = state.width;
		int height = state.height;

		// crop (remove prefixes of URIs in graph display)
		node.cropCaptionsAuto();

		// render
		Canvas canvas = new Canvas(html, "dagoutput", width, height);
		canvas.open();
		Context2D context = canvas.getContext2D();
		node.draw(context, state, true);
		canvas.close();
		//return html.toString();

		int countDag = 0;
		for (Entry<Double, List<RdfDagNode>> entry : toDisplayTrees.entrySet()) {
			List<RdfDagNode> dagList = entry.getValue();
			for (RdfDagNode dag : dagList) {
				logger.info("OUTPUT "+ countDag + ": " + dag.toNTriples());
				Html5DagGenericNode html5Dag = dag.toHtml5Dag();
				String idDag = "qg-dag-" +  + countDag + "output";

				String styleNoDisplay = "";
				html.append("<div class='content querygraphs'>\n");
				html.append("<div class='querygraphs-dags'>\n");
				html.append("<div><a href='javascript:toggleDisplay(\"" + idDag + "\")'>-+</a></div>\n");
				html.append("<div id='" + idDag + "'" + styleNoDisplay + ">\n");
				// width and height
				DagState state2 = html5Dag.getBounds(40, 0);
				int width2 = state2.width;
				int height2 = state2.height;

				// crop
				html5Dag.cropCaptionsAuto();

				// render
				Canvas canvas2 = new Canvas(html, "dag" + countDag + "output", width2, height2);
				canvas2.open();
				Context2D context2 = canvas2.getContext2D();
				context2.text("RESULT "+ (countDag % 100) + " [total cost " + entry.getKey() + "]", width2, height2);		
				html5Dag.draw(context2, state2, true);
				canvas2.close();

				html.append("</div>\n");
				html.append("</div>\n");

				countDag++;

			}

		}

		logger.info("Final time: "+ new Timestamp(System.currentTimeMillis()));

		return html.toString();
	}

	/**
	 * walks the DAG to collect variables for the sparql query
	 * @param node
	 * @return
	 */

	public static Set<Set<Object>> cartesianProduct(Set<?>... sets) {
		if (sets.length < 2)
			throw new IllegalArgumentException(
					"Can't have a product of fewer than two sets (got " +
							sets.length + ")");

		return _cartesianProduct(0, sets);
	}

	private static Set<Set<Object>> _cartesianProduct(int index, Set<?>... sets) {
		Set<Set<Object>> ret = new HashSet<Set<Object>>();
		if (index == sets.length) {
			ret.add(new HashSet<Object>());
		} else {
			for (Object obj : sets[index]) {
				for (Set<Object> set : _cartesianProduct(index+1, sets)) {
					set.add(obj);
					ret.add(set);
				}
			}
		}
		return ret;
	}

}
