package ch.ethz.semdwhsearch.prototyp1.metadata;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.dag.DagEdge;
import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.semdwhsearch.prototyp1.metadata.mapping.MetadataMapping;

/**
 * Tools to work with metadata.
 * 
 * @author Lukas Blunschi
 * 
 */
public class MetadataTools {

	private final static Logger logger = LoggerFactory.getLogger(MetadataTools.class);

	/**
	 * Get table node which links to given column node.
	 * 
	 * @param columnNode
	 * @param mapping
	 *            metadata mapping which describes the edges between table and
	 *            column nodes.
	 * @return table node
	 */
	public static Html5DagGenericNode getTableNode(Html5DagGenericNode columnNode, MetadataMapping mapping) {
		Html5DagGenericNode tableNode = null;
		Collection<DagEdge<Html5DagGenericNode>> coll = columnNode.getInputs(mapping.getSchemaPropNameLogicalAttr());
		if (coll.size() == 0) {
			logger.warn("logical attribute has no incoming edge from logical table.");
		} else {
			tableNode = coll.iterator().next().getOtherEnd(columnNode);
		}
		return tableNode;
	}

}
