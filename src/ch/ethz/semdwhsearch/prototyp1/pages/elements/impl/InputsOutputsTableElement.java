package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import ch.ethz.dag.DagEdge;
import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.metadata.MetadataSingleton;
import ch.ethz.semdwhsearch.prototyp1.metadata.mapping.MetadataMapping;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.enums.ElementType;

/**
 * An element which shows inputs and outputs of a node in a table.
 * 
 * @author Lukas Blunschi
 * 
 */
public class InputsOutputsTableElement implements Element {

	private final Html5DagGenericNode node;

	private final String ctxPath;

	private final String pagename;

	private final List<String> basePathsToRemove;

	public InputsOutputsTableElement(Html5DagGenericNode node, String ctxPath, String pagename) {
		this.node = node;
		this.ctxPath = ctxPath;
		this.pagename = pagename;
		this.basePathsToRemove = new ArrayList<String>();

		// compile list of base paths to remove
		Metadata metadata = MetadataSingleton.getInstance().getMetadata();
		MetadataMapping mapping = metadata.getMapping();
		basePathsToRemove.add(mapping.getSchemaBasePath());
		basePathsToRemove.add(mapping.getSchemaBaseUrl());
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {

		html.append("<!-- inputs outputs table -->\n");
		html.append("<div class='content inputsoutputs'>\n");
		html.append("<table class='inputsoutputs'>\n");

		html.append("<tr>\n");

		// inputs
		html.append("<td class='input'>\n");
		SortedSet<DagEdge<Html5DagGenericNode>> inputs = node.getInputs();
		if (inputs.size() > 0) {
			html.append("<ul>\n");
			for (DagEdge<Html5DagGenericNode> edge : inputs) {
				String edgeName = edge.getName();
				Html5DagGenericNode input = edge.getInput();
				new NodeLiElement(node, input, true, ElementType.INPUT, edgeName, ctxPath, pagename).appendEmbedableHtml(
						html, dict);
			}
			html.append("</ul>\n");
		}
		html.append("</td>\n");

		// node
		html.append("<td class='center'>\n");
		html.append("<ul>\n");
		new NodeLiElement(node, node, true, ElementType.NODE, null, ctxPath, pagename).appendEmbedableHtml(html, dict);
		html.append("</ul>\n");
		html.append("</td>\n");

		// outputs
		html.append("<td class='output'>\n");
		SortedSet<DagEdge<Html5DagGenericNode>> outputs = node.getOutputs();
		if (outputs.size() > 0) {
			html.append("<ul>\n");
			for (DagEdge<Html5DagGenericNode> edge : outputs) {
				String edgeName = edge.getName();
				Html5DagGenericNode output = edge.getOutput();
				new NodeLiElement(node, output, true, ElementType.OUTPUT, edgeName, ctxPath, pagename).appendEmbedableHtml(
						html, dict);
			}
			html.append("</ul>\n");
		}
		html.append("</td>\n");

		html.append("</tr>\n");

		html.append("</table>\n");
		html.append("</div>\n\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
