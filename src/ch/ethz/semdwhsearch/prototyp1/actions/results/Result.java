package ch.ethz.semdwhsearch.prototyp1.actions.results;

/**
 * Result of an action.
 * 
 * @author Lukas Blunschi
 * 
 */
public abstract class Result {

	public final boolean success;

	public String message;

	public Result(boolean success) {
		this.success = success;
		this.message = "";
	}

	public Result(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

}
