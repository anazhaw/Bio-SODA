package ch.ethz.semdwhsearch.prototyp1.classification.terms;

import java.util.Comparator;
import ch.ethz.semdwhsearch.prototyp1.tools.Tokenizer;

/**
 * A comparator for terms using minimal Page Rank scoring.
 * 
 * @author Ana Sima
 * 
 */
public class PageRankTermComparator implements Comparator<Term> {
	Tokenizer tokenizer = new Tokenizer();

	public int compare(Term t1, Term t2) {
		int result = 0;
		// compare by type
		result = t1.type.compareTo(t2.type);
		if (result == 0) {

			//this should always hold 
			// for this comparator, order by page rank score instead of lexicographically
			//if(t1.key.equals(t2.key)) {
				
				/*if(t1.originName.equals(t2.originName))
					return 0;
				 if((t1.key.length()/(double)t1.label.length()) > (t2.key.length()/(double)t2.label.length()))
                                        return -1;
                                return 1;
                                */
				if(t1.pageRank > t2.pageRank)
					return -1;
				else if (t1.pageRank == t2.pageRank)
					return 0;
				return 1;
		}

		return result;
	}
	
	

}
