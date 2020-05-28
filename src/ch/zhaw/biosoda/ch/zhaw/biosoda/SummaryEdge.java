package ch.zhaw.biosoda;

import java.util.HashSet;

import org.jgrapht.graph.DefaultEdge;

/**
 * An edge in the Summary Graph
 * 
 * @author Ana Sima
 * 
 */
public class SummaryEdge extends DefaultEdge {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7836585495939580530L;
	SummaryVertex uriSourceClass;
	SummaryVertex uriDestClass;
	//there may be multiple edges between summary vertices
	HashSet<String> labels;

    public SummaryEdge(SummaryVertex v1, SummaryVertex v2, HashSet<String> labels) {
        this.uriSourceClass = v1;
        this.uriDestClass = v2;
        this.labels = labels;
    }
    
    public String getEdge(){
    		return labels.toString();
    }
    
    public boolean equals(Object otherEdge) {
    		SummaryEdge other = (SummaryEdge) otherEdge;
    		return this.uriSourceClass.equals(other.uriSourceClass) &&
    				this.uriDestClass.equals(other.uriDestClass) &&
    				this.labels.toString().equals(other.labels.toString());
    }

    public int hashCode() {
    	return this.uriSourceClass.uri.hashCode() + this.uriDestClass.uri.hashCode() + this.labels.toString().hashCode();
    }
    
    public HashSet<String> getSummaryEdges(){
    	return labels;
    }

    public String getSrc() {
        return uriSourceClass.uri;
    }
    
    public VertexType getSrcType() {
        return uriSourceClass.type;
    }

    public String getDest() {
        return uriDestClass.uri;
    }    
    
    public VertexType getDestType() {
        return uriDestClass.type;
    }

    public HashSet<String> getLabels(){
    		return labels;
    }

    public String toString() {
    	String result = "";
    	for (String label: labels)
        	result += uriSourceClass.uri + " --- " + "[ "+ label + " ]" + " ---> "+ uriDestClass.uri  + "\n";
    	return result.trim();
    }
}
