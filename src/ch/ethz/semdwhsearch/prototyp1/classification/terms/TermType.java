package ch.ethz.semdwhsearch.prototyp1.classification.terms;

/**
 * Possible term types.
 * 
 * @author Lukas Blunschi, Ana Sima
 * 
 */
public enum TermType {

	RDF_MATCH(1), VALUE(2), ADJECTIVE(3), UNKNOWN(4);

	private final int type;

	private TermType(int type) {
		this.type = type;
	}

	public int toInt() {
		return type;
	}

	public static TermType fromInt(int type) {
		if (type == 1) {
			return  RDF_MATCH;
		} else if (type == 2) {
			return TermType.VALUE;
		} else if(type == 3){
			return TermType.ADJECTIVE;
		} else {
			return UNKNOWN;
		}
	}

}
