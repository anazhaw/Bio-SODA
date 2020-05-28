package ch.ethz.rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.tools.StopWatch;


/**
 * Extended Model class.
 * 
 * @author Lukas Blunschi
 * 
 */
public class EModel {

	private static final Logger logger = LoggerFactory.getLogger(EModel.class);

	private final Model model;

	private final String filename;

	// ----------------------------------------------------------- construction

	public EModel() {
		this(ModelFactory.createDefaultModel(), null);
	}

	public EModel(Model model) {
		this(model, null);
	}

	public EModel(Model model, String filename) {
		this.model = model;
		this.filename = filename;
	}

	// ------------------------------------------------------ access operations

	public Model getModel() {
		return model;
	}

	public String getFilename() {
		return filename;
	}

	public String getFilenameNoExt() {
		if (filename.endsWith(".rdf")) {
			return filename.substring(0, filename.length() - 4);
		} else {
			return filename;
		}
	}

	public String getModelName() {
		return getFilenameNoExt();
	}

	public Set<String> getAllUris() {
		Set<String> uris = new HashSet<String>();

		// loop over all statements
		int countAnonSubjects = 0;
		int countAnonObjects = 0;
		StmtIterator iter = model.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.next();

			// get triple data
			Resource subject = stmt.getSubject();
			RDFNode object = stmt.getObject();

			// URI from subject
			if (subject.isAnon()) {
				countAnonSubjects++;
			} else {
				uris.add(subject.getURI());
			}

			// URI from object (if it is a resource)
			if (object.isResource()) {
				if (object.isAnon()) {
					countAnonObjects++;
				} else {
					Resource objectR = object.as(Resource.class);
					uris.add(objectR.getURI());
				}
			}
		}
		logger.info("#anon subjects = " + countAnonSubjects + ", #anon objects = " + countAnonObjects);
		return uris;
	}

	public String getCommonUriPrefix() {

		// get URIs
		List<String> uris = new ArrayList<String>(getAllUris());
		if (uris.size() == 0) {
			return "";
		}

		// use first URI to compute max result
		String firstUri = uris.get(0);
		int length = firstUri.length();
		String[] chars = new String[length];
		for (int i = 0; i < length; i++) {
			chars[i] = firstUri.substring(i, i + 1);
		}

		// loop over other URIs
		for (int i = 1; i < uris.size(); i++) {
			String curUri = uris.get(i);
			if (curUri.length() < length) {
				length = curUri.length();
			}
			for (int pos = 0; pos < length; pos++) {
				if (!curUri.substring(pos, pos + 1).equals(chars[pos])) {
					length = pos;
					break;
				}
			}
		}
		return firstUri.substring(0, length);
	}

	public Resource getResource(String uri) {
		return model.getResource(uri);
	}

	public EResource getEResource(String uri) {
		return new EResource(model.getResource(uri));
	}

	public EResource getResource(Property predicate, String object) {

		// result
		Resource subject = null;

		// loop over all statements that fullfill the given criterias
		StmtIterator iter = model.listStatements(null, predicate, object);
		while (iter.hasNext()) {
			Statement stmt = iter.next();
			subject = stmt.getSubject();
		}

		// ensure only one such resource exists
		if (subject != null && iter.hasNext()) {
			throw new RuntimeException("multiple resources exist for predicate '" + predicate + "' and object '" + object
					+ "'.");
		}

		return new EResource(subject);
	}

	/**
	 * Get resource which contains the given literal.
	 * 
	 * @param literal
	 * @return resource or null if literal not found.
	 */
	public EResource getResourceByLiteral(String literal) {

		// loop over all statements
		// TODO can this be optimized?
		StmtIterator iter = model.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.next();

			// test if current object resource matches given literal
			RDFNode obj = stmt.getObject();
			if (obj.isLiteral()) {
				if (obj.toString().equals(literal)) {

					// literal found - return the subject resource
					return new EResource(stmt.getSubject());
				}
			}
		}

		// bad luck - literal not found - return null
		return null;
	}

	public List<EResource> getEntryPoints() {
		StopWatch watch = new StopWatch("°");

		// Graph graph = model.getGraph();
		// Capabilities caps = graph.getCapabilities();
		// Reifier reifier = graph.getReifier();
		// QueryHandler queryhandler = graph.queryHandler();

		// Note: I was trying VERY hard, but I didn't manage to write an
		// intersect in SPARQL 1.0.
		// Therefore, here it is in Java

		// collect subject and object URIs
		Set<String> subjectUris = new HashSet<String>();
		Set<String> objectUris = new HashSet<String>();

		// loop over all statements
		StmtIterator iterStmts = model.listStatements();
		while (iterStmts.hasNext()) {
			Statement stmt = iterStmts.next();
			Resource subject = stmt.getSubject();
			RDFNode object = stmt.getObject();

			// only use statement where both ends are resources
			if (object.isResource()) {
				Resource objectRes = object.as(Resource.class);

				// ignore statements where at least one side is null
				String uriSubject = subject.getURI();
				String uriObject = objectRes.getURI();
				if (uriSubject != null && uriObject != null) {
					subjectUris.add(uriSubject);
					objectUris.add(uriObject);
				}
			}
		}

		// entry points = subjects URIs minus object URIs
		subjectUris.removeAll(objectUris);
		List<EResource> entryPoints = new ArrayList<EResource>();
		for (String uri : subjectUris) {
			Resource resource = model.getResource(uri);
			entryPoints.add(new EResource(resource));
		}

		// report duration
		watch.stopAndReport("Computing entry points");

		return entryPoints;
	}

	public List<String> getAllLiterals(Property property) {

		// loop over all statments of given property
		List<String> literals = new ArrayList<String>();
		StmtIterator iter = model.listStatements(null, property, (String) null);
		while (iter.hasNext()) {
			literals.add(iter.nextStatement().getObject().toString());
		}
		return literals;
	}

	// ------------------------------------------------------ modify operations

	public void addStatement(Statement stmt) {
		model.add(stmt);
	}

	public void addStatement(EResource subject, Property predicate, EResource object) {
		model.add(subject.getResource(), predicate, object.getResource());
	}

	public void addStatement(EResource subject, Property predicate, String object) {
		model.add(subject.getResource(), predicate, object);
	}

	// ------------------------------------------------------ create operations

	public EResource createResource(String uri) {
		return new EResource(model.createResource(uri));
	}

	// -------------------------------------------------------------- rendering

	public String toNTriples(boolean asHtml) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		model.write(out, "N-TRIPLES");
		String result = out.toString();
		if (asHtml) {
			result = result.trim().replaceAll("\\n", "<br />\n");
		}
		return result;
	}

}
