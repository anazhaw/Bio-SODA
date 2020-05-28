package ch.ethz.html5.dag;

/**
 * All possible shapes of a DAG node.
 * 
 * @author Lukas Blunschi
 * 
 */
public enum DagNodeShape {

	CIRCLE("circle"), RECTANGLE("rectangle");

	// ---------------------------------------------------------------- members

	private String shape;

	private DagNodeShape(String form) {
		this.shape = form;
	}

	public String getForm() {
		return shape;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public String toString() {
		return this.shape;
	}

}
