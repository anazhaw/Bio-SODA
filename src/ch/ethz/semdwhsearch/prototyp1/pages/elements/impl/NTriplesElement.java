package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;
import ch.ethz.semdwhsearch.prototyp1.tools.Escape;

/**
 * An element to show N-Triples.
 * 
 * @author Lukas Blunschi
 * 
 */
public class NTriplesElement implements Element {

	// ------------------------------------------------------- static variables

	private static int localCounter = 0;

	// -------------------------------------------------------- final variables

	private final String nTriples;

	private final boolean collapse;

	public NTriplesElement(String nTriples, boolean collapse) {
		this.nTriples = nTriples;
		this.collapse = collapse;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {

		// vars
		String idNTriples = "id-ntriples-" + localCounter++;
		String styleNoDisplay = collapse ? " style='display:none'" : "";
		String nTriplesDisplay = Escape.safeXml(nTriples).replaceAll("\n", "<br/>\n");

		// print
		html.append("<!-- N-Triples -->\n");
		html.append("<div class='querygraphs-ntriples content'>\n");
		html.append("<div><a href='javascript:toggleDisplay(\"" + idNTriples + "\")'>-+</a></div>\n");
		html.append("<div id='" + idNTriples + "'" + styleNoDisplay + ">\n");
		html.append(nTriplesDisplay);
		html.append("</div>\n");
		html.append("</div>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
