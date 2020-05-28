package ch.ethz.semdwhsearch.prototyp1.querygraph;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.rdf.dag.RdfDagNode;

/**
 * Tools to work with a collection of query graphs.
 * 
 * @author Lukas Blunschi
 * 
 */
public class QueryGraphs {

	private final String qGraphsStr;

	private final List<RdfDagNode> inputDags;

	public QueryGraphs(String qGraphsStr) {
		this.qGraphsStr = qGraphsStr;
		this.inputDags = new ArrayList<RdfDagNode>();
		if (qGraphsStr != null) {
			parse();
		}
	}

	/**
	 * Separates query graphs by empty lines and parses them.
	 */
	private void parse() {
		StringBuffer buf = new StringBuffer();
		String[] lines = qGraphsStr.split("\n");
		for (String line : lines) {
			line = line.trim();
			if (line.length() == 0) {
				if (buf.length() == 0) {
				} else {
					String nTriples = buf.toString();
					RdfDagNode dag = RdfDagNode.fromNTriples(nTriples);
					inputDags.add(dag.getByUniqueId(QueryGraph.URI_QGRAPH));
					buf = new StringBuffer();
				}
			} else {
				buf.append(line).append("\n");
			}
		}
		if (buf.length() > 0) {
			String nTriples = buf.toString();
			RdfDagNode dag = RdfDagNode.fromNTriples(nTriples);
			inputDags.add(dag.getByUniqueId(QueryGraph.URI_QGRAPH));
		}
	}

	public String getqGraphsStr() {
		return qGraphsStr;
	}

	public List<RdfDagNode> getInputDags() {
		return inputDags;
	}

}
