package ch.zhaw.biosoda;

/**
* The type of a vertex in the Summary Graph
*
* @author Ana Sima
*/
public enum VertexType{
	// a node representing all instances of a class in the summary graph
	CLASS, 
	// a node representing an attribute
	PROPERTY,
	// a known literal
	LITERAL, 
	// an unknown literal (results from adding property edges to the summary graph)
	UNKOWN
}