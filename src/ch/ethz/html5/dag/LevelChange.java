package ch.ethz.html5.dag;

public class LevelChange {

	public final Html5DagGenericNode node;

	public final int levelSrc;

	public final int levelDest;

	public LevelChange(Html5DagGenericNode node, int levelSrc, int levelDest) {
		this.node = node;
		this.levelSrc = levelSrc;
		this.levelDest = levelDest;
	}

}
