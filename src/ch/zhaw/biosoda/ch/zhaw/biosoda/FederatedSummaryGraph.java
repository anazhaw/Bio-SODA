package ch.zhaw.biosoda;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.EnumeratedClass;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.SomeValuesFromRestriction;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import org.jgrapht.Graph;
import org.jgrapht.graph.Multigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.algorithms.queryclassification.LongestMatchClassify;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.metadata.MetadataSingleton;

/**
 * This class is used to represent a summary graph
 * This will be augmented with URIs that match keywords
 * in order to build a query graph and construct Sparql queries
 * See paper: http://dl.acm.org/citation.cfm?id=2810357
 * 
 *
 * @author Ana Sima
 */
public class FederatedSummaryGraph implements Serializable {
	static final long serialVersionUID = 1L;

	// the list of remote endpoints which will be used to build the summary graph
	List<String> URLs = new ArrayList<String>();
	
	private final static Logger logger =
			LoggerFactory.getLogger(FederatedSummaryGraph.class);

	// the summary graph model (JGraphT implementation)
	Graph<String, SummaryEdge> summaryGraph =
			new Multigraph<String, SummaryEdge>(SummaryEdge.class);

	//TODO: move this to a constants file (dictionary of prefix + URI)
	final static String prolog1 = "PREFIX rdfs: <" + RDFS.getURI() + ">";
	final static String prolog2 = "PREFIX rdf: <" + RDF.getURI() + ">";
	final static String prolog3 = "PREFIX owl: <"+ OWL.getURI() + ">";
	final static String prolog4 = "PREFIX xsd: <" + XSD.getURI() + ">";

	// this will get classes from an individual SPARQL endpoint
	public List<String> getRemoteClasses(String remoteSparqlEndpoint) {
		// 1. get all classes in the ontology

		final String queryStringClasses = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT DISTINCT ?class WHERE { { ?x a ?class . } UNION { ?class a owl:Class . } }";
		return SPARQLUtilsRemote.execRemoteQuery(queryStringClasses, remoteSparqlEndpoint);

	}

	public String getMostSpecificClass(List<String> classList) {
		// if classes in list are linked by a chain of inheritance, choose the most specific one
		// e.g. for a subClassOf b subclassOf c.... choose a
		if(classList == null || classList.size() == 0)
			return null;
		
		if(classList.size() == 1) {
			return classList.get(0);
		}

		String subClass = classList.get(0);

		List<String> markForRemoval = new ArrayList<String>();
		boolean changed = true;
		while (changed) {
			changed = false;
			for(int i = 0; i < classList.size(); i++) {
				String potentialSuperClass = classList.get(i);
				if(subClass.equals(potentialSuperClass) || markForRemoval.contains(subClass) || 
						markForRemoval.contains(potentialSuperClass))
					continue;
				HashSet<String> subClassProp = new HashSet<>();
				subClassProp.add("subClassOf");
	
				Set<SummaryEdge> directEdges = summaryGraph.getAllEdges(subClass, potentialSuperClass);
				/*logger.info("Between "+ subClass + " and "+ potentialSuperClass);
				logger.info("Direct edges: "+ directEdges);*/
				Set<SummaryEdge> indirectEdges = summaryGraph.getAllEdges(potentialSuperClass, subClass);
				//logger.info("Indirect edges: "+ indirectEdges);
				
				// a subClassOf b
				if(directEdges != null
						&& directEdges.contains(new SummaryEdge(
						new SummaryVertex(subClass, VertexType.CLASS),
						new SummaryVertex(potentialSuperClass, VertexType.CLASS),
						subClassProp))) {

					markForRemoval.add(potentialSuperClass);
					changed = true;
				}

				// b subClassOf a
				else if (indirectEdges != null
						&& indirectEdges.contains(new SummaryEdge(
						new SummaryVertex(potentialSuperClass, VertexType.CLASS),
						new SummaryVertex(subClass, VertexType.CLASS),
						subClassProp))) {
					
					markForRemoval.add(subClass);
					subClass = potentialSuperClass;
					changed = true;
				}
			}
		}

		//logger.info("Returning "+ subClass);
		return subClass;
	}

	
	/*
	 * Takes a list of endpoints and/or local files for the ontology and creates the federated summary graph
	 */
	public FederatedSummaryGraph(List<String> URIs) {

		// 1. get classes of all models
		for (String sparqlEndpoint: URIs) {
			logger.info("######### SEARCHING IN REPO "+ sparqlEndpoint);
			
			List<String> classes = getRemoteClasses(sparqlEndpoint);

			logger.info("Classes: "+ classes);
			List<String> toSearch = new ArrayList<String>();
			
			//TODO: remove those that are owl specific!
			for(String className : classes) {
				if(className.toLowerCase().contains("http://www.w3.org/2002/07/owl#"))
					continue;
				else
					toSearch.add(className);
			}

			// 2. get properties of model

			// 2.1 inheritance
			//toSearch.addAll(classes);

			for (String classs: toSearch) {

				List<String> subClassOf;
				HashSet<String> inheritance;

				// 1. is this a subclass of some other classes? (RDF allows multiple inheritance)
				String queryStringInheritance = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
						"SELECT ?class WHERE { " + classs + " rdfs:subClassOf ?class  ."
						+ "}";

				subClassOf = SPARQLUtilsRemote.execRemoteQuery(queryStringInheritance, sparqlEndpoint);

				logger.info("Subclasses of: "+ classs + " "+ subClassOf);
				
				
				//add properties to hashset, this also removes duplicates
				inheritance = new HashSet<>(subClassOf);
				if (inheritance.size() != 0) {
					HashSet<String> subClassProp = new HashSet<>();
					subClassProp.add("subClassOf");
					for (String superClass : inheritance) {
						if(!superClass.equals(classs)) {
							summaryGraph.addVertex(classs);
		
							summaryGraph.addVertex(superClass);
							summaryGraph.addEdge(classs, superClass, 
									new SummaryEdge(
											new SummaryVertex(classs, VertexType.CLASS),
											new SummaryVertex(superClass, VertexType.CLASS),
											subClassProp));
					}}
				}

			/*String queryStringProps = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
					"SELECT distinct ?prop WHERE { ?prop " + " a rdf:Property  ."
					+ "}";

			List<String> props = SPARQLUtils.execQuery(queryStringProps, model);
			for (String prop: props) {
				HashSet<String> labels = new HashSet<String>();
				labels.add(prop);
				//get domain class
				HashSet<String> clsDomains = SPARQLUtils.getDomainOfProperty(prop, model);
				//get range class
				HashSet<String> clsRanges = SPARQLUtils.getRangeOfProperty(prop, model);
				for(String cls: clsDomains)
					for(String range: clsRanges) {
						summaryGraph.addVertex(cls);
						summaryGraph.addVertex(range);
						summaryGraph.addEdge(classs, range, 
						new SummaryEdge(
								new SummaryVertex(cls, VertexType.CLASS),
								new SummaryVertex(range, VertexType.CLASS),labels));

			}*/

				//TODO: figure out how costly (and useful) this is
				for (String otherClass : toSearch){
					if(otherClass.equals(classs)) {
						//TODO: think about hasOrthologous and stuff
						continue;
					}
					// get properties between all instances
					String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
							"SELECT distinct ?prop WHERE { ?inst a "+ classs + " .\n"
							+ "?inst ?prop ?inst2" + " .\n"
							+ "?inst2 a " + otherClass +  " ."
							+ "}";

					List<String> res = SPARQLUtilsRemote.execRemoteQuery(queryString, sparqlEndpoint);

					//add properties to hashset, this also removes duplicates
					HashSet<String> instanceProps = new HashSet<>(res);

					if(instanceProps.size() != 0) {
						for (String prop: instanceProps){
							HashSet<String> labels = new HashSet<String>();
							labels.add(prop);
							summaryGraph.addVertex(classs);
							summaryGraph.addVertex(otherClass);
							summaryGraph.addEdge(classs, otherClass, 
									new SummaryEdge(
											new SummaryVertex(classs, VertexType.CLASS),
											new SummaryVertex(otherClass, VertexType.CLASS),labels));

						}
					}
					// add annotation properties

					// problem: this doesn't work for Ontop data.
					// TODO: should we consider that an AnnotationProperty can apply to anything and 
					// treat it as a special case?
					String annotationPropertyClass = "<http://www.w3.org/2002/07/owl#AnnotationProperty>"; 
					//if(!classs.equals(annotationPropertyClass)) {
						String getLiteralPropsQueryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
								prolog4 + "\n" +
								"select distinct ?prop where { " +
								" ?x a " + classs + ". "+
								" ?x ?prop ?y ."+
								" ?prop a owl:AnnotationProperty . " +
								" } "; 

						List<String> res3 = SPARQLUtilsRemote.execRemoteQuery(getLiteralPropsQueryString, sparqlEndpoint);
						HashSet<String> props3 = new HashSet<>(res3);


						if (props3.size() != 0) {
							for (String property : props3){
								HashSet<String> p = new HashSet<String>();
								p.add(property);
								summaryGraph.addVertex(classs);
								summaryGraph.addVertex(annotationPropertyClass);
								summaryGraph.addEdge(classs, annotationPropertyClass, 
										new SummaryEdge(
												new SummaryVertex(classs, VertexType.CLASS),
												new SummaryVertex(annotationPropertyClass, VertexType.CLASS),
												p));
							}
						}
					//}
				}
			}
		}
		
		//get labels for all classes for "pretty display"
		for(String vx : summaryGraph.vertexSet()) {
			// set somehow the label for the vertex here
			
		}

		// 3. from the VOID ontology, add the links between the models

		logger.info("SUMMARY GRAPH: "+ summaryGraph.toString());
	}
	
	public FederatedSummaryGraph(){
		final Model model = MetadataSingleton.getInstance().getMetadata().getModelInfo(Constants.MODEL_NAME).getModel();
		List<String> classes = getClasses(model);
		List<String> toSearch = new ArrayList<String>();
		
		//TODO: remove those that are owl specific!
		for(String className : classes) {
			if(className.toLowerCase().contains("http://www.w3.org/2002/07/owl#") || className.startsWith("_:"))
				continue;
			else
				toSearch.add(className);
		}

		// 2. get properties of model

		// 2.1 inheritance
		//toSearch.addAll(classes);

		for (String classs: toSearch) {

			List<String> subClassOf;
			HashSet<String> inheritance;

			// 1. is this a subclass of some other classes? (RDF allows multiple inheritance)
			String queryStringInheritance = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
					"SELECT ?class WHERE { " + classs + " rdfs:subClassOf ?class  ."
					+ "}";

			subClassOf = SPARQLUtilsLocal.execQuery(queryStringInheritance, model);
			
			//add properties to hashset, this also removes duplicates
			inheritance = new HashSet<>(subClassOf);
			if (inheritance.size() != 0) {
				HashSet<String> subClassProp = new HashSet<>();
				subClassProp.add("subClassOf");
				for (String superClass : inheritance) {
					summaryGraph.addVertex(classs);

					summaryGraph.addVertex(superClass);
					summaryGraph.addEdge(classs, superClass, 
							new SummaryEdge(
									new SummaryVertex(classs, VertexType.CLASS),
									new SummaryVertex(superClass, VertexType.CLASS),
									subClassProp));
				}
			}
			}

			// 2.2 get all remaining props
			String queryStringProps = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
					"SELECT distinct ?prop WHERE { ?prop " + " a owl:ObjectProperty  ."
					+ "}";

			List<String> props = SPARQLUtilsLocal.execQuery(queryStringProps, model);
			for (String prop: props) {
				HashSet<String> labels = new HashSet<String>();
				labels.add(prop);
				//get domain class
				HashSet<String> clsDomains = SPARQLUtilsLocal.getDomainOfProperty(prop);
				//get range class
				HashSet<String> clsRanges = SPARQLUtilsLocal.getRangeOfProperty(prop);
				for(String cls: clsDomains)
					for(String range: clsRanges) {
						if(range.equals(cls))
							continue;
						logger.info(" ADDING EDGE BETWEEN " + cls + "  AND " + range);
						summaryGraph.addVertex(cls);
						summaryGraph.addVertex(range);
						summaryGraph.addEdge(cls, range, 
						new SummaryEdge(
								new SummaryVertex(cls, VertexType.CLASS),
								new SummaryVertex(range, VertexType.CLASS),labels));

			}
			}
			//TODO: figure out how costly (and useful) this is
			/*for (String otherClass : toSearch){
				if(otherClass.equals(classs)) {
					//TODO: think about hasOrthologous and stuff
					continue;
				}
				// get properties between all instances
				String queryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
						"SELECT distinct ?prop WHERE { ?inst a "+ classs + " .\n"
						+ "?inst ?prop ?inst2" + " .\n"
						+ "?inst2 a " + otherClass +  " ."
						+ "}";

				List<String> res = SPARQLUtils.execQuery(queryString, model);

				//add properties to hashset, this also removes duplicates
				HashSet<String> instanceProps = new HashSet<>(res);

				if(instanceProps.size() != 0) {
					for (String prop: instanceProps){
						HashSet<String> labels = new HashSet<String>();
						labels.add(prop);
						summaryGraph.addVertex(classs);
						summaryGraph.addVertex(otherClass);
						summaryGraph.addEdge(classs, otherClass, 
								new SummaryEdge(
										new SummaryVertex(classs, VertexType.CLASS),
										new SummaryVertex(otherClass, VertexType.CLASS),labels));

					}
				}
				// add annotation properties

				// problem: this doesn't work for Ontop data.
				// TODO: should we consider that an AnnotationProperty can apply to anything and 
				// treat it as a special case?
				String annotationPropertyClass = "<http://www.w3.org/2002/07/owl#AnnotationProperty>"; 
				//if(!classs.equals(annotationPropertyClass)) {
					String getLiteralPropsQueryString = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
							prolog4 + "\n" +
							"select distinct ?prop where { " +
							" ?x a " + classs + ". "+
							" ?x ?prop ?y ."+
							" ?prop a owl:AnnotationProperty . " +
							" } "; 

					List<String> res3 = SPARQLUtils.execQuery(getLiteralPropsQueryString, model);
					HashSet<String> props3 = new HashSet<>(res3);


					if (props3.size() != 0) {
						for (String property : props3){
							HashSet<String> p = new HashSet<String>();
							p.add(property);
							summaryGraph.addVertex(classs);
							summaryGraph.addVertex(annotationPropertyClass);
							summaryGraph.addEdge(classs, annotationPropertyClass, 
									new SummaryEdge(
											new SummaryVertex(classs, VertexType.CLASS),
											new SummaryVertex(annotationPropertyClass, VertexType.CLASS),
											p));
		}}}*/

		// 3. process RESTRICTIONS on classes from the ontology
		OntModel ontology = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		ontology.read( MetadataSingleton.getInstance().getMetadata().getDataDirPath() );

		for (String cls: toSearch) {
			OntClass ontcls = ontology.getOntClass(cls.replaceAll("[<>]", ""));
			if(ontcls == null)
				continue;
			//get superclass and see if is restriction
			ExtendedIterator<OntClass> superClass = ontcls.listSuperClasses();
			while( superClass.hasNext() ) {
			    final OntClass aParent = superClass.next();
			    if( aParent.isRestriction() ) {
				    	//get allvaluesFrom or someValuesFrom
				    	//add edge between cls and that
				    	Restriction r = (Restriction)aParent.asRestriction();
				    	OntProperty prop = r.getOnProperty();
					EnumeratedClass enm = null;
				    	if (aParent.canAs(AllValuesFromRestriction.class))
			        	{
			                	AllValuesFromRestriction avfr = aParent.as(AllValuesFromRestriction.class);
			                	if (avfr.getAllValuesFrom().canAs(OntClass.class))
			                	{
			                    	OntClass valueClass = avfr.getAllValuesFrom().as(OntClass.class);
						try{
					    		enm = valueClass.asClass().asEnumeratedClass();
                				} catch (Exception e)
                        		    {
						String cls2= "<"+valueClass.toString()+">";
				            	if(cls.equals(cls2))
							continue;
					    logger.info("Adding all values restriction for " + cls + " from " + cls2);
                                            HashSet<String> p = new HashSet<String>();
                                                                p.add("<"+prop.toString()+">");
                                                                summaryGraph.addVertex(cls);
                                                                summaryGraph.addVertex(cls2);
                                                                summaryGraph.addEdge(cls, cls2,
                                                                                new SummaryEdge(
                                                                                                new SummaryVertex(cls, VertexType.CLASS),
                                                                                                new SummaryVertex(cls2, VertexType.CLASS),
                                                                                                p));

                        }
                        if(enm != null){
                                ExtendedIterator<? extends OntResource> enums = enm.listOneOf();
                                while ( enums.hasNext() ) {
                                        String enumClass = "<" + enums.next().toString() + ">";
					if(cls.equals(enumClass))
						continue;
					logger.info("Adding all values restriction for " + cls + " from " + enumClass);
                                            HashSet<String> p = new HashSet<String>();
                                                                p.add(prop.toString());
                                                                summaryGraph.addVertex(cls);
                                                                summaryGraph.addVertex(enumClass);
                                                                summaryGraph.addEdge(cls, enumClass,
                                                                                new SummaryEdge(
                                                                                                new SummaryVertex(cls, VertexType.CLASS),
                                                                                                new SummaryVertex(enumClass, VertexType.CLASS),
                                                                                                p));

                        }}
                        
                }}
	                else if (aParent.canAs(SomeValuesFromRestriction.class))
	                {
	                		SomeValuesFromRestriction avfr = aParent.as(SomeValuesFromRestriction.class);
		                if (avfr.getSomeValuesFrom().canAs(OntClass.class))
		                {
		                    OntClass valueClass = avfr.getSomeValuesFrom().as(OntClass.class);
				    String cls2 = "<" + valueClass.toString() + ">";
				    if(cls.equals(cls2))
					continue;
                                    logger.info("Adding some values restriction for " + cls + " from " + valueClass);
		                    HashSet<String> p = new HashSet<String>();
							p.add("<"+prop.toString()+">");
							summaryGraph.addVertex(cls);
							summaryGraph.addVertex(cls2);
							summaryGraph.addEdge(cls, cls2, 
									new SummaryEdge(
											new SummaryVertex(cls, VertexType.CLASS),
											new SummaryVertex(cls2, VertexType.CLASS),
											p));
		                }
		            }
			    }
	        }
        }
		logger.info("RESULTING GRAPH: " + summaryGraph);
	}
	
	public List<String> getClasses(Model m) {
        // 1. get all classes in the ontology
        
        //final String queryStringClasses = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
        //		"SELECT DISTINCT ?class WHERE { ?x a ?class . }";
		final String queryStringClasses = prolog1 + "\n" + prolog2 + "\n"+ prolog3 + "\n"+
				"SELECT DISTINCT ?class WHERE { { ?x a ?class . } UNION { ?class a owl:Class . } }";
		List<String> res = SPARQLUtilsLocal.execQuery(queryStringClasses, m);
		res.remove("http://www.w3.org/2002/07/owl#Class");
		return res;

	}

	public String getSummaryGraphString(){
		String res = "";
		for (SummaryEdge edge : summaryGraph.edgeSet()){
			res += edge.toString() + "\n";
		}
		return res;
	}

	public Graph<String, SummaryEdge> getSummaryGraph(){
		return summaryGraph;
	}

	public void setSummaryGraph(Graph<String, SummaryEdge> newSG){
		this.summaryGraph = newSG;
	}

	public Model getModel() {
		return null;
	}

}
