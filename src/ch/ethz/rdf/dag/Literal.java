package ch.ethz.rdf.dag;

/**
 * An RDF literal.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Literal {

	public final String name;

	public final String value;

	public Literal(String name, String value) {
		this.name = name;
		this.value = value;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public int hashCode() {
		return name.hashCode() + value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Literal) {
			Literal literal = (Literal) obj;
			return name.equals(literal.name) && value.equals(literal.value);
		} else {
			return false;
		}
	}

}
