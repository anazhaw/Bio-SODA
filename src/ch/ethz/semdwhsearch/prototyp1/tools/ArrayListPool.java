package ch.ethz.semdwhsearch.prototyp1.tools;

import java.util.ArrayList;

/**
 * A pool of array lists to avoid allocating many new arraylist objects.
 * 
 * @author Lukas Blunschi
 * 
 * @param <T>
 *            type of array list.
 */
public class ArrayListPool<T> {

	private final ArrayList<ArrayList<T>> pool;

	public ArrayListPool() {
		pool = new ArrayList<ArrayList<T>>();

		// provision for 10 array lists
		for (int i = 0; i < 10; i++) {
			pool.add(new ArrayList<T>());
		}
	}

	public ArrayList<T> getArrayList() {
		int size = pool.size();
		if (size == 0) {
			return new ArrayList<T>();
		} else {
			return pool.remove(size - 1);
		}
	}

	public void returnArrayList(ArrayList<T> arrayList) {
		arrayList.clear();
		pool.add(arrayList);
	}

}
