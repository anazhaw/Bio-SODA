package ch.ethz.semdwhsearch.prototyp1.classification.terms;

/**
 * Possible match types - exact match means that only 1 instance of this class / property matches
 * filter means a that a range of instances, based on a filter on a property, are the match for this term
 * Two Terms defined with filter on same property are therefore identical.
 * 
 * @author Ana Sima
 * 
 */
public enum MatchType {

	EXACT(0), FILTER(1), UNKNOWN(-1);

	private final int type;

	private MatchType(int type) {
		this.type = type;
	}

	public int toInt() {
		return type;
	}

	public static MatchType fromInt(int type) {
		if (type == 0) {
			return EXACT;
		} else if (type == 1) {
			return FILTER;
		}
		return UNKNOWN;
	}
}

