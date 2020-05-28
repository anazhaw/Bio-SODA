package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * Logo element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class LogoElement implements Element {

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- logo -->\n");
		html.append("<div class='content logo'>&#160;</div>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
