package ch.ethz.semdwhsearch.prototyp1.actions.results;

/**
 * A failed result.
 * 
 * @author Lukas Blunschi
 */
public class Failure extends Result {

	/**
	 * Constructor.
	 * 
	 * @param errorMessage
	 *            error message
	 */
	public Failure(String errorMessage) {
		super(false, errorMessage);
	}

}
