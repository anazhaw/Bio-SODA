package ch.ethz.semdwhsearch.prototyp1.classification;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;

/**
 * Lookup functionality for matches.
 * 
 * @author Lukas Blunschi
 * 
 */
public class MatchLookup {

	private final Metadata metadata;

	private final Match match;

	public MatchLookup(Metadata metadata, Match match) {
		this.metadata = metadata;
		this.match = match;
	}

	public List<String> getMatchedUris() {
		List<String> uris = new ArrayList<String>();

		// loop over all terms
		for (Term term : match.getTerms()) {
			uris.addAll(new TermLookup(metadata, term).getUri());
		}

		return uris;
	}

}
