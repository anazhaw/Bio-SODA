package ch.zhaw.biosoda;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
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

class Filter{
	String rdfClass;
	String rdfProperty;
	String operator;
	String kw;
	String varName;

	public Filter(String cls, String prop, String operator, String kw, String varName) {
		this.rdfClass = cls;
		this.rdfProperty = prop;
		this.operator = operator;
		this.kw = kw;
		this.varName = varName;
	}
}

public class SPARQLUtilsLocal {
	final static String prolog1 = "PREFIX rdfs: <" + RDFS.getURI() + ">";
	final static String prolog2 = "PREFIX rdf: <" + RDF.getURI() + ">";
	final static String prolog3 = "PREFIX owl: <"+ OWL.getURI() + ">";
	final static String prolog4 = "PREFIX xsd: <"+ XSD.getURI() + ">";
	private final static Logger logger =
			LoggerFactory.getLogger(SPARQLUtilsLocal.class);


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

	/** a definition is either a label, or a description, or a text field in the general case **/
	public static String getDefinitionForUri(String uri, Model m) {
		String queryStringLabel = 
				"SELECT ?label WHERE { <" + uri + "> rdfs:label ?label . "
						+ "}";

		List<String> rdfLabels = SPARQLUtilsLocal.execQuery(queryStringLabel, m);
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);

		String queryStringDescription = 
				"SELECT ?label WHERE { <" + uri + "> rdfs:description ?label  ."
						+ "}";

		rdfLabels = SPARQLUtilsLocal.execQuery(queryStringDescription, m);
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);

		String queryStringDescription2 =
				"SELECT ?label WHERE { <" + uri + "> <http://purl.org/dc/terms/description> ?label  ."
						+ "}";

		rdfLabels = SPARQLUtilsLocal.execQuery(queryStringDescription2, m);
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);

		String queryStringComment = 
				"SELECT ?label WHERE { <" + uri + "> rdfs:comment ?label  ."
						+ "}";

		rdfLabels = SPARQLUtilsLocal.execQuery(queryStringComment, m);
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);

		String queryStringOther = 
				"SELECT ?label WHERE { <" + uri + "> ?prop ?label . ?prop rdfs:range xsd:string. "
						+ "}";

		rdfLabels = SPARQLUtilsLocal.execQuery(queryStringOther, m);
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);

		return getLiteralFromString(uri);
	}

	/** generic function to execute a given SPARQL query against a loaded Jena Model 
	 * TODO: refactor function to make it more generic, now it assumes that variable names are e.g. "class" or "label" etc
	 * **/
	public static List<String> execQuery(String queryString, Model m){
		ArrayList<String> results = new ArrayList<String>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + prolog4 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
		try {
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			if(!rs.hasNext()){
				logger.info("NO SOLUTIONS.");
			}
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				logger.info(rb.toString());
				// TODO: TAKE CARE ABOUT UNION CLASSES - should remove the .isURIResource check
				if(rb.get("class")!= null ){
					if(rb.get("class").toString().startsWith("_:")) {
						List<Resource> classes = explodeAnonymousResource(rb.get("class").asResource());
						for(Resource rClass: classes) {
							results.add("<"+ rClass.getURI().toString() + ">");
						}
					}
					else if (rb.get("class").isURIResource()) {
						results.add("<" + rb.get("class").toString() + ">");
					}
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

	public static JSONArray execQueryToJson(String queryString, Model m){
		ArrayList<String> results = new ArrayList<String>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
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
					jo.put("?"+selectedVar, rb.get(selectedVar));				
				}
				ja.put(jo);
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
		logger.info("JSON ARRAY: "+ ja);
		return ja;
	}

	public static ArrayList<Triple> getTriplesFromQuery(String queryString, Model m, String subjURI, String propURI, String objURI){
		ArrayList<Triple> results = new ArrayList<Triple>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
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

	public static List<String> execQueryGeneric(String queryString, Model m){
		ArrayList<String> results = new ArrayList<String>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
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

	public static List<String> getAllPropertiesOfThing(Model m) {	
		List<String> results = new ArrayList<String>();

		// here, we need to actually fetch properties that have domain / range Thing
		String datarangequeryString =
				"select distinct ?prop where { ?prop <http://www.w3.org/2000/01/rdf-schema#range> " +
						"<http://www.w3.org/2002/07/owl#Thing>" + " } "; 

		List<String> res4 = SPARQLUtilsLocal.execQuery(datarangequeryString, m);
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

		List<String> res3 = SPARQLUtilsLocal.execQuery(datatypequeryString, m);
		//add properties to hashset, this also removes duplicates
		HashSet<String> props3 = new HashSet<>(res3);

		if (props3.size() != 0) {
			for (String className : props3)
				results.add(className);
		}

		return results;
	}

	public static HashSet<String> getProps(String queryString, Model m){
		HashSet<String> results = new HashSet<String>();
		queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n" + queryString;
		final Query query = QueryFactory.create(queryString);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
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

	public static HashSet<String> getDomainOfProperty(String prop){
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

	public static HashSet<String> getRangeOfProperty(String prop){
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
				cls = ran.asClass().asUnionClass(); //model.getUnionClass(domain);
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

	public static ArrayList<Triple> getAllValuesOfProperty(Model m, String propertyURI) {

		//1. get datatype properties
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?subj ?obj WHERE { ?subj <"+ propertyURI + "> ?obj . } LIMIT "+ Constants.MAX_SPARQL_RESULTS_COUNT;
		ArrayList<Triple> results = (ArrayList<Triple>) getTriplesFromQuery(queryString, m, null, propertyURI, null);
		return results;
	}

	public static ArrayList<Triple> getAllValuesOfSubject(Model m, String subjectURI) {

		//1. get datatype properties
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?prop ?obj WHERE { <"+ subjectURI + "> ?prop ?obj . } LIMIT "+ Constants.MAX_SPARQL_RESULTS_COUNT;
		ArrayList<Triple> results = (ArrayList<Triple>) getTriplesFromQuery(queryString, m, subjectURI, null, null);
		return results;
	}

	public static List<String> getAllPropertiesOfInstance(Model m, String instanceURI, boolean getParentProperties) {

		//1. get datatype properties
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?prop ?obj WHERE { <"+ instanceURI + "> ?prop ?obj . }";
		ArrayList<String> results = new ArrayList<String>();
		results = (ArrayList<String>) execQueryGeneric(queryString, m);

		return results;
	}

	public static List<String> getAllDatatypePropertiesOfClass(Model m, String classURI, boolean getParentProperties) {

		//1. get datatype properties
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?prop ?obj WHERE { <"+ classURI + "> ?prop ?obj . }";
		ArrayList<String> results = new ArrayList<String>();
		results = (ArrayList<String>) execQueryGeneric(queryString, m);

		return results;
	}

	/** get all properties that apply to a certain class, moving up the inheritance hierarchy 
	 * (properties of parent classes also apply to child classes, so in principle should traverse up to "Thing")
	 * @param m
	 * @param classURI
	 * @param getParentProperties
	 * @return
	 */
	public static List<String> getAllPropertiesOfClass(Model m, String classURI, boolean getParentProperties) {
		ArrayList<String> results = new ArrayList<String>();

		//2. get instance properties (these might be inherited from superclasses)
		//TODO: find a better way to do this, on large graph will be very costly
		final String queryStringProps = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?prop ?obj WHERE { ?x a <"+ classURI + "> .\n" +
				"?x ?prop ?obj . }";
		results.addAll(getProps(queryStringProps, m));

		if(getParentProperties == true && ! classURI.contains("Thing")){
			String parentClassURI = getParentClass(m, classURI);
			logger.info("Parent class of "+ classURI + " is "+ parentClassURI );
			if(parentClassURI == null){
				results.addAll(getAllPropertiesOfThing(m));
			}
		}

		return results;
	}

	public static List<Triple> getAllInstancesOfClass(Model m, String classURI, boolean getParentProperties) {

		//1. get datatype properties
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?subj ?prop ?obj WHERE { ?subj a <"+ classURI + ">  . ?subj ?prop ?obj .} LIMIT " + Constants.MAX_SPARQL_RESULTS_COUNT;
		ArrayList<Triple> results = (ArrayList<Triple>) getTriplesFromQuery(queryString, m, null, null, null);

		return results;
	}

	public static String getParentClass(Model m, String subclassURI){
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?class WHERE { <"+ subclassURI + "> rdfs:subClassOf ?class . }";
		final Query query = QueryFactory.create(queryString);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
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

	public static String getTypeOfResource(Model m, String resourceURI){
		if(!resourceURI.startsWith("<"))
			resourceURI = "<" + resourceURI +">";
		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?type WHERE { "+ resourceURI + " rdf:type ?type . }";
		final Query query = QueryFactory.create(queryString);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
		try {	                
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				logger.info("Sol: "+ rb.toString());
				if(rb.get("type").toString().contains("#Ontology"))
					continue;
				return rb.get("type").toString();
			}
		} finally {
			// QueryExecution objects should be closed to free any system
			// resources
			qexec.close();
		}
		return null;
	}

	public static HashSet<String> getTypesOfResource(Model m, String resourceURI){

		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?type WHERE { <"+ resourceURI + "> rdf:type ?type . }";
		final Query query = QueryFactory.create(queryString);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		HashSet<String> results = new HashSet<String>();
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
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
		//TODO: fix this function
		if(uri.equals(""))
			return "unknown";
		if(uri.indexOf("#") == -1)
			return uri.substring(uri.lastIndexOf("/") + 1).replaceAll("[<>]", "");
		return uri.substring(uri.indexOf("#") + 1).replaceAll("[<>]", "");
	}

	public static String getClassOfResource(Model m, String resourceURI){
		final String prolog1 = "PREFIX rdfs: <" + RDFS.getURI() + ">";
		final String prolog2 = "PREFIX rdf: <" + RDF.getURI() + ">";
		final String prolog3 = "PREFIX owl: <"+ OWL.getURI() + ">";

		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?class WHERE { "+ resourceURI + " a ?class . }";
		final Query query = QueryFactory.create(queryString);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
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

	public static HashSet<String> getClassesOfResource(Model m, String resourceURI){
		final String prolog1 = "PREFIX rdfs: <" + RDFS.getURI() + ">";
		final String prolog2 = "PREFIX rdf: <" + RDF.getURI() + ">";
		final String prolog3 = "PREFIX owl: <"+ OWL.getURI() + ">";

		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT ?class WHERE { "+ resourceURI + " a ?class . }";
		final Query query = QueryFactory.create(queryString);
		// Print with line numbers
		query.serialize(new IndentedWriter(System.out, true));
		

		HashSet<String> results = new HashSet<String>();
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
		try {	                
			// Assumption: it’s a SELECT query.
			final ResultSet rs = qexec.execSelect();
			// The order of results is undefined.
			for (; rs.hasNext();) {
				final QuerySolution rb = rs.nextSolution();
				//handle union classes etc

				results.add("<" + rb.get("class").toString() + ">");
			}
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
			// TODO Auto-generated catch block
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

	public static List<RdfDagNode> execQuery(String subj, String prop, String obj, Model m){
		ArrayList<RdfDagNode> results = new ArrayList<RdfDagNode>();
		final String prolog1 = "PREFIX rdfs: <" + RDFS.getURI() + ">";
		final String prolog2 = "PREFIX rdf: <" + RDF.getURI() + ">";
		final String prolog3 = "PREFIX owl: <"+ OWL.getURI() + ">";

		if(subj == null){
			if(prop == null || obj == null)
				return results;		
		}

		final String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT " + (subj == null? "?subj ":"") + (prop == null? "?prop ":"") + (obj == null? "?obj ":"") +
				" WHERE { "+ 
				(subj == null? "?subj":"<"+ subj + ">") + " " + 
				(prop == null? "?prop":prop) + " " + 
				(obj == null? "?obj":"<"+ obj + ">") + " . }";

		final Query query = QueryFactory.create(queryString);

		query.serialize(new IndentedWriter(System.out, true));
		
		// Create a single execution of this query, apply to a model
		// which is wrapped up as a Dataset
		final QueryExecution qexec = QueryExecutionFactory.create(query, m);
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
			RdfDagNode res = RdfDagNode.fromNTriples(nTripleRes);
			results.add(res);
		}
		logger.info("\n\n##################################\n\n");
		return results;
	}

}
