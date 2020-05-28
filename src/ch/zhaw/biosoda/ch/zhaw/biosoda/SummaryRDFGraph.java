package ch.zhaw.biosoda;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This class is used to represent a summary graph
 * This will be augmented with URIs that match keywords
 * in order to build a query graph and construct Sparql queries
 * See paper: http://dl.acm.org/citation.cfm?id=2810357
 * @author simn
 *
 */

public class SummaryRDFGraph {
	Model rdfModel;
	Graph<String, SummaryEdge> summaryGraph =
            new SimpleGraph<String, SummaryEdge>(SummaryEdge.class);
	/*DirectedWeightedPseudograph<String, SummaryEdge> summaryGraph =
            new DirectedWeightedPseudograph<String, SummaryEdge>(SummaryEdge.class);*/

    final static String prolog1 = "PREFIX rdfs: <" + RDFS.getURI() + ">";
    final static String prolog2 = "PREFIX rdf: <" + RDF.getURI() + ">";
    final static String prolog3 = "PREFIX owl: <"+ OWL.getURI() + ">";
    final static String prolog4 = "PREFIX xsd: <" + XSD.getURI() + ">";
    
	private final static Logger logger =
			LoggerFactory.getLogger(SummaryRDFGraph.class);
	
	public List<String> getClasses(Model m) {
        // 1. get all classes in the ontology
        
        final String queryStringClasses = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
        		"SELECT DISTINCT ?class WHERE { ?x a ?class . }";
        return SPARQLUtilsLocal.execQuery(queryStringClasses, m);
        /*for (String classs  : classes){
        	logger.info(classs);
        }*/
	}
	
	public static void execQuery(String remoteSparqlEndpoint) {
		final String queryStringClasses = "PREFIX bgee: <http://bgee.org/bgeeOntology#> "+
			"SELECT * WHERE { " +
			" ?gene a <http://purl.org/net/orth#Gene> ." +
			" ?gene a bgee:AnatomicEntity ." +		
			"}"  ;
		SPARQLUtilsRemote.execRemoteQuery(queryStringClasses, remoteSparqlEndpoint);
		
	}
	
	public List<String> getRemoteClasses(String remoteSparqlEndpoint) {
        // 1. get all classes in the ontology
        
        final String queryStringClasses = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
        		"SELECT DISTINCT ?class WHERE { ?x a ?class . }";
        return SPARQLUtilsRemote.execRemoteQuery(queryStringClasses, remoteSparqlEndpoint);
        /*for (String classs  : classes){
        	logger.info(classs);
        }*/
	}
	
	/*
	 * Takes a model and summarizes the underlying ontology
	 */
	public SummaryRDFGraph(String path, boolean withProps, boolean isFileOrRemoteEndpoint){
		if(isFileOrRemoteEndpoint) {
			final Model model = ModelFactory.createDefaultModel();
			File file = new File(path);
			if(file.isDirectory()){
				for(File f : file.listFiles()){
					if(f.getAbsolutePath().endsWith("ttl") ||
							f.getAbsolutePath().endsWith("rdf") ||
							f.getAbsolutePath().endsWith("owl"))
						FileManager.get().readModel(model, f.getAbsolutePath());
				}
			}
			else {
				FileManager.get().readModel(model, path);
			}
			
			rdfModel = model;
			
			createSummaryGraph(withProps, null);
		}
		else {
			logger.info("Creating summary graph from "+ path);
			createSummaryGraph(withProps, path);
		}
	}
	
	public Model getModel(){
		return rdfModel;
	}
	
	public String getSummaryGraphString(){
		String res = "";
        for (SummaryEdge edge : summaryGraph.edgeSet()){
        	res += edge.toString() + "\n";
        }
        return res;
	}
	
	public  Graph<String, SummaryEdge> getSummaryGraph(){
		return summaryGraph;
	}
	
	public void setSummaryGraph(Graph<String, SummaryEdge> newSG){
		this.summaryGraph = newSG;
	}
	
	public void createSummaryGraph(boolean withProps, String remoteSparqlEndpoint) {
		// the summary graph consists of classes and relations among them
        // 1. get all classes in the ontology
        
        final String queryStringClasses = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
        		"SELECT DISTINCT ?class WHERE { ?class a owl:Class . }";
        List<String> classes;
        if (remoteSparqlEndpoint != null) {
        		classes = SPARQLUtilsRemote.execRemoteQuery(queryStringClasses, remoteSparqlEndpoint);
        }
        else {
        		classes = SPARQLUtilsLocal.execQuery(queryStringClasses, rdfModel);
        }
        
        /*for (String classs  : classes){
        		logger.info(classs);
        }*/
        if (classes.size() == 0 ){
        		//if we don't have the ontology, but only the instances, 
        		//then infer the classes from the instances:
        		if(remoteSparqlEndpoint != null)
        			classes = getRemoteClasses(remoteSparqlEndpoint);
        		else
        			classes = getClasses(rdfModel);
        }
        
		// 2. iterate through instances and add edge between classes C1 and C2
		// in the summary wherever there
		// is an edge between an instance of C1 and an instance of C2
		
        // query for a instances of classes 
        // get all properties for instances of other classes
        
        //TODO: optimize this (make it non-quadratic)
        List<String> toSearch = new ArrayList<String>();
        toSearch.addAll(classes);
        
        for(String classs: toSearch) {
        	List<String> res2;
        	HashSet<String> props2;
        	// 1. is this a subclass of some other classes? (RDF allows multiple inheritance)
        	String queryStringInheritance = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
            		"SELECT ?class WHERE { " + classs + " rdfs:subClassOf ?class  ."
            		+ "}";
    		
    		if(remoteSparqlEndpoint != null)
    			res2 = SPARQLUtilsRemote.execRemoteQuery(queryStringInheritance, remoteSparqlEndpoint);

    		else
    			res2 = SPARQLUtilsLocal.execQuery(queryStringInheritance, rdfModel);
    		 //add properties to hashset, this also removes duplicates
    		 props2 = new HashSet<>(res2);
    		 
    		 if(props2.size() != 0) {
    			 HashSet<String> subClassProp = new HashSet<>();
    			 subClassProp.add("subClassOf");
    			 for (String superClass : props2) {
	    			 summaryGraph.addVertex(classs);
	    			 summaryGraph.addVertex(superClass);
	    			 summaryGraph.addEdge(classs, superClass, 
	    					 new SummaryEdge(
	    							 new SummaryVertex(classs, VertexType.CLASS),
	    							 new SummaryVertex(superClass, VertexType.CLASS),
	    							 subClassProp));
    			 }
    		 }
    		 
    		 // e.g. uri_capital isCityOf uri_state --- this is a property of the parent
    		 
    		 // Find list of properties you can query here: https://www.slideshare.net/olafhartig/an-introduction-to-rdf-and-the-web-of-data
    		 // slide 19
    		 // TODO: add properties from data structure (subClassOf), object properties
    		
    		 
    		 String queryStringProps = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
             		"SELECT distinct ?prop WHERE { ?prop " + " a rdf:Property  ."
             		+ "}";
    		 
    		 if(remoteSparqlEndpoint != null)
    			 res2 = SPARQLUtilsRemote.execRemoteQuery(queryStringProps, remoteSparqlEndpoint);
    		 else
    			 res2 = SPARQLUtilsLocal.execQuery(queryStringProps, rdfModel);
    		 //add properties to hashset, this also removes duplicates
    		 props2 = new HashSet<>(res2);
    		 
    		 // TODO: don't add props of parents as links between children and other classes
    		 // e.g. uri_capital isCityOf uri_state --- this is a property of the parent
    		 
    		 // Find list of properties you can query here: https://www.slideshare.net/olafhartig/an-introduction-to-rdf-and-the-web-of-data
    		 // slide 19
    		 // TODO: add properties from data structure (subClassOf), object properties
    		 
    		 if (props2.size() != 0) {
    			 logger.info("CLASS PROPS:"+ res2);
    		 }
    		 
        	for (String otherClass : toSearch){
        		//if(otherClass.equals(classs))
        		//	continue;
        		 
        		// get properties between all instances
        		String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
                		"SELECT distinct ?prop WHERE { ?inst a "+ classs + " .\n"
                		+ "?inst ?prop ?inst2" + " .\n"
                		+ "?inst2 a " + otherClass +  " ."
                		+ "}";
        		
        		List<String> res;
        		if (remoteSparqlEndpoint != null)
        			res = SPARQLUtilsRemote.execRemoteQuery(queryString, remoteSparqlEndpoint);
        		else
        		    res = SPARQLUtilsLocal.execQuery(queryString, rdfModel);
        		 //add properties to hashset, this also removes duplicates
        		 HashSet<String> props = new HashSet<>(res);
        		 
        		 // TODO: don't add props of parents as links between children and other classes
        		 // e.g. uri_capital isCityOf uri_state --- this is a property of the parent
        		 
        		 // TODO: add properties from data structure (subClassOf), object properties
        		 
        		 if(props.size() != 0) {
        			 for(String prop: props){
        				 HashSet<String> labels = new HashSet<String>();
        				 labels.add(prop);
        			 summaryGraph.addVertex(classs);
        			 summaryGraph.addVertex(otherClass);
        			 try {
        			 summaryGraph.addEdge(classs, otherClass, 
        					 new SummaryEdge(
	    							 new SummaryVertex(classs, VertexType.CLASS),
	    							 new SummaryVertex(otherClass, VertexType.CLASS),labels));
        			 } catch(Exception e) {
        				 logger.warn(e.toString());
        				 continue;
        			 }
        			 
        			 //TODO: figure out if this is useful?
        			/* HashSet<String> labels2 = new HashSet<String>();
        			 labels2.add(classs);
        			 labels2.add(prop);
        			 summaryGraph.addVertex(classs);
        			 summaryGraph.addVertex(prop);
        			 summaryGraph.addEdge(classs, prop, 
        					 new SummaryEdge(
	    							 new SummaryVertex(classs, VertexType.CLASS),
	    							 new SummaryVertex(prop, VertexType.PROPERTY),labels2));
        			 
        			 HashSet<String> labels3 = new HashSet<String>();
        			 labels2.add(otherClass);
        			 labels2.add(prop);
        			 summaryGraph.addVertex(prop);
        			 summaryGraph.addVertex(otherClass);
        			 summaryGraph.addEdge(prop, otherClass, 
        					 new SummaryEdge(
	    							 new SummaryVertex(prop, VertexType.PROPERTY),
	    							 new SummaryVertex(otherClass, VertexType.CLASS),labels3));
	    							 */
        			 }
        		 }
        		 
        		 // add datatype properties
        		 // SHOULD ONLY ADD FOR THOSE PROPS THAT HAVE BOTH A DOMAIN AND A RANGE
        		 // the edge is between the domain and the range.
        		 // arguably, we do this above.
        		 /*if(withProps) {
	        		 String datatypequeryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
	        		 "select distinct ?prop where { " +
	        			  " ?prop <http://www.w3.org/2000/01/rdf-schema#domain> " +
	        			                           classs + " } "; 
	        		 
	        		 List<String> res3;
	        		 if(remoteSparqlEndpoint != null)
	        			 res3 = SPARQLUtils.execRemoteQuery(datatypequeryString, remoteSparqlEndpoint);
	        		 else
	        			 res3 = SPARQLUtils.execQuery(datatypequeryString, rdfModel);
	        		 //add properties to hashset, this also removes duplicates
	        		 HashSet<String> props3 = new HashSet<>(res3);
	        		 
	        		 // TODO: don't add props of parents as links between children and other classes
	        		 // e.g. uri_capital isCityOf uri_state --- this is a property of the parent
	        		 
	        		 // TODO: add properties from data structure (subClassOf), object properties
	        		 
	        		 if (props3.size() != 0) {
	        			 for (String prop : props3){
	        				 HashSet<String> p = new HashSet();
	        				 p.add(prop);
	        				 summaryGraph.addVertex(classs);
		        			 summaryGraph.addVertex(prop);
		        			 summaryGraph.addEdge(classs, prop, 
		        					 new SummaryEdge(
			    							 new SummaryVertex(classs, VertexType.CLASS),
			    							 new SummaryVertex(prop, VertexType.PROPERTY),
			    							 p));
	        			 }
	        		 }
        		 }
        		 */
        		 // add annotation properties
        		 String annotationPropertyClass = "<http://www.w3.org/2002/07/owl#AnnotationProperty>"; 
        		 String getLiteralPropsQueryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
        				 prolog4 + "\n" +
    	        		 "select distinct ?prop where { " +
    	        			  " ?x a " + classs + ". "+
    	        			  " ?x ?prop ?y ."+
    	        			  " ?prop a owl:AnnotationProperty . " +
    	        			  " } "; 
    	        		 
    	        		 List<String> res3;
    	        		 if(remoteSparqlEndpoint != null)
    	        			 res3 = SPARQLUtilsRemote.execRemoteQuery(getLiteralPropsQueryString, remoteSparqlEndpoint);
    	        		 else
    	        			 res3 = SPARQLUtilsLocal.execQuery(getLiteralPropsQueryString, rdfModel);
    	        		 //add properties to hashset, this also removes duplicates
    	        		 HashSet<String> props3 = new HashSet<>(res3);
    	        		 
    	        		 // TODO: don't add props of parents as links between children and other classes
    	        		 // e.g. uri_capital isCityOf uri_state --- this is a property of the parent
    	        		 
    	        		 // TODO: add properties from data structure (subClassOf), object properties
    	        		 
    	        		 if (props3.size() != 0) {
    	        			 for (String prop : props3){
    	        				 HashSet<String> p = new HashSet();
    	        				 p.add(prop);
    	        				 summaryGraph.addVertex(classs);
    		        			 summaryGraph.addVertex(annotationPropertyClass);
    		        			 summaryGraph.addEdge(classs, annotationPropertyClass, 
    		        					 new SummaryEdge(
    			    							 new SummaryVertex(classs, VertexType.CLASS),
    			    							 new SummaryVertex(annotationPropertyClass, VertexType.CLASS),
    			    							 p));
    	        			 }
    	        		 }
    	        		 
        	} 
        		logger.info(summaryGraph.toString());
        }
	}
	
	public String getMostSpecificClass(List<String> classList) {
		// if classes in list are linked by a chain of inheritance, choose the most specific one
		// e.g. for a subClassOf b subclassOf c.... choose a

		if(classList == null || classList.size() == 0)
			return null;
		
		if(classList.size() == 1) {
			return classList.get(0);
		}

		String class1 = classList.get(0);

		for (int i = 1; i < classList.size(); i++) {

			String class2 = classList.get(i);
			HashSet<String> subClassProp = new HashSet<>();
			subClassProp.add("subClassOf");

			//a subClassOf b
			if(summaryGraph.getAllEdges(class1, class2).contains(new SummaryEdge(
					new SummaryVertex(class1, VertexType.CLASS),
					new SummaryVertex(class2, VertexType.CLASS),
					subClassProp))) {
				classList.remove(i);
				return getMostSpecificClass(classList);
			}
			//b subclassOf a
			else if(summaryGraph.getAllEdges(class2, class1).contains(new SummaryEdge(
					new SummaryVertex(class2, VertexType.CLASS),
					new SummaryVertex(class1, VertexType.CLASS),
					subClassProp))) {
				classList.remove(0);
				return getMostSpecificClass(classList);
			}
		}

		return classList.get(0);
	}

	
	public static void searchURIByName(String name, Model m) {
	    String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
	    		"PREFIX sch: <http://example-world.ch/database-schema#> " +
	    		"Select ?uri Where { " +
	            "?uri sch:Name \"" + name + "\" . " +
	            " }";

	    Query query = QueryFactory.create(queryString);

	    QueryExecution qe = QueryExecutionFactory.create(query, m);
	    ResultSet results =  qe.execSelect();

	    while(results.hasNext() ) {

	       QuerySolution querySolution = results.next();
	       String uriNode = querySolution.get("uri").toString();
	       logger.info("ANSWER is "+ uriNode + "\n\n" );
	       String queryType = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		    		"PREFIX sch: <http://example-world.ch/database-schema#> " +
		    		"Select ?type Where { " +
		            "<" + uriNode + "> a ?type ." +
		            " }";
	       
	       Query query2 = QueryFactory.create(queryType);

		    QueryExecution qe2 = QueryExecutionFactory.create(query2, m);
		    ResultSet results2 =  qe2.execSelect();

		    while(results2.hasNext() ) {

		       QuerySolution querySolution2 = results2.next();
		       String uriNode2 = querySolution2.get("type").toString();
		       logger.info("ANSWER is "+ uriNode2 + "\n\n" );
		    }
		    qe2.close();
		    
		    String queryProps = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		    		"PREFIX sch: <http://example-world.ch/database-schema#> " +
		    		"Select ?subj ?prop Where { " +
		            "?subj ?prop " + "<" + uriNode + "> ." +
		            " }";
		    Query query3 = QueryFactory.create(queryProps);

		    QueryExecution qe3 = QueryExecutionFactory.create(query3, m);
		    ResultSet results3 =  qe3.execSelect();

		    while(results3.hasNext() ) {

		       QuerySolution querySolution3 = results3.next();
		       String uriSubj = querySolution3.get("subj").toString();
		       String uriProp = querySolution3.get("prop").toString();
		       logger.info("ANSWER is "+ uriSubj +  " "+ uriProp + "\n\n" );
		    }
		    
		    qe2.close();

	    }
	    
	    qe.close();  
	}
	
	public static void main(String[] args){
		//final Model m = ModelFactory.createDefaultModel();
        // use the file manager to read an RDF document into the model
        //FileManager.get().readModel(m, args[0]);
        
		long start = System.currentTimeMillis();
        //final Model m = ModelFactory.createDefaultModel();
        // use the file manager to read an RDF document into the model
        // testing out with DBpedia with 6 million entries:
		
        long end = System.currentTimeMillis();
        
        //logger.info("TIME "+ (end - start));
        
        //logger.info("We have loaded a model with no. statements = " + m.size());
        
        //FileManager.get().readModel(m, args[0]);
        
        // OMA: https://sparql.omabrowser.org/sparql
        // local repo: http://localhost:8080/rdf4j-server/repositories/1
        //execQuery(args[0]);
        SummaryRDFGraph g = new SummaryRDFGraph(args[0], true, args[1].equals("file"));

		//testJGraphT();
	}

}