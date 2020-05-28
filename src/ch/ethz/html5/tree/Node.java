package ch.ethz.html5.tree;

import java.util.List;

/**
 * Interface of all nodes.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface Node<T> {

	/**
	 * Get type of incoming link.
	 * 
	 * @return link type of incoming link.
	 */
	LinkType getLinkType();

	/**
	 * Get caption.
	 * 
	 * @return caption.
	 */
	String getCaption();

	/**
	 * Get style information (in CSS).
	 * 
	 * @return style information or null if no style information attached.
	 */
	String getStyle();

	/**
	 * Get additional information.
	 * 
	 * @return additional info.
	 */
	String getInfo();

	/**
	 * Get children nodes.
	 * 
	 * @return children.
	 */
	List<T> getChildren();

}
