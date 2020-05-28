package ch.ethz.html5.dag;

/**
 * A struct to hold type boundaries.
 * 
 * @author Lukas Blunschi
 * 
 */
public class TypeBoundaries implements Comparable<TypeBoundaries> {

	/**
	 * From inclusive.
	 */
	public int from;

	/**
	 * To exclusive.
	 */
	public int to;

	/**
	 * 
	 * @param from
	 *            from inclusive.
	 * @param to
	 *            to exclusive.
	 */
	public TypeBoundaries(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public int getWidth() {
		return to - from;
	}

	// --------------------------------------------------- comparable interface

	public int compareTo(TypeBoundaries o) {
		if (to == o.to) {
			if (from == o.from) {
				return 0;
			} else {
				return from < o.from ? -1 : 1;
			}
		} else {
			return to < o.to ? -1 : 1;
		}

		// this would compare by from
		// if (from == o.from) {
		// if (to == o.to) {
		// return 0;
		// } else {
		// return to <= o.to ? -1 : 1;
		// }
		// } else {
		// return from < o.from ? -1 : 1;
		// }
	}

	// ------------------------------------------------------- object overrides

	@Override
	public String toString() {
		return "[" + from + ":" + to + "[";
	}

}
