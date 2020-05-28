package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.actions.Params;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * A button to continue with the next step in the algorithm.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ContinueButton implements Element {

	private final String pagename;

	private final String allNTriples;

	public ContinueButton(String pagename, String allNTriples) {
		this.pagename = pagename;
		this.allNTriples = allNTriples;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- continue -->\n");
		html.append("<form enctype='multipart/form-data' action='?page=" + pagename + "' method='post'>\n");
		html.append("<div class='content'>\n");
		html.append("<textarea style='display:none' name='" + Params.QGRAPHS + "' cols='140' rows='8'>");
		html.append(allNTriples);
		html.append("</textarea>\n");
		html.append("<input type='submit' value='" + dict.goon() + "' />\n");
		html.append("</div>\n");
		html.append("</form>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
