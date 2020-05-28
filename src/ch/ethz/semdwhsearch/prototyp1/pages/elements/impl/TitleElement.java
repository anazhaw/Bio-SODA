package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * Title element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class TitleElement implements Element {

	private final String title;

	public TitleElement(String title) {
		this.title = title;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- page title -->\n");
		html.append("<h2 class='content'>" + title + "</h2>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
