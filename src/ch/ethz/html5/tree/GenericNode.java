package ch.ethz.html5.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * A node.
 * 
 * @author Lukas Blunschi
 * 
 */
public class GenericNode extends AbstractNode<GenericNode> {

	private final LinkType linkType;

	private final String caption;

	private final String info;

	private final List<GenericNode> children;

	/**
	 * Constructor
	 * 
	 * @param caption
	 */
	public GenericNode(String caption) {
		this(LinkType.BASIC, caption, null);
	}

	/**
	 * Constructor
	 * 
	 * @param caption
	 * @param info
	 */
	public GenericNode(String caption, String info) {
		this(LinkType.BASIC, caption, info);
	}

	/**
	 * Full constructor.
	 * 
	 * @param linkType
	 * @param caption
	 * @param info
	 */
	public GenericNode(LinkType linkType, String caption, String info) {
		this.linkType = linkType;
		this.caption = caption;
		this.info = info;
		this.children = new ArrayList<GenericNode>();
	}

	// -------------------------------------------------------------------- add

	public GenericNode addChild(String caption) {
		return addChild(LinkType.BASIC, caption, null);
	}

	public GenericNode addChild(String caption, String info) {
		return addChild(LinkType.BASIC, caption, info);
	}

	public GenericNode addChild(LinkType linkType, String caption, String info) {
		GenericNode child = new GenericNode(linkType, caption, info);
		children.add(child);
		return child;
	}

	public void addChild(GenericNode child) {
		children.add(child);
	}

	// ----------------------------------------------------------------- access

	public LinkType getLinkType() {
		return linkType;
	}

	public String getCaption() {
		return caption;
	}

	public String getInfo() {
		return info;
	}

	public List<GenericNode> getChildren() {
		return children;
	}

}
