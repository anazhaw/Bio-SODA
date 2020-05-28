package ch.ethz.semdwhsearch.prototyp1.classification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.semdwhsearch.prototyp1.classification.terms.IRTermComparator;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.PageRankTermComparator;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;

/**
 * A match can hold a list of term occurrences.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Match {

	private List<Term> terms;

	// ----------------------------------------------------------- construction

	public Match(Term term) {
		this.terms = new ArrayList<Term>();
		terms.add(term);
	}

	public Match(List<Term> terms) {
		this.terms = terms;
		Collections.sort(terms, new IRTermComparator());
	}

	// ---------------------------------------------------------- functionality

	public void addTerm(Term term) {
		if(!this.terms.contains(term)) {
			this.terms.add(term);
			Collections.sort(terms, new IRTermComparator());
		}
	}

	public void sortByPageRank() {
		Collections.sort(terms, new PageRankTermComparator());
	}
	
	public void sortByBestMatch() {
		Collections.sort(terms, new IRTermComparator());
	}

	public void removeDuplicates() {
		Set<Term> set = new HashSet<Term>(terms);
		terms.clear();
		terms.addAll(set);
		Collections.sort(terms, new IRTermComparator());
	}
	
	public void limitResults() {
		Collections.sort(terms, new IRTermComparator());
		terms =  terms.subList(0, Math.min(terms.size(), Constants.MAX_MATCHES_COUNT));
	}

	public List<Term> getTerms() {
		return terms;
	}

	// ------------------------------------------------------- Object overrides

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Match) {
			Match match = (Match) obj;
			if (terms.size() == match.terms.size()) {
				for (int i = 0; i < terms.size(); i++) {
					if (!terms.get(i).equals(match.terms.get(i))) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuffer txt = new StringBuffer();
		for (int i = 0; i < terms.size(); i++) {
			if (i > 0) {
				txt.append("; ");
			}
			txt.append(terms.get(i).toString());
		}
		return txt.toString();
	}

	// ----------------------------------------------------- additional methods

	public String toHtml() {
		StringBuffer html = new StringBuffer();
		int displayCount = Constants.DISPLAY_COUNT;
		int termCount = terms.size();
		for (int i = 0; i < termCount && i < displayCount; i++) {
			if (i > 0) {
				html.append("<br/>");
			}
			if(terms.get(i) != null) {
				//TODO: MAKE HERE A LINK TO INTERNAL LOOKUP!
				html.append(terms.get(i).toClickableHtml());
			}
		}
		if (termCount > displayCount) {
			html.append("<br/>").append("... " + (termCount - displayCount) + " more result");
		}
		return html.toString();
	}

}
