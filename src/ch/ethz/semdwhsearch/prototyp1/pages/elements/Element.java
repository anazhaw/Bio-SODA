package ch.ethz.semdwhsearch.prototyp1.pages.elements;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;

/**
 * Interface for all page elements.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface Element {

	void appendHtml(StringBuffer html, Dictionary dict);

	void appendHtmlClose(StringBuffer html, Dictionary dict);

}
