package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.actions.Params;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * Query element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class QueryElement implements Element {

	private final String query;

	public QueryElement(String query) {
		this.query = query;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- query -->\n");
		html.append("<div class='content'>\n");

		// input
		String value = query == null ? "" : query;
		html.append("<input id='query' type='text' name='" + Params.Q + "' value='" + value + "' size='80' />\n");

		// submit
		html.append("<input type='submit' value='" + dict.go() + "' />\n");

		html.append("</div>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
