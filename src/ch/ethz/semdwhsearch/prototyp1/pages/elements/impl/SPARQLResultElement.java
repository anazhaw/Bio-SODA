package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * SPARQL result element.
 * 
 * @author Ana Sima
 * 
 */
public class SPARQLResultElement implements Element {

	private final int resultIndex;

	public SPARQLResultElement(int resultIndex) {
		this.resultIndex = resultIndex;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {

		// open
		html.append("<!-- relation " + resultIndex + " -->\n");
		html.append("<div class='content floatleft'>\n");

		// result div id
		String resultDivId = resultIndex  + "_resultdiv";

		appendResultTable(html, resultDivId);

		// close
		html.append("</div>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

	// -------------------------------------------------- helper

	private void appendResultTable(StringBuffer html, String resultDivId) {

		// prepare for async execution
		html.append("<div id='" + resultDivId + "' class='sql-result-div'>");
		html.append("<div class='waiting'>executing SPARQL...</div>\n");
		html.append("</div>\n");
	}

}

