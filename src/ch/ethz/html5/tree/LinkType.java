package ch.ethz.html5.tree;

/**
 * All possible link types.
 * 
 * @author Lukas Blunschi
 * 
 */
public enum LinkType {

	REFERENCE("reference"), BASIC("basic");

	// ---------------------------------------------------------------- members

	private String label;

	/**
	 * Constructor.
	 * 
	 * @param label
	 */
	LinkType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

	// -------------------------------------------------------------- overrides

	@Override
	public String toString() {
		return this.label;
	}

	// ---------------------------------------------------------------- helpers

	public static LinkType fromString(String s) {
		for (LinkType type : LinkType.values()) {
			if (type.getLabel().equals(s)) {
				return type;
			}
		}
		return LinkType.BASIC;
	}

}
