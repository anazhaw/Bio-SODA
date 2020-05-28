package ch.ethz.semdwhsearch.prototyp1.classification.terms;

import java.util.Comparator;

/**
 * A generic comparator for terms.
 * 
 * @author Lukas Blunschi
 * 
 */
public class TermComparator implements Comparator<Term> {

	public int compare(Term t1, Term t2) {
		int result = 0;

		// compare by type
		result = t1.type.compareTo(t2.type);
		if (result == 0) {

			// compare by value
			result = t1.value.compareTo(t2.value);
			if (result == 0) {

				// conmpare by origin
				result = t1.origin.compareTo(t2.origin);
				if (result == 0) {

					// compare by origin name
					result = t1.originName.compareTo(t2.originName);
				}
			}
		}

		return result;
	}

}
