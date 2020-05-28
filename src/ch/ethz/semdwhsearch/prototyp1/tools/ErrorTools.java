package ch.ethz.semdwhsearch.prototyp1.tools;

/**
 * Tools to handle and work with errors.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ErrorTools {

	/**
	 * Get warning HTML div.
	 * 
	 * @param warningMsg
	 * @return a warning HTML div
	 */
	public static String toDivWarn(String warningMsg) {
		return "<div class='content warning'>" + warningMsg + "</div>\n";
	}

}
