package ch.ethz.semdwhsearch.prototyp1.tools;

/**
 * Tools to work with arrays.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ArrayTools {

	/**
	 * Separate array.
	 * 
	 * @param array
	 * @param separator
	 * @return
	 */
	public static String separate(Object[] array, String separator) {
		StringBuffer buf = new StringBuffer();
		int size = array.length;
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				buf.append(separator);
			}
			buf.append(array[i]);
		}
		return buf.toString();
	}

}
