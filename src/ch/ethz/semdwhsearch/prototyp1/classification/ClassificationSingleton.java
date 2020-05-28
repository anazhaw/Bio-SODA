package ch.ethz.semdwhsearch.prototyp1.classification;

/**
 * A singleton for the classification.
 * <p>
 * To avoid having to load the classification upon each request.
 * <p>
 * All this singleton does is holding a reference to a Classification instance.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ClassificationSingleton {

	private static final ClassificationSingleton instance = new ClassificationSingleton();

	private Classification classification;

	private ClassificationSingleton() {
		this.classification = null;
	}

	public static ClassificationSingleton getInstance() {
		return instance;
	}

	// ---------------------------------------------------- getters and setters

	public Classification getClassification() {
		return classification;
	}

	public void setClassification(Classification classification) {
		this.classification = classification;
	}

}
