package ch.ethz.html5.dag;

/**
 * A struct to hold a key and a value.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Parameter {

	public String key;

	public String value;

	public Parameter(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public Parameter copy() {
		return new Parameter(key, value);
	}

	// ------------------------------------------------------- object overrides

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Parameter) {
			Parameter parameter = (Parameter) obj;
			// TODO that is not very nice
			// we do this only because of Derby!
			return parameter.key.equalsIgnoreCase(key) && parameter.value.equalsIgnoreCase(value);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return key + "=" + value;
	}

}
