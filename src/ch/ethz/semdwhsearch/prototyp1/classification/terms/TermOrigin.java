package ch.ethz.semdwhsearch.prototyp1.classification.terms;

/**
 * Possible term origins.
 * 
 * @author Lukas Blunschi, Ana Sima
 * 
 */
public enum TermOrigin {

	DOMAIN_ONTOLOGY(0), SPARQL(1), UNKNOWN(3);

	private final int origin;

	private TermOrigin(int origin) {
		this.origin = origin;
	}

	public int toInt() {
		return origin;
	}

	public static TermOrigin fromInt(int origin) {
		if (origin == 0) {
			return DOMAIN_ONTOLOGY;
		} else if (origin == 1) {
			return SPARQL;
		}else {
			return UNKNOWN;
		}
	}

}
