package ch.ethz.semdwhsearch.prototyp1.tools;

/**
 * Defines the interface for a loadable singleton class.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface Loadable {

	/**
	 * Call this after the loading process is finished.
	 */
	void loadingDone();

	/**
	 * Get loading state.
	 * 
	 * @return true if loaded, false otherwise.
	 */
	boolean isLoaded();

}
