package ch.ethz.semdwhsearch.prototyp1.tools;

/**
 * Tools for escaping 'dangerous' characters.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Escape {

	/**
	 * Escape the following characters (these are the predefined entities in
	 * XML):
	 * <ul>
	 * <li>" = quotation mark
	 * <li>& = ampersand
	 * <li>' = apostrophe quote
	 * <li>&lt; = less-than sign
	 * <li>&gt; = greater-than sign
	 * </ul>
	 * 
	 * @param input
	 * @return
	 */
	public static String safeXml(String input) {

		// this must be first!
		input = input.replaceAll("&", "&amp;");

		// now the rest
		input = input.replaceAll("\"", "&quot;");
		input = input.replaceAll("'", "&apos;");
		input = input.replaceAll("<", "&lt;");
		input = input.replaceAll(">", "&gt;");
		return input;
	}

	/**
	 * Escape single quotes in SQL.
	 * 
	 * @param input
	 * @return
	 */
	public static String safeSql(String input) {

		// escape single quotes and weird \" in comments, which are transformed to \\\" somewhere
		// just skip them alltogether
		//if(input.contains("\\\\\\\""))
		input = input.replace("\\\\\\\"", "").replaceAll("'", "''");
		return input;
	}

	/**
	 * Escape given input to a filename.
	 * <ul>
	 * <li>replace whitespace
	 * <li>to lower case
	 * </ul>
	 * 
	 * @param input
	 * @return filename.
	 */
	public static String safeFilename(String input) {
		input = input.replaceAll("\\s", "-");
		input = input.toLowerCase();
		return input;
	}

}
