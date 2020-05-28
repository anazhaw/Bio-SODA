package ch.ethz.semdwhsearch.prototyp1.classification.terms;

import java.util.Comparator;
import ch.ethz.semdwhsearch.prototyp1.tools.Tokenizer;

/**
 * A comparator for terms using minimal IR scoring.
 * 
 * @author Ana Sima
 * 
 */
public class IRTermComparator implements Comparator<Term> {
	Tokenizer tokenizer = new Tokenizer();

	public int compare(Term t1, Term t2) {
		int result = 0;

		// compare by type
		result = t1.type.compareTo(t2.type);
		if (result == 0) {

			//this should always hold 
			// for this comparator, order by match size instead of lexicographically
			if(t1.key.equals(t2.key)) {
				if(t1.value.equals(t2.value))
					return 0;
				// TODO: revise this
				if(t1.value.length() < t2.value.length())
					return -1;
				else if (t1.value.length() == t2.value.length())
					return 0;
				else
					return 1;
			}
		else{
			return t1.key.compareTo(t2.key);
		}
		}
		return result;
	}

}
