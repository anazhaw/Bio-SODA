package ch.ethz.html5.tree;

import java.util.List;

import ch.ethz.html5.Context2D;
import ch.ethz.html5.Style;

/**
 * Base class of all concrete node implementations.
 * 
 * @author Lukas Blunschi
 * 
 * @param <T>
 *            concrete type.
 */
public abstract class AbstractNode<T extends AbstractNode<T>> implements Node<T> {

	// ---------------------------------------------------------------- members

	private String style;

	/**
	 * Default constructor.
	 */
	public AbstractNode() {
		this.style = null;
	}

	// --------------------------------------------------------- node interface

	public abstract LinkType getLinkType();

	public abstract String getCaption();

	public String getStyle() {
		return style;
	}

	/**
	 * Set style information.
	 * 
	 * @param style
	 *            CSS style information (or null to remove style information).
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	public abstract String getInfo();

	public abstract List<T> getChildren();

	// --------------------------------------------------------- common methods

	public final int getWidth(TreeSpacing spacing) {
		if (getChildren().size() > 0) {
			int width = 0;
			for (AbstractNode<T> child : getChildren()) {
				width += child.getWidth(spacing);
			}
			return width;
		} else {
			return spacing.grid;
		}
	}

	public final int getHeight(TreeSpacing spacing) {
		int maxHeight = 0;
		for (AbstractNode<T> child : getChildren()) {
			int curHeight = child.getHeight(spacing);
			if (curHeight > maxHeight) {
				maxHeight = curHeight;
			}
		}
		return maxHeight + spacing.grid;
	}

	public final void draw(Context2D context, TreeSpacing spacing) {

		// apply style information
		Style prevStyle = context.applyStyle(new Style(getStyle()));

		// move down
		context.translate(0, spacing.grid);

		// recurse on children
		final int numChildren = getChildren().size();

		// find bounds
		int[] bounds = new int[numChildren + 1];
		for (int i = 0; i < numChildren; i++) {
			bounds[i + 1] = bounds[i] + getChildren().get(i).getWidth(spacing);
		}
		final int totalWidth = bounds[numChildren];
		context.translate(-totalWidth / 2.0, 0);
		for (int i = 0; i < numChildren; i++) {
			double x = (bounds[i + 1] - bounds[i]) / 2.0;
			context.translate(x, 0);
			double dx = (-totalWidth / 2.0) + bounds[i] + x;
			double dy = spacing.grid;
			context.line(-dx, -dy);
			getChildren().get(i).draw(context, spacing);
			context.translate(x, 0);
		}
		context.translate(-totalWidth / 2.0, 0);

		// move up
		context.translate(0, -spacing.grid);

		// draw myself
		String text = getCaption();
		if (getLinkType() == LinkType.REFERENCE) {
			text += "[Ref]";
		}
		context.circle(spacing.radius);
		// context.text(text, 2 * spacing.radius);
		context.textSplitLines(text);

		// restore style information
		context.applyStyle(prevStyle);
	}

	// ------------------------------------------------------- object overrides

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getCaption());
		if (getChildren().size() > 0) {
			buf.append(" {");
			for (int i = 0; i < getChildren().size(); i++) {
				if (i > 0) {
					buf.append(", ");
				}
				AbstractNode<T> child = getChildren().get(i);
				buf.append(child.toString());
			}
			buf.append("}");
		}
		return buf.toString();
	}

}
