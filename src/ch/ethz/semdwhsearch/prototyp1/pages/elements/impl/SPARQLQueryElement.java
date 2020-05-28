package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.actions.Params;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * Query element.
 * 
 * @author Ana Sima
 * 
 */
public class SPARQLQueryElement implements Element {

	private final String query;

	public SPARQLQueryElement(String query) {
		this.query = query;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- query -->\n");
		html.append("<div class='content'>\n");

		// input
		String value = query == null ? "" : query;
		html.append("<textarea id='query' rows='120' cols='120' name='" + Params.Q + "' value='" + value + "' ></textarea>\n");

		// submit
		html.append("<input type='submit' value='" + dict.go() + "' />\n");

		html.append("</div>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
