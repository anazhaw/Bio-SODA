package ch.ethz.semdwhsearch.prototyp1.tools;

import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These servlet tools help to retrieve parameters inside a HTTP request or to
 * dump such a request.
 * 
 * @author Lukas Blunschi
 */
public class ServletTools {

	private static final Logger logger = LoggerFactory.getLogger(ServletTools.class);

	/**
	 * Decode the given URI using UTF-8 charset.
	 * 
	 * @param encoded
	 *            encoded URI.
	 * @return decoded URI, or null if failure occured.
	 */
	public static String decodeURI(String encoded) {
		String decoded = null;
		try {
			decoded = URLDecoder.decode(encoded, "utf-8");
		} catch (Exception e) {
			logger.warn("Failure while decoding URI (" + encoded + "): " + e.getMessage());
			decoded = null;
		}
		return decoded;
	}

	/**
	 * Remove path information from a given filename.
	 * 
	 * @param filename
	 *            filename to remove path information from.
	 * @return filename without path information.
	 */
	public static String removePathInformation(String filename) {

		// convert backslashes to forward slahes
		filename = filename.replace('\\', '/');

		// cut after last slash
		int pos = filename.lastIndexOf("/");
		String name = null;
		if (pos == -1) {
			// okay. only name.
			name = filename;
		} else {
			name = filename.substring(pos + 1);
		}
		return name;
	}

}
