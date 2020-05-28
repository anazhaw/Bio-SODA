package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.html5.dag.Parameter;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.metadata.MetadataSingleton;
import ch.ethz.semdwhsearch.prototyp1.metadata.mapping.MetadataMapping;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Embedable;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.enums.ElementType;

/**
 * An embedabble element to show a node list item.
 * 
 * @author Lukas Blunschi
 * 
 */
public class NodeLiElement implements Embedable {

	private final Html5DagGenericNode centerNode;

	private final Html5DagGenericNode elementNode;

	private final boolean showRemove;

	private final ElementType elementType;

	private final String edgeName;

	private final String ctxPath;

	private final String pagename;

	private final List<String> basePathsToRemove;

	public NodeLiElement(Html5DagGenericNode centerNode, Html5DagGenericNode elementNode, boolean showRemove,
			ElementType elementType, String edgeName, String ctxPath, String pagename) {
		this.centerNode = centerNode;
		this.elementNode = elementNode;
		this.showRemove = showRemove;
		this.elementType = elementType;
		this.edgeName = edgeName;
		this.ctxPath = ctxPath;
		this.pagename = pagename;
		this.basePathsToRemove = new ArrayList<String>();

		// compile list of base paths to remove
		Metadata metadata = MetadataSingleton.getInstance().getMetadata();
		MetadataMapping mapping = metadata.getMapping();
		basePathsToRemove.add(mapping.getSchemaBasePath());
		basePathsToRemove.add(mapping.getSchemaBaseUrl());
	}

	public void appendEmbedableHtml(StringBuffer html, Dictionary dict) {

		// uri
		String uri = null;
		String pUri = null;
		try {
			uri = URLEncoder.encode(elementNode.getUniqueId(), "utf-8");
			pUri = EntryPointSelectorElement.P_URI_INPUT + "=" + uri;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 not supported?");
		}

		// id
		String id = elementNode.getUniqueId();
		for (String basePathToRemove : basePathsToRemove) {
			if (id.startsWith(basePathToRemove)) {
				id = id.substring(basePathToRemove.length());
				break;
			}
		}

		// type coloring
		final boolean isType = edgeName != null && edgeName.equals(Constants.TYPE);

		// list item
		html.append("<li" + (isType ? " class='is-type'" : "") + ">\n");

		// edge name
		if (edgeName != null) {
			if (elementType == ElementType.INPUT) {
				html.append("<div class='edge edge-input'>" + edgeName + " -></div>\n");
			} else {
				html.append("<div class='edge edge-output'>-> " + edgeName + "</div>\n");
			}
		}

		// caption
		html.append("<h3 class='caption'>" + elementNode.getCaption() + "</h3>\n");

		// id with link
		String pPage = "page=" + pagename + "&amp;";
		html.append("<a href='?" + pPage + pUri + "'>");
		html.append(id);
		html.append("</a>");

		// parameters
		if (elementNode.getParameters().size() > 0) {

			// div to collapse parameters
			html.append("<a class='toggle' href='javascript:toggleDisplay(\"" + id + "\");'> + </a>\n");
			html.append("<div id='" + id + "' style='display:none'>\n");

			// parameters
			StringBuffer buf = new StringBuffer();
			for (Parameter parameter : elementNode.getParameters()) {
				buf.append(parameter.key + " = " + parameter.value + "<br/>");
			}
			String paramStr = buf.toString();
			if (paramStr.length() > 9999) {
				paramStr = paramStr.substring(0, 150) + " [...]";
			}
			html.append(paramStr);

			// close collapse div
			html.append("</div>\n");
		}
		// close list item
		html.append("</li>\n");
	}

}
