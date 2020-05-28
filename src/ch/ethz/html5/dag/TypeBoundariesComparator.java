package ch.ethz.html5.dag;

import java.util.Comparator;
import java.util.Map;

/**
 * Comparator for types depending on given boundary map.
 * 
 * @author Lukas Blunschi
 * 
 */
public class TypeBoundariesComparator implements Comparator<Integer> {

	private final Map<Integer, TypeBoundaries> boundaryMap;

	public TypeBoundariesComparator(Map<Integer, TypeBoundaries> boundaryMap) {
		this.boundaryMap = boundaryMap;
	}

	public int compare(Integer t1, Integer t2) {
		TypeBoundaries b1 = boundaryMap.get(t1);
		TypeBoundaries b2 = boundaryMap.get(t2);
		int bCompare = b1.compareTo(b2);
		if (bCompare == 0) {
			return t1.compareTo(t2);
		} else {
			return bCompare;
		}
	}

}
