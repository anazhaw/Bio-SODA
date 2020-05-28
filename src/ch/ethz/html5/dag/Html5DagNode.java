package ch.ethz.html5.dag;

import java.util.List;

import ch.ethz.dag.DagNode;

/**
 * HTML5 extensions to a DAG node.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface Html5DagNode<T> extends DagNode<T> {

	/**
	 * 
	 * @return caption (or name) of this node.
	 */
	String getCaption();

	/**
	 * 
	 * @return integer type of this node.
	 */
	int getType();

	/**
	 * 
	 * @return shape of this DAG node
	 */
	DagNodeShape getShape();

	/**
	 * 
	 * @return CSS style, e.g. color: black;
	 */
	String getStyle();

	/**
	 * 
	 * @return list of parameters.
	 */
	List<Parameter> getParameters();

}
