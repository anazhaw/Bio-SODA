package ch.ethz.semdwhsearch.prototyp1.querygraph;

/**
 * An interface which is implemented by all objects which may have a score.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface HasScore extends HasURI {

	/**
	 * Get score.
	 * 
	 * @return score. Negativ if not set.
	 */
	double getScore();

	/**
	 * Set score.
	 * 
	 * @param score
	 *            between 0 and 1.0 (inclusive). Negativ to unset the score.
	 */
	void setScore(double score);

}
