package ch.ethz.html5.dag;

import java.util.SortedMap;
import java.util.SortedSet;

/**
 * State transport between method calls of a HTML DAG.
 * 
 * @author Lukas Blunschi
 * 
 */
public class DagState {

	public final DagSpacing spacing;

	public final int width;

	public final int height;

	private final SortedMap<Integer, SortedSet<Html5DagGenericNode>> levelMap;

	DagState(DagSpacing spacing, int width, int height, SortedMap<Integer, SortedSet<Html5DagGenericNode>> levelMap) {
		this.spacing = spacing;
		this.width = width;
		this.height = height;
		this.levelMap = levelMap;
	}

	SortedMap<Integer, SortedSet<Html5DagGenericNode>> getLevelMap() {
		return levelMap;
	}

}
