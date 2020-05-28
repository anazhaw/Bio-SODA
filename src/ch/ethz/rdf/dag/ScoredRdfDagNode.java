package ch.ethz.rdf.dag;

/**
 * A scored RDF-DAG node.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ScoredRdfDagNode implements Comparable<ScoredRdfDagNode> {

	public final int index;

	public final Double score;

	public final RdfDagNode node;

	public ScoredRdfDagNode(int index, Double score, RdfDagNode node) {
		this.index = index;
		this.score = score;
		this.node = node;
	}

	public int compareTo(ScoredRdfDagNode o) {
		return -1 * score.compareTo(o.score);
	}

}
