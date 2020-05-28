package ch.ethz.semdwhsearch.prototyp1.pages.elements;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;

/**
 * An embedable produces a block of HTML which has no comments or trailing empty
 * line.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface Embedable {

	void appendEmbedableHtml(StringBuffer html, Dictionary dict);

}
