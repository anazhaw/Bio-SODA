package ch.ethz.semdwhsearch.prototyp1.pages.elements.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.ethz.dag.DagEdge;
import ch.ethz.html5.Canvas;
import ch.ethz.html5.Context2D;
import ch.ethz.html5.dag.DagState;
import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.rdf.dag.RdfDagNode;
import ch.ethz.rdf.dag.ScoredRdfDagNode;
import ch.ethz.semdwhsearch.prototyp1.actions.Params;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.localization.Dictionary;
import ch.ethz.semdwhsearch.prototyp1.metadata.Metadata;
import ch.ethz.semdwhsearch.prototyp1.pages.elements.Element;
import ch.ethz.semdwhsearch.prototyp1.querygraph.BusinessObject;
import ch.ethz.semdwhsearch.prototyp1.querygraph.QueryGraph;
import ch.ethz.semdwhsearch.prototyp1.querygraph.Value;
import ch.ethz.semdwhsearch.prototyp1.tools.Doubles;
import ch.ethz.semdwhsearch.prototyp1.tools.Escape;

/**
 * Query graphs element.
 * 
 * @author Lukas Blunschi
 * 
 */
public class QueryGraphsElement implements Element {

	private final List<RdfDagNode> queryGraphsDags;

	private final String suffix;

	private final boolean removeSrcLinks;

	private final boolean collapseNTriples;

	private final boolean collapseDags;

	private final String pagenameBrowse;

	private final String pagenameInspect;

	public QueryGraphsElement(List<RdfDagNode> queryGraphsDags, String suffix, boolean removeSrcLinks,
			boolean collapseNTriples, boolean collapseDags, String pagenameBrowse, String pagenameInspect) {
		this.queryGraphsDags = queryGraphsDags;
		this.suffix = suffix;
		this.removeSrcLinks = removeSrcLinks;
		this.collapseNTriples = collapseNTriples;
		this.collapseDags = collapseDags;
		this.pagenameBrowse = pagenameBrowse;
		this.pagenameInspect = pagenameInspect;
	}

	public void appendHtml(StringBuffer html, Dictionary dict) {
		html.append("<!-- query graphs -->\n");

		// show form if no input given
		if (queryGraphsDags.size() == 0) {
			html.append("<div class='content'>\n");
			html.append("<textarea id='querygraphs' name='" + Params.QGRAPHS + "' cols='140' rows='8'></textarea>\n");
			html.append("<input type='submit' value='" + dict.go() + "' />\n");
			html.append("</div>\n");
		}

		// order by score
		List<ScoredRdfDagNode> orderedDags = new ArrayList<ScoredRdfDagNode>(queryGraphsDags.size());
		for (int i = 0; i < queryGraphsDags.size(); i++) {
			RdfDagNode dag = queryGraphsDags.get(i);

			// get score
			QueryGraph qg = QueryGraph.parse(dag);
			double score = qg.getScore();
			
			String scoreStr = dag.getByUniqueId(QueryGraph.URI_QGRAPH).getLiteralValue(QueryGraph.EDGE_HASRANKING);
			if ((score == Constants.INITIAL_SCORE) && (scoreStr != null)) {
				score = Double.parseDouble(scoreStr);
			}

			orderedDags.add(new ScoredRdfDagNode(i, score, dag));
		}
		Collections.sort(orderedDags);

		// loop over all query graphs
		final int displayCount = Constants.DISPLAY_COUNT;
		final int dagCount = orderedDags.size();
		for (int i = 0; i < dagCount && i < displayCount; i++) {
			ScoredRdfDagNode rdf = orderedDags.get(i);
			RdfDagNode qGraph = rdf.node.getByUniqueId(QueryGraph.URI_QGRAPH);
			QueryGraph qg = QueryGraph.parse(qGraph);
			RdfDagNode dag = qGraph.copyOutputSubdag();
			Html5DagGenericNode html5Dag = dag.toHtml5Dag();
			String nTriples = qGraph.toNTriples();

			html.append("<div class='content querygraphs'>\n");

			// div IDs
			String idNTriples = "qg-ntriples-" + i + suffix;
			String idDag = "qg-dag-" + i + suffix;

			// Id
			html.append("<div class='querygraphs-ids'>");
			html.append(i);
			if (qg.hasReducedJoinSet()) {
				html.append(" (r)");
			}
			html.append("</div>\n");

			// Score
			String scoreString = dict.score() + ": " + Doubles.formatter.format(qg.getScore());

			// append score
			html.append("<div class='querygraphs-scores'>");
			html.append(scoreString);
			html.append("</div>\n");

			// business object subgraph links
			html.append("<div class='querygraphs-bos'>\n");
			for (BusinessObject bo : qg.getBusinessObjects()) {
				String pPageS = "page=" + pagenameInspect + "&amp;";
				String pUri = Constants.P_URI + "=" + bo.srcLink;
				html.append("<a href='?" + pPageS + pUri + "' target='_blank'>");
				html.append("<span class='BUSINESS_OBJECT'>" + bo.srcLink + "</span>");
				html.append("</a>\n");
				String pPageB = "page=" + pagenameBrowse + "&amp;";
				String pInput = EntryPointSelectorElement.P_URI_INPUT + "=" + bo.srcLink;
				html.append("<a href='?" + pPageB + pInput + "' target='_blank'>");
				html.append("<span class='BUSINESS_OBJECT no-underline'>&#160;~&#160;&#160;</span>");
				html.append("</a>\n");
			}
			for (Value val : qg.getValues()) {
				String pPage = "page=" + pagenameInspect + "&amp;";
				String pUri = Constants.P_URI + "=" + val.srcLink;
				html.append("<a href='?" + pPage + pUri + "' target='_blank'>");
				html.append("<span class='VALUE'>" + val.srcLink + "</span>");
				html.append("</a>\n");
				String pPageB = "page=" + pagenameBrowse + "&amp;";
				String pInput = EntryPointSelectorElement.P_URI_INPUT + "=" + val.srcLink;
				html.append("<a href='?" + pPageB + pInput + "' target='_blank'>");
				html.append("<span class='VALUE no-underline'>&#160;~&#160;&#160;</span>");
				html.append("</a>\n");
			}
			html.append("</div>\n");

			// N-Triples
			String styleNoDisplay = collapseNTriples ? " style='display:none'" : "";
			String nTriplesDisplay = Escape.safeXml(nTriples).replaceAll("\n", "<br/>\n");
			html.append("<div class='querygraphs-ntriples'>\n");
			html.append("<div><a href='javascript:toggleDisplay(\"" + idNTriples + "\")'>-+</a></div>\n");
			html.append("<div id='" + idNTriples + "'" + styleNoDisplay + ">\n");
			html.append(nTriplesDisplay);
			html.append("</div>\n");
			html.append("</div>\n");

			// Dags
			styleNoDisplay = collapseDags ? " style='display:none'" : "";
			html.append("<div class='querygraphs-dags'>\n");
			html.append("<div><a href='javascript:toggleDisplay(\"" + idDag + "\")'>-+</a></div>\n");
			html.append("<div id='" + idDag + "'" + styleNoDisplay + ">\n");

			// coloring
			// - root (0)
			html5Dag.getByUniqueId(QueryGraph.URI_QGRAPH).setStyle(Metadata.STYLE_UK);
			html5Dag.getByUniqueId(QueryGraph.URI_QGRAPH).setType(0);
			// - unknowns (2)
			Html5DagGenericNode ukType = html5Dag.getByUniqueId(QueryGraph.URI_UNKNOWN_PREFIX + "type");
			if (ukType != null) {
				for (DagEdge<Html5DagGenericNode> edge : ukType.getInputs()) {
					Html5DagGenericNode uk = edge.getOtherEnd(ukType);
					uk.setStyle(Metadata.STYLE_UK);
					uk.setType(2);
				}
			}
			// - operators (1)
			Html5DagGenericNode opType = html5Dag.getByUniqueId(QueryGraph.URI_OPERATOR_PREFIX + "type");
			if (opType != null) {
				for (DagEdge<Html5DagGenericNode> edge : opType.getInputs()) {
					Html5DagGenericNode op = edge.getOtherEnd(opType);
					op.setStyle(Metadata.STYLE_OP);
					op.setType(1);
				}
			}
			// - business objects (2)
			Html5DagGenericNode boType = html5Dag.getByUniqueId(QueryGraph.URI_BUSINESSOBJECT_PREFIX + "type");
			if (boType != null) {
				for (DagEdge<Html5DagGenericNode> edge : boType.getInputs()) {
					Html5DagGenericNode bo = edge.getOtherEnd(boType);
					bo.setStyle(Metadata.STYLE_LS);
					bo.setType(2);
				}
			}
			// - values (2)
			Html5DagGenericNode valType = html5Dag.getByUniqueId(QueryGraph.URI_VALUE_PREFIX + "type");
			if (valType != null) {
				for (DagEdge<Html5DagGenericNode> edge : valType.getInputs()) {
					Html5DagGenericNode val = edge.getOtherEnd(valType);
					val.setStyle(Metadata.STYLE_BD);
					val.setType(2);
				}
			}
			// - join sets (3)
			Html5DagGenericNode jsType = html5Dag.getByUniqueId(QueryGraph.URI_JOINSET_PREFIX + "type");
			if (jsType != null) {
				for (DagEdge<Html5DagGenericNode> edge : jsType.getInputs()) {
					Html5DagGenericNode js = edge.getOtherEnd(jsType);
					js.setStyle(Metadata.STYLE_JO);
					js.setType(3);

					// joins (4)
					for (DagEdge<Html5DagGenericNode> edgeJ : js.getOutputs(QueryGraph.EDGE_JOIN)) {
						Html5DagGenericNode join = edgeJ.getOtherEnd(js);
						join.setStyle(Metadata.STYLE_JO);
						join.setType(4);
					}
				}
			}
			// - tables, relationships, join conditions, keys and columns
			List<Html5DagGenericNode> tableNodes = new ArrayList<Html5DagGenericNode>();
			if (boType != null) {
				for (DagEdge<Html5DagGenericNode> edge : boType.getInputs()) {
					Html5DagGenericNode bo = edge.getOtherEnd(boType);
					for (DagEdge<Html5DagGenericNode> edgeTable : bo.getOutputs(QueryGraph.EDGE_TABLE)) {
						Html5DagGenericNode table = edgeTable.getOtherEnd(bo);
						tableNodes.add(table);
					}
				}
			}
			if (valType != null) {
				for (DagEdge<Html5DagGenericNode> edge : valType.getInputs()) {
					Html5DagGenericNode val = edge.getOtherEnd(valType);
					for (DagEdge<Html5DagGenericNode> edgeTable : val.getOutputs(QueryGraph.EDGE_TABLE)) {
						Html5DagGenericNode table = edgeTable.getOtherEnd(val);
						tableNodes.add(table);
					}
				}
			}
			// - tables (5)
			for (Html5DagGenericNode table : tableNodes) {
				table.setStyle(Metadata.STYLE_BD);
				table.setType(5);
				// - relationships (6)
				for (DagEdge<Html5DagGenericNode> edgeRel : table.getOutputs(QueryGraph.EDGE_RELATIONSHIP)) {
					Html5DagGenericNode relationship = edgeRel.getOtherEnd(table);
					relationship.setStyle(Metadata.STYLE_JO);
					relationship.setType(6);
					// - join conditions (7)
					for (DagEdge<Html5DagGenericNode> edgeJc : relationship.getOutputs(QueryGraph.EDGE_JOINCONDITION)) {
						Html5DagGenericNode joinCondition = edgeJc.getOtherEnd(relationship);
						joinCondition.setStyle(Metadata.STYLE_JO);
						joinCondition.setType(7);
						// - keys (8)
						for (DagEdge<Html5DagGenericNode> edgeKeys : joinCondition.getOutputs(QueryGraph.EDGE_KEY)) {
							Html5DagGenericNode key = edgeKeys.getOtherEnd(joinCondition);
							key.setStyle(Metadata.STYLE_JO);
							key.setType(8);
						}
					}
				}
				// - columns (9)
				for (DagEdge<Html5DagGenericNode> edge : table.getOutputs(QueryGraph.EDGE_COLUMN)) {
					Html5DagGenericNode column = edge.getOtherEnd(table);
					column.setStyle(Metadata.STYLE_BD);
					column.setType(9);
				}
			}
			// - pk type (10)
			Html5DagGenericNode pkType = html5Dag.getByUniqueId(QueryGraph.URI_PK_PREFIX + "type");
			if (pkType != null) {
				pkType.setStyle(Metadata.STYLE_JO);
				pkType.setType(10);
			}

			// hide type nodes
			html5Dag.removeNodeByUniqueId(QueryGraph.URI_UNKNOWN_PREFIX + "type");
			html5Dag.removeNodeByUniqueId(QueryGraph.URI_OPERATOR_PREFIX + "type");
			html5Dag.removeNodeByUniqueId(QueryGraph.URI_BUSINESSOBJECT_PREFIX + "type");
			html5Dag.removeNodeByUniqueId(QueryGraph.URI_VALUE_PREFIX + "type");
			html5Dag.removeNodeByUniqueId(QueryGraph.URI_JOINSET_PREFIX + "type");

			// hide source links
			if (removeSrcLinks) {
				html5Dag.getByUniqueId(QueryGraph.URI_QGRAPH).removeSubdagAfterEdge(QueryGraph.EDGE_SRCLINK);
			}

			// width and height
			DagState state = html5Dag.getBounds(40, 0);
			int width = state.width;
			int height = state.height;

			// crop
			html5Dag.cropCaptionsByPrefix(QueryGraph.URI_PREFIX);
			html5Dag.cropParameterNamesByPrefix(QueryGraph.URI_PREFIX);

			// render
			Canvas canvas = new Canvas(html, "dag" + i + suffix, width, height);
			canvas.open();
			Context2D context = canvas.getContext2D();
			html5Dag.draw(context, state, true);
			canvas.close();

			html.append("</div>\n");
			html.append("</div>\n");

			// clear
			html.append("<div class='clearer'></div>\n");

			html.append("</div>\n");
		}
		if (dagCount > displayCount) {
			html.append("<div class='content'>\n");
			html.append("... " + (dagCount - displayCount) + " more results.");
			html.append("</div>\n");
		}

		// finish element
		html.append("\n");
	}

	public void appendHtmlClose(StringBuffer html, Dictionary dict) {
	}

}
