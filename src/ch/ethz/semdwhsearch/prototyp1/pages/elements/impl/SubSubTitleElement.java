package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * Sub sub title element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class SubSubTitleElement implements Element {

	private final String title;

	public SubSubTitleElement(String title) {
		this.title = title;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- sub sub title -->\n");
		html.append("<h4 class='content'>" + title + "</h4>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
