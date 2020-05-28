package ch.ethz.semdwhsearch.prototyp1.pages;

import javax.servlet.http.HttpServletRequest;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;

/**
 * Every page has to implement this interface.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface Page {

	/**
	 * Create init() JavaScript.
	 * 
	 * @return init script or null if none.
	 */
	String getInitJs();

	/**
	 * Create HTML for this page. No body tag.
	 * 
	 * @param req
	 * @param dict
	 * @return
	 */
	String getHtml(HttpServletRequest req, Dictionary dict);

	/**
	 * Create HTML for this page. No body tag.
	 * <p>
	 * This method generate header, menu and footer.
	 * 
	 * @param req
	 * @param dict
	 * @return
	 */
	String getContent(HttpServletRequest req, Dictionary dict);

}
