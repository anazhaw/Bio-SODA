package ch.ethz.rdf;

/**
 * A URI.
 * 
 * @author Lukas Blunschi
 * 
 */
public class EUri {

	public final String uri;

	public EUri(String uri) {
		this.uri = uri;
	}

	public String getLocalName() {
		int pos = uri.lastIndexOf("/");
		if (pos < 0) {
			return uri;
		} else {
			return uri.substring(pos + 1);
		}
	}

	public String getPath() {
		int pos = uri.lastIndexOf("/");
		if (pos < 0) {
			return null;
		} else {
			return uri.substring(0, pos + 1);
		}
	}

	@Override
	public String toString() {
		return uri;
	}

}
