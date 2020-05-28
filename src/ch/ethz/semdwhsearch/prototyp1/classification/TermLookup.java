package ch.ethz.semdwhsearch.prototyp1.classification;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermOrigin;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.TermType;
import ch.ethz.semdwhsearch.prototyp1.metadata.BasedataNode;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.metadata.ModelInfo;

/**
 * Lookup functionality for terms.
 * 
 * @author Lukas Blunschi
 * 
 */
public class TermLookup {

	private final Metadata metadata;

	private final Term term;

	public TermLookup(Metadata metadata, Term term) {
		this.metadata = metadata;
		this.term = term;
	}

	/**
	 * Get URI(s) of given term.
	 * 
	 * @return URIs (maybe empty, but never null).
	 */
	public List<String> getUri() {
		List<String> uris = new ArrayList<String>();
		// switch on term type
		if (term.type == TermType.RDF_MATCH && term.origin == TermOrigin.DOMAIN_ONTOLOGY) {

			//the originNameId has the URI, get it from second table
			String schemaName = term.originName;
			uris.add(schemaName);

		} else if (term.type == TermType.VALUE && term.origin == TermOrigin.SPARQL) {
			// same as for DBpedia!
			// find node in domain ontology
			int pos = term.originName.indexOf(".");
			String modelName = term.originName.substring(0, pos);
			ModelInfo info = metadata.getModelInfo(Metadata.TYPE_DO, modelName);
			uris = info.getUriByLiteral(term.value);
		} 

		return uris;
	}

}
