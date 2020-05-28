package ch.ethz.dag;

/**
 * A DAG edge implementation.
 * 
 * @author Lukas Blunschi
 * 
 * @param <T>
 */
public class DagEdgeImpl<T> implements DagEdge<T> {

	private final String name;

	private final T input;

	private final T output;

	public DagEdgeImpl(String name, T input, T output) {
		this.name = name;
		this.input = input;
		this.output = output;
	}

	// ----------------------------------------------------- DAG edge interface

	public String getName() {
		return name;
	}

	public T getInput() {
		return input;
	}

	public T getOutput() {
		return output;
	}

	public T getOtherEnd(T node) {
		return input == node ? output : input;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public String toString() {
		return input + " -- " + name + " --> " + output;
	}

}
