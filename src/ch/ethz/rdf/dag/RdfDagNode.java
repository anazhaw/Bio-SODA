package ch.ethz.rdf.dag;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.ethz.dag.DagAbstractNode;
import ch.ethz.dag.DagEdge;
import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.html5.dag.Parameter;

/**
 * An RDF DAG node.
 * 
 * @author Lukas Blunschi
 * 
 */
public class RdfDagNode extends DagAbstractNode<RdfDagNode> {

	// ---------------------------------------------------------------- members

	/**
	 * optional list of literals attached to this RDF node.
	 * <p>
	 * initially equals to null.
	 */
	private List<Literal> literals;

	// ----------------------------------------------------------- construction

	/**
	 * Creates a new DAG.
	 * <p>
	 * Use this only once per DAG.
	 * 
	 * @param uniqueId
	 *            of first node.
	 */
	public RdfDagNode(String uniqueId) {
		super(new TreeMap<String, RdfDagNode>(), uniqueId);
		addNode(this);
	}

	private RdfDagNode(SortedMap<String, RdfDagNode> idMap, String uniqueId) {
		super(idMap, uniqueId);
		addNode(this);
	}

	protected final RdfDagNode getNewNode(SortedMap<String, RdfDagNode> idMap, String uniqueId) {
		return new RdfDagNode(idMap, uniqueId);
	}

	// ------------------------------------------------------------------- copy

	protected void copyNodeMembers(RdfDagNode src, RdfDagNode dst) {

		// copy literals
		if (src.literals != null) {
			for (Literal literal : src.literals) {
				dst.addLiteral(literal.name, literal.value);
			}
		}
	}

	// --------------------------------------------------------------- literals

	public void addLiteral(String name, String value) {
		if (literals == null) {
			literals = new ArrayList<Literal>();
		}
		Literal literal = new Literal(name, value);
		if (!literals.contains(literal)) {
			literals.add(literal);
		}
	}

	/**
	 * WARNING: this method only returns the first matching literal it finds.
	 * 
	 * @param name
	 * @return literal value or null if given name not found.
	 */
	public String getLiteralValue(String name) {
		if (literals != null) {
			for (Literal literal : literals) {
				if (literal.name.equals(name)) {
					return literal.value;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param name
	 * @return literal values for given edge name. never null.
	 */
	public List<String> getLiteralValues(String name) {
		List<String> result = new ArrayList<String>();
		if (literals != null) {
			for (Literal literal : literals) {
				if (literal.name.equals(name)) {
					result.add(literal.value);
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @return true if
	 */
	public boolean hasLiteral(String name, String value) {
		String storedValue = getLiteralValue(name);
		return storedValue != null && storedValue.equals(value);
	}

	// ---------------------------------------------------------- serialization

	public String toNTriples() {
		StringBuffer txt = new StringBuffer();
		for (RdfDagNode node : getAllNodes()) {

			// outputs
			for (DagEdge<RdfDagNode> edge : node.getOutputs()) {
				RdfDagNode otherEnd = edge.getOtherEnd(node);
				txt.append("<").append(node.getUniqueId()).append("> ");
				txt.append("<").append(edge.getName()).append("> ");
				txt.append("<").append(otherEnd.getUniqueId()).append("> .\n");
			}

			// literal
			if (node.literals != null) {
				for (Literal literal : node.literals) {
					txt.append("<").append(node.getUniqueId()).append("> ");
					txt.append("<").append(literal.name).append("> ");
					txt.append("\"").append(literal.value).append("\" .\n");
				}
			}
		}
		return txt.toString();
	}

	public static RdfDagNode fromNTriples(String nTriples) {
		RdfDagNode result = null;

		// loop over all lines
		String[] lines = nTriples.split("\n");
		for (String line : lines) {
			line = line.trim();

			// ignore empty lines
			if (line.length() == 0) {
				continue;
			}

			// position of triples
			final int pos0 = 0;
			final int pos1 = line.indexOf(">", 1);

			final int pos2 = line.indexOf("<", pos1 + 1);
			final int pos3 = line.indexOf(">", pos2 + 1);

			int pos4 = -1;
			int pos5 = -1;

			// literal flag
			boolean isLiteral = false;
			int posQuotes = line.indexOf("\"", pos3 + 1);
			if (posQuotes < 0) {
				pos4 = line.indexOf("<", pos3 + 1);
				pos5 = line.indexOf(">", pos4 + 1);
				String objectStr = line.substring(pos4 + 1, pos5);
				if (objectStr.startsWith("http://")) {
					isLiteral = false;
				} else {
					isLiteral = true;
				}
			} else {
				isLiteral = true;
				pos4 = posQuotes;
				pos5 = line.indexOf("\"", pos4 + 1);
			}

			// parse
			String uniqueIdFrom = line.substring(pos0 + 1, pos1);
			String edgeName = line.substring(pos2 + 1, pos3);
			String uniqueIdToOrLiteral = line.substring(pos4 + 1, pos5);

			// add to dag
			if (result == null) {
				result = new RdfDagNode(uniqueIdFrom);
			}
			if (isLiteral) {
				RdfDagNode nodeCur = result.getByUniqueId(uniqueIdFrom);
				if (nodeCur == null) {
					nodeCur = result.addNode(uniqueIdFrom);
				}
				nodeCur.addLiteral(edgeName, uniqueIdToOrLiteral);
			} else {
				result.addEdge(edgeName, uniqueIdFrom, uniqueIdToOrLiteral);
			}
		}

		return result;
	}

	public Html5DagGenericNode toHtml5Dag() {
		Html5DagGenericNode html5Dag = new Html5DagGenericNode(this.getUniqueId());
		toHtml5DagRec(this, html5Dag);

		// put some default style
		String style = "color: black; background-color: #ddaaff; border: 1px solid #9900ff";
		html5Dag.styleByType(Integer.MAX_VALUE, style);

		return html5Dag;
	}

	private void toHtml5DagRec(RdfDagNode node, Html5DagGenericNode html5Dag) {

		// precondition: given node is already added and connected to the HTML
		// dag

		// outputs
		for (DagEdge<RdfDagNode> edge : node.getOutputs()) {
			RdfDagNode otherEnd = edge.getOtherEnd(node);
			html5Dag.addEdge(edge.getName(), node.getUniqueId(), otherEnd.getUniqueId());
			// recurse
			toHtml5DagRec(otherEnd, html5Dag);
		}

		// literals
		if (node.literals != null) {
			for (Literal literal : node.literals) {
				Html5DagGenericNode html5Node = html5Dag.getByUniqueId(node.getUniqueId());
				html5Node.addParameter(new Parameter(literal.name.substring(literal.name.indexOf("#") + 1,  literal.name.length()), literal.name.substring(literal.name.indexOf("#") + 1,  literal.name.length()) + " "+ literal.value));
				//html5Node.addParameter(new Parameter(literal.name, literal.value));
			}
		}
	}

}
