package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import ch.ethz.html5.Canvas;
import ch.ethz.html5.Context2D;
import ch.ethz.html5.dag.DagState;
import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;

/**
 * An element to draw a DAG.
 * 
 * @author Lukas Blunschi
 * 
 */
public class DagElement implements Element {

	// ------------------------------------------------------- static variables

	private static int localCounter = 0;

	// -------------------------------------------------------- final variables

	private final Html5DagGenericNode dag;

	private final boolean collapse;

	public DagElement(Html5DagGenericNode dag, boolean collapse) {
		this.dag = dag;
		this.collapse = collapse;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {

		// vars
		String idDag = "id-dag-" + localCounter++;
		String styleNoDisplay = collapse ? " style='display:none'" : "";

		// print
		html.append("<!-- DAG -->\n");
		html.append("<div class='querygraphs-dags content'>\n");
		html.append("<div><a href='javascript:toggleDisplay(\"" + idDag + "\")'>-+</a></div>\n");
		html.append("<div id='" + idDag + "'" + styleNoDisplay + ">\n");

		// width and height
		DagState state = dag.getBounds(40, 0);
		int width = state.width;
		int height = state.height;

		// crop captions
		dag.cropCaptionsAuto();

		// render
		Canvas canvas = new Canvas(html, "metadataStructure", width, height);
		canvas.open();
		Context2D context = canvas.getContext2D();
		dag.draw(context, state, true);
		canvas.close();

		// close dag
		html.append("</div>\n");
		html.append("</div>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {

	}

}
