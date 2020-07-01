package ch.zhaw.biosoda;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.jena.util.iterator.ExtendedIterator;

import org.apache.jena.atlas.io.IndentedWriter;

import org.apache.jena.ontology.EnumeratedClass;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.SpanningTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;

import org.apache.jena.ontology.UnionClass;
import com.hp.hpl.jena.sparql.lib.org.json.JSONArray;
import com.hp.hpl.jena.sparql.lib.org.json.JSONObject;

import ch.ethz.rdf.dag.RdfDagNode;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.metadata.MetadataSingleton;
import org.apache.commons.lang3.tuple.Pair;

public class SPARQLUtilsRemote {
	final static String prolog1 = "PREFIX rdfs: <" + RDFS.getURI() + ">";
	final static String prolog2 = "PREFIX rdf: <" + RDF.getURI() + ">";
	final static String prolog3 = "PREFIX owl: <"+ OWL.getURI() + ">";
	final static String prolog4 = "PREFIX xsd: <"+ XSD.getURI() + ">";

	private final static Logger logger =
			LoggerFactory.getLogger(SPARQLUtilsRemote.class);
	
	private static List<Resource> explodeAnonymousResource(Resource resource)
	{
		List<Property> collectionProperties = new LinkedList<Property>(Arrays.asList(OWL.unionOf,OWL.intersectionOf,RDF.first,RDF.rest));

		List<Resource> resources=new LinkedList<Resource>();
		Boolean needToTraverseNext=false;

		if(resource.isAnon())
		{
			for(Property cp:collectionProperties)
			{
				if(resource.hasProperty(cp) && !resource.getPropertyResourceValue(cp).equals(RDF.nil))
				{
					Resource nextResource=resource.getPropertyResourceValue(cp);
					resources.addAll(explodeAnonymousResource(nextResource));

					needToTraverseNext=true;
				}
			}

			if(!needToTraverseNext)
			{
				resources.add(resource);
			}
		}
		else
		{
			resources.add(resource);
		}

		return resources;
	}

	public static int countInstancesOfClass(String classURI, String endpoint){	
		if(!classURI.startsWith("<"))
			classURI = "<"+classURI + ">";	
		String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + 
				"SELECT (count(*) as ?count) where { ?s a "+ classURI +" } ";
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			if(!rs.hasNext()){
				logger.info("NO SOLUTIONS.");
			}
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				return rb.get("?count").asLiteral().getInt();
				//return Integer.parseInt(rb.get("?count").asLiteral().toString());
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}
		logger.info("\n\n##################################\n\n");
		return 0;

	}

	public static List<String> getLabelRemote(String queryString, String endpoint){
		ArrayList<String> results = new ArrayList<String>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		//query.serialize(new IndentedWriter(System.out, true));
		//
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			if(!rs.hasNext()){
				logger.info("NO SOLUTIONS.");
			}
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				if(rb.get("label") != null)
					results.add(rb.get("label").toString());
				else 
					results.add("");
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}
		return results;
	}

	public static List<String> execRemoteQuery(String queryString, String endpoint){

		// since query execution time is unpredictable (and uncontrollable), 
		// run query in separate thread and kill on timeout

		ArrayList<String> results = new ArrayList<String>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		query.serialize(new IndentedWriter(System.out, true));
		
		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			/*if(!rs.hasNext()){
				logger.info("NO SOLUTIONS.");
			}*/
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				//logger.info(rb.toString());
				if(rb.get("class")!= null && rb.get("class").isURIResource()){
					results.add("<" + rb.get("class").toString() + ">");
					//classes.add(getLiteralFromUri(rb.get("class").toString()));
					//uriclasses.add("<"+ rb.get("class").toString()+ ">");
				}
				if(rb.get("label")!= null){
					results.add(rb.get("label").toString());
					//classes.add(getLiteralFromUri(rb.get("class").toString()));
					//uriclasses.add("<"+ rb.get("class").toString()+ ">");
				}
				if(rb.get("instance")!= null){
					results.add(getLiteralFromUri(rb.get("instance").toString()));
					//System.out.print(getLiteralFromUri(rb.get("instance").toString()) + " \t");
				}

				if(rb.get("prop") != null){
					results.add("<" + rb.get("prop").toString() + ">");
				}

				//logger.info(rb.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}
		//logger.info("\n\n##################################\n\n");
		return results;
	}

	public static List<RdfDagNode> execQuery(String subj, String prop, String obj, String endpoint){
		ArrayList<RdfDagNode> results = new ArrayList<RdfDagNode>();

		if(subj == null){
			if(prop == null || obj == null)
				return results;		
		}

		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+ prolog4 + "\n" + 
				"SELECT " + (subj == null? "?subj ":"") + (prop == null? "?prop ":"") + (obj == null? "?obj ":"") +
				" WHERE { "+ 
				(subj == null? "?subj":"<"+ subj + ">") + " " + 
				(prop == null? "?prop":prop) + " " + 
				(obj == null? "?obj":"<"+ obj + ">") + " . }";

		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);

		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset

		String nTripleRes ="";
		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			QuerySolution rb = null;
			if(rs.hasNext()) {
				rb = rs.nextSolution();
				if(subj == null && (rb.get("subj") == null))
					return results;

				// The order of results is undefined.
				while(true) {
					//logger.info("SOL "+ rb.toString());
					String uriSubj = "<" + (subj == null? rb.get("subj").toString().replace("\n", "") : subj) + ">";
					String uriProp = "<" + (prop == null? rb.get("prop").toString().replace("\n", "") : prop) + ">";
					String uriObj = "<" + (obj == null? rb.get("obj").toString().replace("\n", "") : obj)+ ">";

					nTripleRes += uriSubj + " " + uriProp + " " + uriObj + "\n";

					if(!rs.hasNext())
						break;
					rb = rs.nextSolution();
				}
			}
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}

		if(!nTripleRes.isEmpty()) {
			logger.info("EXTRACTING NODE FROM TRIPLES: "+ nTripleRes);
			RdfDagNode res = RdfDagNode.fromNTriples(nTripleRes);
			results.add(res);
		}
		logger.info("\n\n##################################\n\n");
		return results;
	}

	public static String getDefinitionForUri(String uri, String endpoint) {
		String queryStringLabel = 
				"SELECT ?label WHERE { <" + uri + "> rdfs:label ?label . "
						+ "}";

		List<String> rdfLabels = SPARQLUtilsRemote.execRemoteQuery(queryStringLabel, endpoint);
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);

		String queryStringDescription = 
				"SELECT ?label WHERE { <" + uri + "> rdfs:description ?label  ."
						+ "}";

		rdfLabels = SPARQLUtilsRemote.execRemoteQuery(queryStringDescription, endpoint);
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);

		String queryStringDescription2 =
				"SELECT ?label WHERE { <" + uri + "> <http://purl.org/dc/terms/description> ?label  ."
						+ "}";

		rdfLabels = SPARQLUtilsRemote.execRemoteQuery(queryStringDescription2, endpoint);
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);

		String queryStringComment = 
				"SELECT ?label WHERE { <" + uri + "> rdfs:comment ?label  ."
						+ "}";

		rdfLabels = SPARQLUtilsRemote.execRemoteQuery(queryStringComment, endpoint);
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);

		String queryStringOther = 
				"SELECT ?label WHERE { <" + uri + "> ?prop ?label . ?prop rdfs:range xsd:string. "
						+ "}";

		rdfLabels = SPARQLUtilsRemote.execRemoteQuery(queryStringOther, endpoint);
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);

		return getLiteralFromString(uri);
	}

	public static JSONArray execQueryToJson(String queryString, String endpoint){ //, TreeSet<String> selectVars){
		ArrayList<String> results = new ArrayList<String>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" +queryString;
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		JSONArray ja = new JSONArray();
		try {
			// If ASK query, then execAsk:
			if(queryString.contains("ASK")) {
				boolean result = qexec.execAsk();
				JSONObject jo = new JSONObject();
				jo.put("Answer", result);
				ja.put(jo);
			}
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			if(!rs.hasNext()){
				logger.info("NO SOLUTIONS.");
			}
			// The order of results is undefined.

			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				JSONObject jo = new JSONObject();
				for(String selectedVar : query.getResultVars()){
					if(rb.get(selectedVar) != null && rb.get(selectedVar).isLiteral())
						jo.put("?"+selectedVar, rb.getLiteral(selectedVar).getLexicalForm());
					else if(rb.get(selectedVar) == null)
						jo.put("?"+selectedVar, "");
					else   
						jo.put("?"+selectedVar, rb.get(selectedVar));
				}
				ja.put(jo);
				results.add(rb.toString());
				//logger.info(rb.toString());
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}
		logger.info("\n\n##################################\n\n");
		//logger.info("JSON ARRAY: "+ ja);
		return ja;
	}

	public static ArrayList<Triple> getTriplesFromQuery(String queryString, String endpoint, String subjURI, String propURI, String objURI){
		ArrayList<Triple> results = new ArrayList<Triple>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" +queryString;

		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset

		org.apache.jena.graph.Node propNode, subjNode, objNode; 


		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			if(!rs.hasNext()){
				logger.info("NO SOLUTIONS.");
			}
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();

				if(propURI != null) {
					propNode = ResourceFactory.createResource(propURI).asNode();
				}
				else {
					propNode = rb.get("prop").asNode();
				}

				if(subjURI != null) {
					subjNode = ResourceFactory.createResource(subjURI).asNode();
				}
				else {
					subjNode = rb.get("subj").asNode();
				}

				if(objURI != null) {
					objNode = ResourceFactory.createResource(objURI).asNode();
				}
				else {
					objNode = rb.get("obj").asNode();
				}

				results.add(Triple.create(subjNode, propNode, objNode));

				logger.info(rb.toString());
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}
		logger.info("\n\n##################################\n\n");
		return results;
	}

	public static ArrayList<String> execRemoteQueryGeneric(String queryString, String endpoint){
		ArrayList<String> results = new ArrayList<String>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			if(!rs.hasNext()){
				logger.info("NO SOLUTIONS.");
			}
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				results.add(rb.toString());
				logger.info(rb.toString());
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}
		logger.info("\n\n##################################\n\n");
		return results;
	}

	public static List<String> getAllPropertiesOfThing(String endpoint) {	
		List<String> results = new ArrayList<String>();

		// here, we need to actually fetch properties that have domain / range Thing
		String datarangequeryString =
				"select distinct ?prop where { ?prop <http://www.w3.org/2000/01/rdf-schema#range> " +
						"<http://www.w3.org/2002/07/owl#Thing>" + " } "; 

		List<String> res4 = SPARQLUtilsRemote.execRemoteQuery(datarangequeryString, endpoint);
		//add properties to hashset, this also removes duplicates
		HashSet<String> props4 = new HashSet<>(res4);

		// TODO: add properties from data structure (subClassOf), object properties

		if (props4.size() != 0) {
			for (String className : props4)
				results.add(className);
		}

		String datatypequeryString =
				"select distinct ?prop where { " +
						"?x ?p ?prop .\n" +
						"?x a <http://www.w3.org/2002/07/owl#Thing>" + " } ";

		List<String> res3 = SPARQLUtilsRemote.execRemoteQuery(datatypequeryString, endpoint);
		//add properties to hashset, this also removes duplicates
		HashSet<String> props3 = new HashSet<>(res3);

		if (props3.size() != 0) {
			for (String className : props3)
				results.add(className);
		}

		return results;
	}

	public static HashSet<String> getProps(String queryString, String endpoint){
		HashSet<String> results = new HashSet<String>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			if(!rs.hasNext()){
				logger.info("NO SOLUTIONS.");
			}
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				results.add("<" + rb.get("prop").toString() + ">");
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			qexec.close();
		}
		return results;
	}

	public static HashSet<String> getDomainOfPropertyFromSummaryGraph(String prop, FederatedSummaryGraph g){
		HashSet<String> domains = new HashSet<String>();
		for(SummaryEdge edge: g.getSummaryGraph().edgeSet()) {
			if(edge.toString().contains(prop) && (!edge.getSrc().equals(prop))) {
				domains.add(edge.getSrc());
			}
		}
		return domains;
	}

	public static HashSet<String> getRangeOfPropertyFromSummaryGraph(String prop, FederatedSummaryGraph g){
		HashSet<String> ranges = new HashSet<String>();
		for(SummaryEdge edge: g.getSummaryGraph().edgeSet()) {
			if(edge.toString().contains(prop) && (!edge.getDest().equals(prop))) {
				ranges.add(edge.getDest());
			}
		}
		return ranges;
	}

	public static HashSet<String> getDomainOfPropertyRemote(String prop, String endpoint){
		HashSet<String> results = new HashSet<String>();
		String queryString = "SELECT DISTINCT ?dom where {{?x a ?dom. ?x " + prop + "?y .}  UNION { " + prop + " rdfs:domain ?dom}}";
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		query.serialize(new IndentedWriter(System.out, true));
		
		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			if(!rs.hasNext()){
				logger.info("NO SOLUTIONS.");
			}
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				if(rb.get("dom") != null && rb.get("dom").toString().contains("http"))
					results.add("<" + rb.get("dom").toString() + ">");
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			qexec.close();
		}
		return results;
	}

	public static HashSet<String> getRangeOfPropertyRemote(String prop, String endpoint){
		HashSet<String> results = new HashSet<String>();
		String queryString = "SELECT DISTINCT ?range where {{?x a ?range. ?y " + prop + "?x .} UNION { " + prop + " rdfs:range ?range}}";
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		query.serialize(new IndentedWriter(System.out, true));
		
		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			if(!rs.hasNext()){
				logger.info("NO SOLUTIONS.");
			}
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();				                          
				if(rb.get("range") != null && rb.get("range").toString().contains("http"))
					results.add("<" + rb.get("range").toString() + ">");
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			qexec.close();
		}
		return results;
	}

	public static HashSet<String> getDomainOfPropertyFromLocalOntology(String prop){
		prop = prop.replace("<","").replace(">","");
		HashSet<String> results = new HashSet<String>();
		OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		model.read( MetadataSingleton.getInstance().getMetadata().getDataDirPath() );
		OntProperty locatedIn = model.getOntProperty( prop );
		ExtendedIterator<? extends OntResource> domains = locatedIn.listDomain();
		while ( domains.hasNext() ) { 
			OntResource dom = domains.next();
			String domain = dom.toString() ;
			UnionClass cls = null;		
			EnumeratedClass enm = null;
			try {
				cls = dom.asClass().asUnionClass(); //model.getUnionClass(domain);
			} catch (Exception e)
			{	;
			}
			if(cls != null){
				ExtendedIterator<? extends OntResource> unionClasses = cls.listOperands();
				while ( unionClasses.hasNext() ) {
					String unionClass = unionClasses.next().toString();
					results.add("<"+unionClass+">");
				}}
			else {
				try {
					enm = dom.asClass().asEnumeratedClass();
				} catch (Exception e)
				{       results.add("<"+domain+">");
				}
				if(enm != null){
					ExtendedIterator<? extends OntResource> enums = enm.listOneOf();
					while ( enums.hasNext() ) {
						String enumClass = enums.next().toString();
						results.add("<"+enumClass+">");
					}}

			}
		}
		return results;
	}

	public static HashSet<String> getRangeOfPropertyFromLocalOntology(String prop){
		HashSet<String> results = new HashSet<String>();
		prop = prop.replace("<","").replace(">","");
		OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		model.read( MetadataSingleton.getInstance().getMetadata().getDataDirPath() );
		OntProperty locatedIn = model.getOntProperty( prop );
		ExtendedIterator<? extends OntResource> ranges = locatedIn.listRange();
		while ( ranges.hasNext() ) {
			OntResource ran = ranges.next();
			String range = ran.toString() ;
			UnionClass cls = null;
			EnumeratedClass enm = null;

			try {
				cls = ran.asClass().asUnionClass();
			} catch (Exception e)
			{       ;
			}
			if(cls != null){
				ExtendedIterator<? extends OntResource> unionClasses = cls.listOperands();
				while ( unionClasses.hasNext() ) {
					String unionClass = unionClasses.next().toString();
					results.add("<"+unionClass+">");
				}}
			else{
				try {
					enm = ran.asClass().asEnumeratedClass();
				} catch (Exception e)
				{       results.add("<"+range+">");
				}
				if(enm != null){
					ExtendedIterator<? extends OntResource> enums = enm.listOneOf();
					while ( enums.hasNext() ) {
						String enumClass = enums.next().toString();
						results.add("<"+enumClass+">");
					}}

			}
		} 
		return results;
	}

	public static ArrayList<Triple> getAllValuesOfProperty(String endpoint, String propertyURI) {

		//1. get datatype properties
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+ prolog4 + "\n" +
				"SELECT ?subj ?obj WHERE { ?subj <"+ propertyURI + "> ?obj . } LIMIT "+ Constants.MAX_SPARQL_RESULTS_COUNT;
		ArrayList<Triple> results = (ArrayList<Triple>) getTriplesFromQuery(queryString, endpoint, null, propertyURI, null);
		return results;
	}

	public static ArrayList<Triple> getAllValuesOfSubject(String endpoint, String subjectURI) {

		//1. get datatype properties
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" +
				"SELECT ?prop ?obj WHERE { <"+ subjectURI + "> ?prop ?obj . } LIMIT "+ Constants.MAX_SPARQL_RESULTS_COUNT;
		ArrayList<Triple> results = (ArrayList<Triple>) getTriplesFromQuery(queryString, endpoint, subjectURI, null, null);
		return results;
	}

	public static List<String> getAllPropertiesOfInstance(String endpoint, String instanceURI, boolean getParentProperties) {

		//1. get datatype properties
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+ prolog4 + "\n" +
				"SELECT ?prop ?obj WHERE { <"+ instanceURI + "> ?prop ?obj . }";
		ArrayList<String> results = new ArrayList<String>();
		results = (ArrayList<String>) execRemoteQueryGeneric(queryString, endpoint);

		return results;
	}

	public static List<String> getAllDatatypePropertiesOfClass(String endpoint, String classURI, boolean getParentProperties) {

		//1. get datatype properties
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" +
				"SELECT ?prop ?obj WHERE { <"+ classURI + "> ?prop ?obj . }";
		ArrayList<String> results = new ArrayList<String>();
		results = (ArrayList<String>) execRemoteQueryGeneric(queryString, endpoint);

		return results;
	}

	public static List<String> getAllPropertiesOfClass(String endpoint, String classURI, boolean getParentProperties) {
		ArrayList<String> results = new ArrayList<String>();

		//2. get instance properties (these might be inherited from superclasses)
		//TODO: find a better way to do this, on large graph will be very costly
		final String queryStringProps = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+ prolog4 + "\n" +
				"SELECT ?prop ?obj WHERE { ?x a <"+ classURI + "> .\n" +
				"?x ?prop ?obj . }";
		results.addAll(getProps(queryStringProps, endpoint));

		if(getParentProperties == true && ! classURI.contains("Thing")){
			String parentClassURI = getParentClass(endpoint, classURI);
			logger.info("Parent class of "+ classURI + " is "+ parentClassURI );
			if(parentClassURI == null){
				results.addAll(getAllPropertiesOfThing(endpoint));
			}
		}

		return results;
	}

	public static List<Triple> getAllInstancesOfClass(String endpoint, String classURI, boolean getParentProperties) {

		//1. get datatype properties
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+ prolog4 + "\n" +
				"SELECT ?subj ?prop ?obj WHERE { ?subj a <"+ classURI + ">  . ?subj ?prop ?obj .} LIMIT " + Constants.MAX_SPARQL_RESULTS_COUNT;
		ArrayList<Triple> results = (ArrayList<Triple>) getTriplesFromQuery(queryString, endpoint, null, null, null);

		return results;
	}

	public static String getParentClass(String endpoint, String subclassURI){
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+ prolog4 + "\n" +
				"SELECT ?class WHERE { <"+ subclassURI + "> rdfs:subClassOf ?class . }";
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		try {	                
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				logger.info("Sol: "+ rb.toString());
				return rb.get("class").toString();
			}
		} finally {
			qexec.close();
		}
		return null;
	}

	public static String getTypeOfResource(String endpoint, String resourceURI){
		if(resourceURI == null)
			return null;
		if(!resourceURI.startsWith("<"))
			resourceURI = "<" + resourceURI +">";

		HashSet<String> results = getClassesOfResource(endpoint, resourceURI);
		if(results == null)
			return null;
		results.remove("<http://www.w3.org/2002/07/owl#NamedIndividual>");
		if(results.size() == 0)
			return null;
		return results.iterator().next();
	}

	public static HashSet<String> getTypesOfResource(String endpoint, String resourceURI){

		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+ prolog4 + "\n" +
				"SELECT ?type WHERE { <"+ resourceURI + "> rdf:type ?type . }";
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		HashSet<String> results = new HashSet<String>();
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		try {	                
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				if(rb.get("type").toString().contains("owl:Ontology"))
					continue;
				logger.info("Sol: "+ rb.toString());
				results.add(rb.get("type").toString());
			}
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}
		return results;
	}


	public static String getLiteralFromString(String uri){
		if(uri.equals(""))
			return "unknown";
		if(uri.indexOf("#") == -1)
			return uri.substring(uri.lastIndexOf("/") + 1).replaceAll("[<>-]", "");
		return uri.substring(uri.indexOf("#") + 1).replaceAll("[<>-]", "");
	}

	public static String getClassOfResource(String endpoint, String resourceURI){
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+ prolog4 + "\n" +
				"SELECT ?class WHERE { "+ resourceURI + " a ?class . }";
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset

		try {	                
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				if(rb.get("class").toString().contains("owl:Ontology"))
					continue;
				return "<" + rb.get("class").toString() + ">";
			}
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}
		return null;
	}

	public static HashSet<String> getClassesOfResource(String endpoint, String resourceURI){
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+ prolog4 + "\n" +
				"SELECT ?class WHERE { "+ resourceURI + " a ?class . }";
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		

		HashSet<String> results = new HashSet<String>();
		try {	                
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				//handle union classes etc

				results.add("<" + rb.get("class").toString() + ">");
			}
		} catch (Exception e) {
			logger.info(e.getStackTrace().toString());
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}
		return results;
	}

	public static String getLiteralFromUri(String uriString){
		URI x = null;
		String result = null;
		uriString = uriString.replace("<", "");
		uriString = uriString.replace(">", "");
		try {
			x = new URI(uriString);
		} catch (URISyntaxException e) {

			e.printStackTrace();
		}
		if(x != null){
			if(x.getPath().contains("#"))
				result = x.getPath().substring(x.getPath().indexOf('#') + 1);
			else
				result = x.getPath().substring(x.getPath().lastIndexOf('/') + 1);
		}
		return result;
	}

	public static HashMap<String, String> getLabelsForClasses(FederatedSummaryGraph g){

		HashMap<String, String> labelsForClasses = new HashMap<String, String>();
		//get all classes in the graph, create map of class -> property
		for(String className: g.summaryGraph.vertexSet()) {
			// first get the literal fragment of the URI
			// e.g. <http://unics.cloud/ontology#EC-Project> -> ECProject (dashes are removed)
			String fragmentClassName = getLiteralFromString(className).toLowerCase();
			if(fragmentClassName.contains("string") || fragmentClassName.contains("decimal") || fragmentClassName.contains("integer") || fragmentClassName.contains("references"))
				continue;
			if(fragmentClassName.contains("side_effect") || fragmentClassName.contains("disease") || fragmentClassName.contains("gene") || fragmentClassName.contains("target") || fragmentClassName.contains("drug") || fragmentClassName.contains("offer") || fragmentClassName.contains("enzyme")){
				labelsForClasses.put(fragmentClassName, "<http://www.w3.org/2000/01/rdf-schema#label>");
				continue;
			}
			if(fragmentClassName.contains("person")) {
				labelsForClasses.put(fragmentClassName, "<http://unics.cloud/ontology#fullName>");
				continue;
			}
			else if(className.toLowerCase().contains("eccall") || className.toLowerCase().contains("fundingscheme")) {
				labelsForClasses.put(fragmentClassName, "<http://unics.cloud/ontology#shortName>");
				continue;
			}
			else if(fragmentClassName.contains("project") && !fragmentClassName.contains("role")) {
				labelsForClasses.put(fragmentClassName, "<http://unics.cloud/ontology#title>");
				continue;
			}
			//in general this dataset seems to define things by the extended name
			labelsForClasses.put(fragmentClassName, "<http://unics.cloud/ontology#extendedName>");
		}

		return labelsForClasses;

	}

	public static java.util.Map.Entry<String,TreeSet<String>> translateDAGtoSPARQL(SpanningTree<SummaryEdge> tree, FederatedSummaryGraph g, HashSet<String> filters, HashSet<String> negations) {
		QueryRewriteModule qrw = new QueryRewriteModule();

		HashSet<String> selectVars = new HashSet<String>();
		HashMap<String, String> replacements = new HashMap<String, String>();
		HashMap<String, String> replacementVars = new HashMap<String, String>();


		logger.info("NEGATIONS LIST: " + negations);
		HashSet<String> filterVars = new HashSet<String>();

		HashMap<String, String> varNameToClassMap = new HashMap<String, String>();

		HashSet<Filter> filterTriples = new HashSet<Filter>();
		for(String key: filters) {
			String[] classPropertyValue = key.split("###");
			if(classPropertyValue.length != 3)
				continue;
			if(classPropertyValue[0].equals("NUMERICAL")) {
				String filteredNumericalVarName = "?" + SPARQLUtilsRemote.getLiteralFromString(classPropertyValue[1].toLowerCase());
				Filter rdfFilter = new Filter(classPropertyValue[0], classPropertyValue[1], classPropertyValue[2], filteredNumericalVarName);
				filterTriples.add(rdfFilter);
				selectVars.add(filteredNumericalVarName);
				continue;
			}

			String filteredClass = SPARQLUtilsRemote.getLiteralFromString(classPropertyValue[0].toLowerCase());
			String filteredProp = SPARQLUtilsRemote.getLiteralFromString(classPropertyValue[1].toLowerCase());

			String filteredVarName = "?" + filteredClass + "_" + filteredProp;
			int index = 0;
			while(filterVars.contains(filteredVarName)) {
				filteredVarName += index;
				index++;
			}

			filterVars.add(filteredVarName);

			varNameToClassMap.put("?" + filteredClass, classPropertyValue[0]);
			varNameToClassMap.put(filteredVarName, classPropertyValue[1]);

			Filter rdfFilter = new Filter(classPropertyValue[0], classPropertyValue[1], classPropertyValue[2], filteredVarName);
			filterTriples.add(rdfFilter);
			selectVars.add("?"+SPARQLUtilsRemote.getLiteralFromString(classPropertyValue[0].toLowerCase()));
			selectVars.add(filteredVarName);
		}

		HashSet<String> toRemove = new HashSet<String>();

		//TODO: proper fix!!
		HashSet<String> classesInQuery = new HashSet<String>();

		//TODO: keep track of the number of instances of the same class
		//replace at the end ?class with ?class1 to ?classN

		//TODO: revise query rewrite module


		//NOTE: we should somehow keep track of which is which
		HashMap<String, Integer> classInstances = new HashMap<String, Integer>();
		HashSet<String> getAllInfos = new HashSet<String>();

		HashSet<String> triplePatterns = new HashSet<String>();


		for (SummaryEdge e: tree.getEdges()) {
			switch(e.getSrcType()){
			case CLASS:

				String varName = "?" + SPARQLUtilsRemote.getLiteralFromString(e.getSrc().toLowerCase());

				String previousMapping = varNameToClassMap.get(varName);
				Integer index = 1;
				while(previousMapping != null && (!previousMapping.equals(e.getSrc()))) {
					varName += index.toString();
					index ++;
					previousMapping = varNameToClassMap.get(varName);
				}

				varNameToClassMap.put(varName, e.getSrc());

				boolean found = false;
				for (SummaryEdge e2: tree.getEdges()) {
					if(e2.getEdge().contains("[subClassOf]") && e2.getDest().equals(e.getSrc()))
						found = true;
				}
				if(!found){
					selectVars.add(varName);

					triplePatterns.add(varName + " a "+ e.getSrc());
					classesInQuery.add(e.getSrc());

					Integer instanceNo = classInstances.get(e.getSrc());

					if (instanceNo == null) {
						classInstances.put(e.getSrc(), 1);
					}
					else {
						classInstances.put(e.getSrc(), instanceNo + 1);
					}
				}

				//TODO: ADD HANDLING OF SAME-AS property here

				if(e.getEdge().contains("sameAs")){
					String varSrc = "?" + SPARQLUtilsRemote.getLiteralFromString(e.getSrc().toLowerCase());
					previousMapping = varNameToClassMap.get(varSrc);
					index = 1;
					while(previousMapping != null && (!previousMapping.equals(e.getSrc()))) {
						varSrc += index.toString();
						index ++;
						previousMapping = varNameToClassMap.get(varSrc);
					}

					varNameToClassMap.put(varSrc, e.getSrc());

					String prop = e.getEdge().replace("[", "").replace("]", "");
					String varDest = "?" + SPARQLUtilsRemote.getLiteralFromString(e.getDest().toLowerCase());
					previousMapping = varNameToClassMap.get(varDest);
					index = 1;
					while(previousMapping != null && (!previousMapping.equals(e.getDest()))) {
						varDest += index.toString();
						index ++;
						previousMapping = varNameToClassMap.get(varDest);
					}

					varNameToClassMap.put(varDest, e.getDest());

					// we don't know the direction of sameAs (yes, they do have a directionality) 
					// so we need to add both...
					triplePatterns.add("{{ " +
							varSrc + " <http://www.w3.org/2002/07/owl#sameAs> " + varDest
							+"} UNION { "+ varDest +" <http://www.w3.org/2002/07/owl#sameAs> " + varSrc +
							"}}"
							);
					selectVars.add(varSrc);
					selectVars.add(varDest);

				}

				///TODO: for subclass, replace with instance of superclass (see "fruit fly" example)
				if(!e.getEdge().contains("[domainOf]") && !e.getEdge().contains("[a]") && !e.getEdge().contains("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") && !e.getEdge().contains("[subClassOf]") && !(e.getEdge().contains("sameAs"))){
					String prop = e.getEdge().replace("[", "").replace("]", "");
					String varDest = "?" + SPARQLUtilsRemote.getLiteralFromString(e.getDest().toLowerCase());
					if(varDest.contains("string")){
						varDest = varName + "_" + SPARQLUtilsRemote.getLiteralFromString(prop.toLowerCase());
						for(Filter f:filterTriples){
							if(f.rdfProperty.equals(prop.replace("<", "").replace(">","")))
								f.varName=varDest;
						}

					}

					previousMapping = varNameToClassMap.get(varDest);
					index = 1;
					while(previousMapping != null && (!previousMapping.equals(e.getDest()))) {
						varDest += index.toString();
						index ++;
						previousMapping = varNameToClassMap.get(varDest);
					}

					varNameToClassMap.put(varDest, e.getDest());

					if(!prop.startsWith("<"))
						prop = "<" +prop +">";

					triplePatterns.add(varName + " "+ prop+" " + varDest);
					selectVars.add(varName);
					selectVars.add(varDest);

				}
				if(e.getEdge().contains("[subClassOf]")) {
					String varDest = "?" + SPARQLUtilsRemote.getLiteralFromString(e.getDest().toLowerCase());
					String varSrc = "?" + SPARQLUtilsRemote.getLiteralFromString(e.getSrc().toLowerCase());
					replacementVars.put(varSrc,  varSrc+"_"+varDest.substring(1));
					replacementVars.put(varDest,  varSrc+"_"+varDest.substring(1));
					//replace instance of superclass with subclass
				}
				/*if(e.getEdge().contains("[a]")) {
					String varDest = "?" + SPARQLUtils.getLiteralFromString(e.getDest().toLowerCase());
					getAllInfos.add(varDest);
				}*/
				break;
			case PROPERTY:
				// get input of this src node => source
				// get output => known var / unknown
				String prop = e.getSrc();

				//is this a special keyword to be handled via custom rule?
				QueryRewrite rewrite = qrw.getMappingForKeyword(SPARQLUtilsRemote.getLiteralFromString(prop));


				if (rewrite != null) {
					HashSet<String> rewriteSelectedVars = rewrite.selectVars;
					selectVars.addAll(rewriteSelectedVars);
					triplePatterns.add(rewrite.query);
				}
				break;
			case LITERAL:
				// if literal, then it's probably the name of a class instance,
				// so we don't need a variable for that class anymore
				String destName = SPARQLUtilsRemote.getLiteralFromString(e.getDest().toLowerCase());
				if(e.getEdge().contains("[a]")) {
					varName = "?" + destName;
					previousMapping = varNameToClassMap.get(varName);
					index = 1;
					while(previousMapping != null && (!previousMapping.equals(e.getDest()))) {
						varName += index.toString();
						index ++;
						previousMapping = varNameToClassMap.get(varName);
					}

					varNameToClassMap.put(varName, e.getDest());

					String classVar = varName;
					//result += classVar + " rdf:ID "+ "<"+e.getSrc() +">"+ ".\n";

					//TODO: add filter at the end instead of replacement
					//TODO2: will need to then remove duplicates!! - maybe we can check 
					// if multiple matches of same type => filter, otherwise exact match
					replacements.put(classVar,  e.getSrc());
				} // else keep the variable because this is what we actually want to retrieve
				else {
					String srcID = e.getSrc();
					String propDest = e.getDest();
					if(!propDest.startsWith("<"))
						propDest ="<"+propDest+">";
					String propDestVar = "?"+ SPARQLUtilsRemote.getLiteralFromString(propDest.toLowerCase());

					if(propDestVar.contains("string")) {
						propDestVar = "?" + SPARQLUtilsRemote.getLiteralFromString(propDest.toLowerCase());
					}

					varName =  propDestVar;
					previousMapping = varNameToClassMap.get(varName);
					index = 1;
					while(previousMapping != null && (!previousMapping.equals(propDest))) {
						varName += index.toString();
						index ++;
						previousMapping = varNameToClassMap.get(varName);
					}

					varNameToClassMap.put(varName, propDest);
					propDestVar = varName;

					triplePatterns.add(srcID + " "+ propDest + " "+ propDestVar);
					selectVars.add(propDestVar);
				}
			default:
				break;
			}
			switch(e.getDestType()){
			case PROPERTY:
				String prop = e.getDest();
				// if NO OUTPUT for this, then generate a new var
				//logger.info("#### GETTING MAPPING FOR "+ SPARQLUtilsRemote.getLiteralFromString(prop));
				QueryRewrite rewrite = qrw.getMappingForKeyword(SPARQLUtilsRemote.getLiteralFromString(prop));

				if(rewrite != null) {
					HashSet<String> rewriteSelectedVars = rewrite.selectVars;
					selectVars.addAll(rewriteSelectedVars);
					triplePatterns.add(rewrite.query);
				}
				else {
					String varSrc = "?" + SPARQLUtilsRemote.getLiteralFromString(e.getSrc().toLowerCase());

					String previousMapping = varNameToClassMap.get(varSrc);
					Integer index = 1;
					while(previousMapping != null && (!previousMapping.equals(e.getSrc()))) {
						varSrc += index.toString();
						index ++;
						previousMapping = varNameToClassMap.get(varSrc);
					}

					varNameToClassMap.put(varSrc, e.getSrc());

					selectVars.add(varSrc);
					String varDst = null;

					String rangeClass = null;
					//get varDst from range of property
					for (SummaryEdge e2: tree.getEdges()) {
						if(e2.getSrc().equals(prop) && e2.getSrcType().equals(VertexType.PROPERTY) && e2.getEdge().contains("[range]")){

							logger.info("RANGE FOUND FOR  " + prop + " " + (e2.getDest()));
							varDst = "?" + SPARQLUtilsRemote.getLiteralFromString(e2.getDest().toLowerCase());
							rangeClass = e2.getDest();
						}
					}
					if(varDst==null){
						logger.info(" RANGE NOT FOUND FOR "+varSrc);
						HashSet<String> rangeClasses = SPARQLUtilsRemote.getRangeOfPropertyRemote(prop, Constants.REMOTE_REPO); 
						if(rangeClasses.size() != 0){
							varDst = "?" + SPARQLUtilsRemote.getLiteralFromString(g.getMostSpecificClass(new ArrayList<String>(rangeClasses)).toLowerCase());
							rangeClass = g.getMostSpecificClass(new ArrayList<String>(rangeClasses));
						}
						else {
							varDst = "?" + SPARQLUtilsRemote.getLiteralFromString(e.getDest().toLowerCase());
							rangeClass = e.getDest();
						}
					}
					if(varDst.contains("string")) {
						varDst = varSrc + "_" + SPARQLUtilsRemote.getLiteralFromString(prop.toLowerCase());
						for(Filter f:filterTriples){
							if(f.rdfProperty.equals(prop))
								f.varName=varDst;
						}
					}
					selectVars.add(varDst);
					if(!prop.startsWith("<"))
						prop = "<" +prop +">";

					previousMapping = varNameToClassMap.get(varDst);
					index = 1;
					while(previousMapping != null && (!previousMapping.equals(rangeClass))) {
						varDst += index.toString();
						index ++;
						previousMapping = varNameToClassMap.get(varDst);
					}

					varNameToClassMap.put(varDst, rangeClass);
					triplePatterns.add(varSrc + " "+ prop + " "+ varDst);
				}
				break;
			case CLASS:
				if(e.getEdge().contains("[range]")) {
					triplePatterns.add("?" + SPARQLUtilsRemote.getLiteralFromString(e.getDest().toLowerCase()) + " a "+ e.getDest());
				}
				break;
			default:
				break;
			}
			// if class 'className', replace with ?var rdf:type className
			// if property, add a ?var node attached to it (if none exists)
		}

		selectVars.removeAll(toRemove);

		logger.info("REPLACEMENT VARS: " + replacementVars.entrySet() + " Filters " + filterTriples);

		logger.info("REPLACEMENTs: " + replacements.entrySet());

		for(Entry<String, String> entry: replacementVars.entrySet()){
			String toReplace = replacements.get(entry.getKey());
			if(toReplace != null){
				replacements.put(entry.getValue(), toReplace);
				//replacements.remove(entry.getKey());
			}
			//else{
			logger.info("REPLACING VARIABLE "+ entry.getKey() + " with "+ entry.getValue());
			HashSet<String> toAdd = new HashSet<String>();
			//replace in all triples with a uniform variable name
			for (Iterator<String> i = triplePatterns.iterator(); i.hasNext();) {
				String element = i.next();
				//if(!(element.contains(entry.getKey() + " a ")))
				toAdd.add(element.replaceAll("\\?\\b" +entry.getKey().substring(1) + "\\b", entry.getValue()));
				i.remove();

			}
			triplePatterns.addAll(toAdd);

			//}			
			selectVars.remove(entry.getKey());
			selectVars.add(entry.getValue());
		}

		logger.info("REPLACEMENTs: " + replacements.entrySet());


		boolean skip = false;
		for(Entry<String, String> entry: replacements.entrySet()){
			String toReplace = entry.getKey();

			if(replacementVars.get(entry.getKey())!= null){
				toReplace = replacementVars.get(entry.getKey());
			}
			skip = false;
			for(Filter f: filterTriples){
				//HERE MAKE SURE TO SKIP EVEN IF CLASS NAME WAS REPLACED BY SUPERCLASS
				String toCheck = "?"+SPARQLUtilsRemote.getLiteralFromString(f.rdfClass.toLowerCase());
				logger.info("CHECKING FOR filtered var " + toCheck + " in " + replacementVars.entrySet() + " and entry "+ toReplace + " and replacements " + replacements);
				if(replacementVars.get(toCheck) != null){
					toCheck = replacementVars.get(toCheck);
				}
				if(toCheck.equals(toReplace)){
					skip = true;
					f.rdfClass = toCheck.substring(1);
					selectVars.add(toCheck);
				}
			}
			if(!skip) {
				logger.info("REPLACING "+ toReplace + " with "+ entry.getValue());
				HashSet<String> toAdd = new HashSet<String>();
				for (Iterator<String> i = triplePatterns.iterator(); i.hasNext();) {
					String element = i.next();
					//if(!(element.contains(entry.getKey() + " a ")) || (!(element.contains(toReplace + " a "))))
					toAdd.add(element.replaceAll("\\?\\b" +entry.getKey().substring(1) + "\\b", entry.getValue()));
					i.remove();
				}
				triplePatterns.addAll(toAdd);
				selectVars.remove(entry.getKey());
			}
		}

		if(selectVars.remove("?annotationproperty")) {
			selectVars.add("?description");
			HashSet<String> toAdd = new HashSet<String>();
			for (Iterator<String> i = triplePatterns.iterator(); i.hasNext();) {
				String element = i.next();
				i.remove();
				toAdd.add(element.replace("?annotationproperty", "?description"));
			}
			triplePatterns.addAll(toAdd);
		}

		String aux;
		for(Entry<String, Integer> classInstance: classInstances.entrySet()){
			if(classInstance.getValue() != 1){
				// need to figure out WHICH refers to which
			}
		}

		HashSet<String> removeSel = new HashSet<String>();
		for(String var:selectVars){
			boolean found = false;
			for(String tp: triplePatterns){
				if(tp.contains(var))
					found = true;
			}
			// also show filtered variables, e.g. ?label if there is a FILTER like contains(str(?label), "bla"
			for(Filter filter: filterTriples){
				if(filter.varName.equals(var))
					found = true;
			}
			if(!found)
				removeSel.add(var);
		}

		selectVars.removeAll(removeSel);

		//for negated entities we don't add descriptions anymore
		if(Constants.ADD_DESCRIPTIONS && negations.size() == 0) {
			HashMap<String, String> labelsForClasses = getLabelsForClasses(g);
			HashSet<String> toAdd = new HashSet<String>();
			for(String selectVar: selectVars) {
				String labelProperty = labelsForClasses.get(selectVar.substring(1));
				if(labelProperty == null){
					logger.info("Could not find LABEL property for " + selectVar);
					continue;
				}
				String labelPropertyVarName = selectVar + "_"+ getLiteralFromString(labelProperty).toLowerCase();
				//don't add filtered classes
				for(Filter filter: filterTriples)
					if(filter.varName.equals(labelPropertyVarName))
						continue;
				if(!selectVars.contains(labelPropertyVarName)) {
					toAdd.add(labelPropertyVarName);
					triplePatterns.add(selectVar + " "+ labelProperty + " " + labelPropertyVarName);
				}
			}
			selectVars.addAll(toAdd);
		}


		TreeSet<String> selectVarsSorted = new TreeSet<String>(selectVars);
		String result = "";
		if(triplePatterns.size() > 0){
			aux = "SELECT DISTINCT ";
			for(String var : selectVarsSorted)
				aux += var + " ";
			// if there are no variables to select, ask for validity to remove unfeasible results
			// e.g. human_protein located_in rat
			if (selectVars.size() == 0) {
				if(getAllInfos.size() != 0) {
					result = " SELECT * WHERE { ";
					result += "\n";
					/*for(String var: getAllInfos) {
						//HERE GET ALL INFO: test with "anatomic entities lung
						//ACTUALLY SHOULD USE FILTER INFO HERE
					}*/
				}
				result = " ASK { ";
				for(String triple : triplePatterns) {
					result += triple + ". \n";
				}
				result += " } ";
				selectVars.add("Answer");
			}
			else {
				result = aux + " WHERE { ";
				result += "\n";
				HashSet<String> toAddNegations = new HashSet<String>();
				for(String triple : triplePatterns) {
					boolean skipTriple = false;
					for(String uri : negations) {
						if(triple.contains(uri)) {
							toAddNegations.add("MINUS { " + triple + " } " + "\n");
							skipTriple = true;
							//TODO: HERE SHOULD NOT LONGER ADD DESCRIPTION FIELDS FOR VARS THAT ARE NEGATED!
						}
					}
					if(!skipTriple)
						result += triple + ". \n";
				}
				for(String negatedTriple : toAddNegations)
					result += negatedTriple;

				//FILTERS
				for(Filter f: filterTriples) {
					String filteredInstance = "?" + SPARQLUtilsRemote.getLiteralFromString(f.rdfClass.toLowerCase());
					if(replacementVars.get(filteredInstance) != null)
						filteredInstance = replacementVars.get(filteredInstance);
					if(!f.rdfProperty.equals("uri") &&!f.rdfClass.equals("NUMERICAL")){// && selectVars.contains(filteredInstance)) {
						result += filteredInstance +" " + f.rdfProperty + " " + f.varName +". " + "\n"; 
					}
					/*else {
						String toAdd = "?" + SPARQLUtils.getLiteralFromString(f.rdfClass.toLowerCase()) + " a " + f.rdfClass; 
						if(!triplePatterns.contains(toAdd))
							result += toAdd + ". ";
					}*/
				}
				for(Filter f: filterTriples) {
					if(f.rdfClass.equals("NUMERICAL")){
						result += " FILTER (str(" + f.varName + ") " + " = " + "\"" + f.kw.toLowerCase() + "\"" + ")" + "\n";
						continue;
					}
					if(!f.rdfProperty.equals("uri")) {
						result += " FILTER (contains(lcase(str(" + f.varName + ")), "+ "\"" + f.kw.toLowerCase() + "\"" + "))" + "\n"; 
					}
				}

				result += " }" + "\n";

				if(Constants.ENABLE_ORDER_BY){	
					for(Filter f: filterTriples) {
						if(!f.rdfProperty.equals("uri")) {

							result += " order by strlen(str(" + f.varName + " )) ";
						}
					}
				}
			}
			result += " LIMIT "+ Constants.MAX_SPARQL_RESULTS_COUNT ;
		}


		logger.info("QUERY "+ result +"\n\n" +  " and triples "+ triplePatterns);

		java.util.Map.Entry<String, TreeSet<String>> resultDict = new java.util.AbstractMap.SimpleEntry<>(result,selectVarsSorted);

		return resultDict;
	}

	public static boolean isSubClassOf(String cls1, String cls2, String endpoint) {
		String queryString = "ASK { " + cls1 + " rdfs:subClassOf* " + cls2 + ".}";
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		return qexec.execAsk();
	}

	public static HashSet<Pair<String, String>> execQueryWithOffsetandLimit(String subject, String propertyURI, String object, int offset,
			int limit, String endpoint) {
		
		HashSet<Pair<String, String>> results = new HashSet<Pair<String, String>>();
		
		String querySPARQL = "select distinct ?subj ?obj where { ?subj "+ propertyURI + " ?obj . } OFFSET "+ offset + " LIMIT "+ limit;
		
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+ prolog4 + "\n" + querySPARQL;

		final Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);

		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset

		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			QuerySolution rb = null;
			if(rs.hasNext()) {
				rb = rs.nextSolution();
				// The order of results is undefined.
				while(true) {
					//logger.info("SOL "+ rb.toString());
					if(rb.get("subj") != null && rb.get("obj") != null) {		
						Pair<String, String> subj_obj = Pair.of(rb.get("subj").toString(), rb.getLiteral("obj").toString());
						results.add(subj_obj);
					}

					if(!rs.hasNext())
						break;
					rb = rs.nextSolution();
				}
			}
		} finally {
			qexec.close();
		}

		logger.info("\n\n##################################\n\n");
		
		return results;
	}

}



