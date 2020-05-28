package ch.ethz.semdwhsearch.prototyp1.metadata;

/**
 * A singleton for the metadata.
 * <p>
 * To avoid having to load the metadata upon each request.
 * <p>
 * All this singleton does is holding a reference to a Metadata instance.
 * 
 * @author Lukas Blunschi
 * 
 */
public class MetadataSingleton {

	private static final MetadataSingleton instance = new MetadataSingleton();

	private Metadata metadata;

	private MetadataSingleton() {
		this.metadata = null;
	}

	public static MetadataSingleton getInstance() {
		return instance;
	}

	// ---------------------------------------------------- getters and setters

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

}
