package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * Sub title element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class SubTitleElement implements Element {

	private final String title;

	public SubTitleElement(String title) {
		this.title = title;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- sub title -->\n");
		html.append("<h3 class='content'>" + title + "</h3>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
