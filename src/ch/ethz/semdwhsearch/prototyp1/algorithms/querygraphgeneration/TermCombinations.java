package ch.ethz.semdwhsearch.prototyp1.algorithms.querygraphgeneration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.semdwhsearch.prototyp1.classification.Match;
import ch.ethz.semdwhsearch.prototyp1.classification.terms.Term;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;

/**
 * Compute term combinations.
 * 
 * @author Lukas Blunschi
 * 
 */
public class TermCombinations {

	private final static Logger logger = LoggerFactory.getLogger(TermCombinations.class);

	public List<LinkedList<Term>> getTermCombinations(List<Match> matches) {
		return getTermCombinationsRecursively(matches, 0);
	}

	private List<LinkedList<Term>> getTermCombinationsRecursively(List<Match> matches, int matchIndex) {
		// result
		List<LinkedList<Term>> combinations = new ArrayList<LinkedList<Term>>();

		// switch on match index
		
		// here, take BEST MATCH for first term, then BEST match for second term, then etc
		// then, second best for first term, second best for next etc
		
		//put all terms from all matches in 1 big term list per KW, sort inside list by page rank and then take in order
		HashMap<String, LinkedList<Term>> termsPerKw = new HashMap<String, LinkedList<Term>>();
		for(Match match: matches) {
			for(Term t: match.getTerms()) {
				LinkedList<Term> listForKW = termsPerKw.get(t.key);
				if(listForKW == null) {
					listForKW = new LinkedList<Term>();
				}
				listForKW.add(t);
				termsPerKw.put(t.key, listForKW);
			}
		}
		
		//sort lists by pageRank
		for(String kw: termsPerKw.keySet()) {
			LinkedList<Term> listForKW = termsPerKw.get(kw);
			listForKW.sort(new Comparator<Term>() {

				@Override
				public int compare(Term o1, Term o2) {
					// TODO Auto-generated method stub
					if(o1.pageRank > o2.pageRank)
						return -1;
					return 1;
				}
			});
		}
		
		LinkedList<Term> combination = new LinkedList<Term>();
		
		int index = 0;
		boolean changed = true;
		//get combinations until MAX_TERM_COMB
		while(combinations.size() < Constants.MAX_TERM_COMBINATIONS && changed) {
			changed = false;
			for(String kw: termsPerKw.keySet()) {
				LinkedList<Term> listForKW = termsPerKw.get(kw);
				if(listForKW.size() > index) {
					//take i-th element from each sorted list (per kw)
					combination.add(listForKW.get(index));
					changed = true;
				}
			}
			combinations.add(combination);
			index ++;
		}

		return combinations;
	}

}
