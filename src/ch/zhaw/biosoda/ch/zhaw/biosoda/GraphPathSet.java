package ch.zhaw.biosoda;

import java.util.HashSet;

import org.jgrapht.GraphPath;

public class GraphPathSet extends HashSet<GraphPath<String, SummaryEdge>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean contains(Object obj) {
		GraphPath<String, SummaryEdge> other = (GraphPath<String, SummaryEdge>)obj;
		for(GraphPath<String, SummaryEdge> src: this) {
			if(src.getVertexList().equals(other.getVertexList()) && src.getEdgeList().equals(other.getEdgeList()))
				return true;
		}
		return false;
	}
}
