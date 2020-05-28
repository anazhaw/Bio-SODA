package ch.ethz.rdf;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;


/**
 * Extended Resource class.
 * 
 * @author Lukas Blunschi
 * 
 */
public class EResource {

	private final Resource resource;

	private final Model model;

	public EResource(Resource resource) {
		this.resource = resource;
		this.model = resource.getModel();
	}

	// ----------------------------------------------- package level operations

	Resource getResource() {
		return resource;
	}

	// ------------------------------------------------------ access operations

	public String getURI() {
		return resource.getURI();
	}

	public List<Statement> listStatements() {
		return listStatements(null);
	}

	public List<Statement> listStatements(Property property) {
		return getStatements(new SimpleSelector(resource, property, (Object) null));
	}

	public List<Statement> listStatementsIncoming() {
		return listStatementsIncoming(null);
	}

	public List<Statement> listStatementsIncoming(Property property) {
		return getStatements(new SimpleSelector(null, property, resource));
	}

	private List<Statement> getStatements(Selector selector) {
		List<Statement> result = new ArrayList<Statement>();
		StmtIterator iter = model.listStatements(selector);
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			result.add(stmt);
		}
		return result;
	}

	/**
	 * List subjects where this resource is the object.
	 * 
	 * @return list of statements.
	 */
	public List<Statement> listSubjects(Property property) {
		List<Statement> result = new ArrayList<Statement>();
		Selector selector = new SimpleSelector(null, property, (RDFNode) resource);
		StmtIterator iter = model.listStatements(selector);
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			result.add(stmt);
		}
		return result;
	}

	/**
	 * Get name statement for given resource.
	 * 
	 * @param resource
	 * @return name statement or null if not available.
	 */
	public Statement getNameIfAvailable() {

		// loop over all statements associated with given resource
		for (Statement stmt : listStatements()) {

			// look for literals
			RDFNode obj = stmt.getObject();
			if (obj.isLiteral()) {

				// look for name property
				Property prop = stmt.getPredicate();
				String localName = prop.getLocalName();
				if (localName.toLowerCase().equals("name")) {
					return stmt;
				}
			}
		}

		// otherwise return null
		return null;
	}

	public String getLocalName() {
		return resource.getLocalName();
	}

	public Statement addStatement(Property prop, String object) {
		return model.createStatement(resource, prop, object);
	}

	// ------------------------------------------------------- object overrides

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EResource) {
			EResource eResource = (EResource) obj;
			return this.resource.getURI().equals(eResource.resource.getURI());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return resource.getURI().hashCode();
	}

}
