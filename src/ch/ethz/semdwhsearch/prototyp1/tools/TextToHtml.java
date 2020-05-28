package ch.ethz.semdwhsearch.prototyp1.tools;

/**
 * Tools to convert from (formatted) plain text to HTML.
 * 
 * @author Lukas Blunschi
 * 
 */
public class TextToHtml {

	/**
	 * Convert text to HTML.
	 * <ul>
	 * <li>remove line feed chars.
	 * <li>replace new line chars with br tag.
	 * </ul>
	 * 
	 * @param text
	 * @return HTML on one line.
	 */
	public static String toHtml(String text) {
		String html = text;
		html = html.replaceAll("\\r", "");
		html = html.replaceAll("\\n", "<br />");

		// leading spaces
		int pos = 0;
		while ((pos = html.indexOf("<br /> ", pos)) > 0) {
			final int posBegin = pos + 6;
			int posEnd = posBegin + 1;
			while (posEnd < html.length() && html.charAt(posEnd) == ' ') {
				posEnd++;
			}
			StringBuffer buf = new StringBuffer();
			for (int count = 0; count < posEnd - posBegin; count++) {
				buf.append("&#160;");
			}
			html = html.substring(0, posBegin) + buf.toString() + html.substring(posEnd, html.length());
			pos++;
		}
		return html;
	}

}
