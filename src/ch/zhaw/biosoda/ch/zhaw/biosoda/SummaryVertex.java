package ch.zhaw.biosoda;

import java.io.Serializable;

/**
 * A vertex in the Summary Graph
 * 
 * @author Ana Sima
 * 
 */
public class SummaryVertex implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	VertexType type;
	String uri;
	String label;
	
	public SummaryVertex(String uri, VertexType v) {
		this.type = v;
		this.uri = uri;
	}
	
	public SummaryVertex(String uri, String label, VertexType v) {
		this.type = v;
		this.uri = uri;
		this.label = label;
	}
	
	public String getUri() {
		return this.uri;
	}
	
	public VertexType getType() {
		return this.type;
	}
	
	public boolean equals(Object other) {
		SummaryVertex otherVertex = (SummaryVertex) other;
		return this.uri.equals(otherVertex.uri) && this.type.equals(otherVertex.type);
	}
}
