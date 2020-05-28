package ch.ethz.html5.dag;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.dag.DagAbstractNode;
import ch.ethz.dag.DagEdge;
import ch.ethz.dag.DagNodeAndLevel;
import ch.ethz.dag.DagNodeComparator;
import ch.ethz.html5.Canvas;
import ch.ethz.html5.Context2D;
import ch.ethz.html5.Style;
import ch.ethz.html5.tools.HtmlHeaderTools;

/**
 * HTML DAG node.
 * <p>
 * This extension adds a caption, a type, a shape, style and coordinates to the
 * basic DAG node.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Html5DagGenericNode extends DagAbstractNode<Html5DagGenericNode> implements Html5DagNode<Html5DagGenericNode> {

	private final static Logger logger = LoggerFactory.getLogger(Html5DagGenericNode.class);

	// ---------------------------------------------------------------- members

	private String caption;

	private int type;

	private DagNodeShape shape;

	private String style;

	private List<Parameter> parameters;

	private int x;

	private int y;

	// [mscmike] -----------------------------------------------------------

	private boolean isNewNode;

	// ----------------------------------------------------------- construction

	/**
	 * Creates a new DAG.
	 * <p>
	 * Use this only once per DAG.
	 * 
	 * @param uniqueId
	 *            of first node.
	 */
	public Html5DagGenericNode(String uniqueId) {
		super(new TreeMap<String, Html5DagGenericNode>(), uniqueId);
		addNode(this);
		init(uniqueId);
	}

	private Html5DagGenericNode(SortedMap<String, Html5DagGenericNode> idMap, String uniqueId) {
		super(idMap, uniqueId);
		addNode(this);
		init(uniqueId);
	}

	private void init(String uniqueId) {
		this.caption = uniqueId;
		this.type = Integer.MAX_VALUE;
		this.shape = DagNodeShape.CIRCLE;
		this.style = null;
		this.parameters = new ArrayList<Parameter>();
		this.isNewNode = false;
	}

	@Override
	protected final Html5DagGenericNode getNewNode(SortedMap<String, Html5DagGenericNode> idMap, String uniqueId) {
		return new Html5DagGenericNode(idMap, uniqueId);
	}

	// ------------------------------------------------------------------- copy

	protected void copyNodeMembers(Html5DagGenericNode src, Html5DagGenericNode dst) {
		dst.caption = src.caption;
		dst.type = src.type;
		dst.shape = src.shape;
		dst.style = src.style;
		for (Parameter param : src.parameters) {
			dst.addParameter(new Parameter(param.key, param.value));
		}
		dst.x = src.x;
		dst.y = src.y;
	}

	// ---------------------------------------------------------------- caption

	/* Return caption as a new string */
	public String getCaption() {
		return ""+caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public void cropCaptionsByPrefix(String prefix) {
		int prefixLength = prefix.length();
		for (Html5DagGenericNode node : getAllNodes()) {
			if (node.caption.startsWith(prefix)) {
				node.caption = node.caption.substring(prefixLength);
			}
		}
	}

    public void cropPropertyNamesByPrefix(String prefix) {
        int prefixLength = prefix.length();
        for (Html5DagGenericNode node : getAllNodes()) {
            for (Parameter property : node.parameters) {
                if (property.key.startsWith(prefix)) {
                    property.key = property.key.substring(prefixLength);
                }
            }
        }
    }
	public void cropCaptionsAuto() {
		for (Html5DagGenericNode node : getAllNodes()) {
			String captionOrg = node.caption;
			captionOrg = captionOrg.replaceAll("[<>\\[\\]]", "");
			// try crop by '#'
			int pos = captionOrg.lastIndexOf("#");
			if (pos > 0) {
				node.caption = captionOrg.substring(pos + 1);
			} else {

				// try crop by '/'
				pos = captionOrg.lastIndexOf("/");
				if (pos > 0) {
					node.caption = captionOrg.substring(pos + 1);
					if(node.caption.length() == 1) {
						int new_pos = captionOrg.substring(0, pos).lastIndexOf("/");
						node.caption = captionOrg.substring(new_pos + 1);
					}
						
				}
			}
		}
	}

	// ------------------------------------------------------------------- type

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	// ------------------------------------------------------------------ shape

	public DagNodeShape getShape() {
		return shape;
	}

	public void setShape(DagNodeShape shape) {
		this.shape = shape;
	}

	// ------------------------------------------------------------------ style

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	// [mscmike]----------------------------------------------------- isNewNode

	public boolean isNewNode() {
		return isNewNode;
	}

	public void setNewNode(boolean isNewNode) {
		this.isNewNode = isNewNode;
	}

	// ------------------------------------------------------------- parameters

	public void addParameter(Parameter parameter) {
		for (Parameter parameterCur : parameters) {
			if (parameterCur.equals(parameter)) {
				return;
			}
		}
		parameters.add(parameter);
	}

	/**
	 * Get parameter value.
	 * 
	 * @param parameterName
	 *            name of parameter to retrieve value for.
	 * @return parameter value or null if parameter not found.
	 */
	public String getParameterValue(String parameterName) {
		String result = null;
		for (Parameter parameterCur : parameters) {
			if (parameterCur.key.equals(parameterName)) {
				if (result == null) {
					result = parameterCur.value;
				} else {
					logger.warn("Ignoring parameter value " + parameterCur.value + ".");
				}
			}
		}
		return result;
	}

	public List<String> getParameterValues(String parameterName) {
		List<String> result = new ArrayList<String>();
		for (Parameter parameterCur : parameters) {
			if (parameterCur.key.equals(parameterName)) {
				result.add(parameterCur.value);
			}
		}
		return result;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void cropParameterNamesByPrefix(String prefix) {
		int prefixLength = prefix.length();
		for (Html5DagGenericNode node : getAllNodes()) {
			for (Parameter parameter : node.parameters) {
				if (parameter.key.startsWith(prefix)) {
					parameter.key = parameter.key.substring(prefixLength);
				}
			}
		}
	}

	// ----------------------------------------------------------------- search

	public Html5DagGenericNode getByParameter(Parameter parameter) {
		for (Html5DagGenericNode node : getAllNodes()) {
			for (Parameter parameterCur : node.parameters) {
				if (parameterCur.equals(parameter)) {
					return node;
				}
			}
		}
		return null;
	}

	// ------------------------------------------------------------------- copy

	public Html5DagGenericNode copyOutputSubdag() {
		return copyOutputSubdag(-1);
	}

	/**
	 * @param distance
	 *            number of hops to copy. -1 for all.
	 */
	public Html5DagGenericNode copyOutputSubdag(int distance) {

		// copy subgraph
		Html5DagGenericNode copy = super.copyOutputSubdag(distance);

		// update member variables of new nodes
		for (Html5DagGenericNode node : copy.getAllNodes()) {
			copyMembers(node);
		}

		return copy;
	}

	private void copyMembers(Html5DagGenericNode copy) {
		Html5DagGenericNode original = this.getByUniqueId(copy.getUniqueId());
		copy.caption = original.caption;
		copy.type = original.type;
		copy.shape = original.shape;
		copy.style = original.style;
		for (Parameter param : original.parameters) {
			if (!copy.parameters.contains(param)) {
				copy.parameters.add(param.copy());
			}
		}
	}

	// ---------------------------------------------------------------- drawing

	public void setTypeByPrefix(String prefix, int type) {
		logger.info("set type by prefix: " + prefix);
		for (Html5DagGenericNode node : getAllNodes()) {
			if (node.getUniqueId().startsWith(prefix)) {
				node.type = type;
			}
		}
	}

	public void styleByPrefix(String prefix, String style) {
		logger.info("set style by prefix: " + prefix);
		for (Html5DagGenericNode node : getAllNodes()) {
			if (node.getUniqueId().startsWith(prefix)) {
				node.style = style;
			}
		}
	}

	public void styleByType(int type, String style) {
		for (Html5DagGenericNode node : getAllNodes()) {
			if (node.type == type) {
				node.style = style;
			}
		}
	}

	public DagState getBounds(int radius, int spaceBetweenLevels) {

		// get node an level list
		// - minimum levels
		// - mixed types
		Collection<DagNodeAndLevel<Html5DagGenericNode>> nodeAndLevelColl = getNodesWithLevels();

		// get type boundaries map
		// (type -> its boundaries)
		Map<Integer, TypeBoundaries> boundaryMap = getBoundaryMap(nodeAndLevelColl);
		logger.info("Boundary map: " + boundaryMap);

		// get ordered types
		List<Integer> types = new ArrayList<Integer>(boundaryMap.keySet());
		Collections.sort(types, new TypeBoundariesComparator(boundaryMap));
		logger.info("Types: " + types);

		// get offset map
		// (type -> shift offset)
		Map<Integer, Integer> offsetMap = getOffsetMap(types, boundaryMap);
		logger.info("Offset map: " + offsetMap);

		// shift levels according to offsets and
		// collect nodes of each level
		// -> levels shifted by type offset
		// -> non-mixed types
		SortedMap<Integer, SortedSet<Html5DagGenericNode>> levelMap = getLevelMap(nodeAndLevelColl, offsetMap);
		if (logger.isDebugEnabled()) {
			for (Map.Entry<Integer, SortedSet<Html5DagGenericNode>> entry : levelMap.entrySet()) {
				Integer level = entry.getKey();
				logger.debug(level + ": " + entry.getValue());
			}
		}

		// compact inside type
		compactLevelMap(levelMap, types, offsetMap, boundaryMap);

		// make room in-between different types
		SortedMap<Integer, SortedSet<Html5DagGenericNode>> levelMap2 = new TreeMap<Integer, SortedSet<Html5DagGenericNode>>();
		if (levelMap.size() > 0) {
			int level = 0;
			int type = types.get(0);
			for (SortedSet<Html5DagGenericNode> nodes : levelMap.values()) {
				if (nodes.size() > 0) {
					int curType = nodes.iterator().next().type;

					// room between types
					if (curType > type) {
						type = curType;
						if (spaceBetweenLevels > 0 && spaceBetweenLevels < 5) {
							for (int i = 0; i < spaceBetweenLevels; i++) {
								levelMap2.put(level++, new TreeSet<Html5DagGenericNode>());
							}
						}
					}

					levelMap2.put(level++, nodes);
				}
			}
		} else {
			levelMap2 = levelMap;
		}

		// spacing
		DagSpacing spacing = new DagSpacing(radius);

		// -- width and height --
		final int width = (levelMap2.size() + 1) * spacing.gridX;
		int maxCount = 0;
		for (SortedSet<Html5DagGenericNode> nodes : levelMap2.values()) {
			if (nodes.size() > maxCount) {
				maxCount = nodes.size();
			}
		}
		final int height = (maxCount + 1) * spacing.gridY;

		return new DagState(spacing, width, height, levelMap2);
	}

	/**
	 * Draw this DAG.
	 * <p>
	 * The cursor is assumed to rest in the upper left corner of the context.
	 * 
	 * @param context
	 * @param state
	 * @param showParameters
	 */
	public void draw(Context2D context, DagState state, boolean showParameters) {

		// -- compute coordinates --

		// spacing
		final int radius = state.spacing.radius;
		final int gridX = state.spacing.gridX;
		final int textShift = state.spacing.textShift;

		// loop over all levels
		for (Map.Entry<Integer, SortedSet<Html5DagGenericNode>> entry : state.getLevelMap().entrySet()) {
			int level = entry.getKey();
			SortedSet<Html5DagGenericNode> nodes = entry.getValue();
			final int spacing = state.height / (nodes.size() + 1);
			final int x = gridX * (1 + level);

			// loop over all nodes
			int indexBase1 = 1;
			for (Html5DagGenericNode node : nodes) {
				node.x = x;
				node.y = indexBase1 * spacing;
				indexBase1++;
			}
		}

		// draw edges
		for (Map.Entry<Integer, SortedSet<Html5DagGenericNode>> entry : state.getLevelMap().entrySet()) {
			for (Html5DagGenericNode node : entry.getValue()) {
				context.translate(node.x, node.y);
				for (DagEdge<Html5DagGenericNode> edge : node.getOutputs()) {
					Html5DagGenericNode output = edge.getOtherEnd(node);
					context.line(output.x - node.x, output.y - node.y);
					//TODO: add LINE here
					//context.textOneLine(edge.getName(), output.x - node.x);
				}
				context.translate(-node.x, -node.y);
			}
		}

		// draw nodes
		for (Map.Entry<Integer, SortedSet<Html5DagGenericNode>> entry : state.getLevelMap().entrySet()) {
			for (Html5DagGenericNode node : entry.getValue()) {

				// apply style information
				Style prevStyle = context.applyStyle(new Style(node.getStyle()));

				// go there
				context.translate(node.x, node.y);

				// shape
				context.circle(radius);

				// caption
				String caption = node.getCaption();
				/*if (caption.length() > 30) {
					caption = "too long";
				}*/
				context.textSplitLines(caption);

				// above: short id
				context.translate(0, -textShift);
				context.textOneLine(String.valueOf(node.getNumericId()), 2 * radius);
				context.translate(0, textShift);

				// below: parameters
				if (showParameters && node.parameters.size() > 0) {
					context.translate(0, textShift);
					boolean first = true;
					StringBuffer buf = new StringBuffer();
					for (Parameter param : node.parameters) {

						// do not show caption twice
						if (param.value.equals(node.caption)) {
							continue;
						}

						// output parameter
						if (first) {
							first = false;
						} else {
							buf.append(", ");
						}
						buf.append(param.value);
					}
					context.textOneLine(buf.toString(), 2 * radius);
					context.translate(0, -textShift);
				}

				// go back
				context.translate(-node.x, -node.y);

				// restore style information
				context.applyStyle(prevStyle);
			}
		}
	}

	/**
	 * Output this DAG in a file to look at.
	 * 
	 * @param filename
	 *            full path.
	 * @throws IOException
	 */
	public void drawToFile(String filename) throws IOException {

		// width and height
		DagState state = getBounds(40, 1);
		int width = state.width;
		int height = state.height;

		// render
		StringBuffer html = new StringBuffer();
		Canvas canvas = new Canvas(html, "dag" + filename, width, height);
		canvas.open();
		Context2D context = canvas.getContext2D();
		draw(context, state, true);
		canvas.close();

		// write to file
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		writer.write(HtmlHeaderTools.getXHtmlStrictHeader("en").toString());
		writer.write("<body>\n\n");

		writer.write(html.toString());

		writer.write("</body>\n");
		writer.write("</html>");
		writer.close();
	}

	/**
	 * Get type boundaries map.
	 * <p>
	 * the boundary map holds lowest and highest level for a given type (lowest
	 * inclusive, highest exclusive).
	 * 
	 * @param nodeAndLevelColl
	 *            collection of nodes with levels (minimum levels, mixed types).
	 * @return
	 */
	private Map<Integer, TypeBoundaries> getBoundaryMap(Collection<DagNodeAndLevel<Html5DagGenericNode>> nodeAndLevelColl) {
		Map<Integer, TypeBoundaries> boundaryMap = new HashMap<Integer, TypeBoundaries>();
		for (DagNodeAndLevel<Html5DagGenericNode> nodeAndLevel : nodeAndLevelColl) {
			int type = nodeAndLevel.node.type;
			int level = nodeAndLevel.level;

			// update type boundaries
			TypeBoundaries typeBoundaries = boundaryMap.get(type);
			if (typeBoundaries == null) {
				typeBoundaries = new TypeBoundaries(level, level + 1);
				boundaryMap.put(type, typeBoundaries);
			} else {
				if (level < typeBoundaries.from) {
					typeBoundaries.from = level;
				}
				if (level >= typeBoundaries.to) {
					typeBoundaries.to = level + 1;
				}
			}
		}
		return boundaryMap;
	}

	public Map<Integer, TypeBoundaries> getBoundaryMap() {
		return getBoundaryMap(getNodesWithLevels());
	}

	/**
	 * Get offset map.
	 * <p>
	 * The offset map holds shift offsets for each type.
	 * <p>
	 * Mapping: (type -> shift offset)
	 * 
	 * @param types
	 *            ordered list of types.
	 * @param boundaryMap
	 *            boundaries for each type.
	 * @return offsets for each type.
	 */
	private Map<Integer, Integer> getOffsetMap(List<Integer> types, Map<Integer, TypeBoundaries> boundaryMap) {
		Map<Integer, Integer> offsetMap = new HashMap<Integer, Integer>();
		int previousEnd = 0;
		for (Integer type : types) {
			TypeBoundaries typeBoundaries = boundaryMap.get(type);
			offsetMap.put(type, previousEnd - typeBoundaries.from);
			previousEnd += typeBoundaries.getWidth();
		}
		return offsetMap;
	}

	/**
	 * Get level map.
	 * <p>
	 * The level map holds a sorted list of all nodes for each level.
	 * <p>
	 * Mapping: (level -> nodes in this level)
	 * 
	 * @param nodeAndLevelColl
	 * @param offsetMap
	 * @return
	 */
	private SortedMap<Integer, SortedSet<Html5DagGenericNode>> getLevelMap(
			Collection<DagNodeAndLevel<Html5DagGenericNode>> nodeAndLevelColl, Map<Integer, Integer> offsetMap) {
		SortedMap<Integer, SortedSet<Html5DagGenericNode>> levelMap = new TreeMap<Integer, SortedSet<Html5DagGenericNode>>();
		int maxLevel = 0;
		for (DagNodeAndLevel<Html5DagGenericNode> nodeAndLevel : nodeAndLevelColl) {
			int level = nodeAndLevel.level + offsetMap.get(nodeAndLevel.node.type);
			if (level > maxLevel) {
				maxLevel = level;
			}
			SortedSet<Html5DagGenericNode> nodes = levelMap.get(level);
			if (nodes == null) {
				nodes = new TreeSet<Html5DagGenericNode>(new DagNodeComparator());
				levelMap.put(level, nodes);
			}
			nodes.add(nodeAndLevel.node);
		}

		// ensure every level exists - even if empty
		for (int i = 0; i < maxLevel; i++) {
			SortedSet<Html5DagGenericNode> nodes = levelMap.get(i);
			if (nodes == null) {
				levelMap.put(i, new TreeSet<Html5DagGenericNode>(new DagNodeComparator()));
			}
		}
		return levelMap;
	}

	/**
	 * Compact given level map.
	 * 
	 * @param levelMap
	 * @param types
	 * @param offsetMap
	 * @param boundaryMap
	 */
	private void compactLevelMap(SortedMap<Integer, SortedSet<Html5DagGenericNode>> levelMap, List<Integer> types,
			Map<Integer, Integer> offsetMap, Map<Integer, TypeBoundaries> boundaryMap) {

		// collect level changes
		List<LevelChange> levelChanges = new ArrayList<LevelChange>();

		// loop over all types
		// - first type is already fully compact
		for (int typeIndex = 1; typeIndex < types.size(); typeIndex++) {
			final int type = types.get(typeIndex);
			final int levelFrom = boundaryMap.get(type).from + offsetMap.get(type);
			final int levelTo = levelFrom + boundaryMap.get(type).getWidth();

			// for each type create a map from seen ids to their level
			Map<String, Integer> seenIdsToLevelMap = new HashMap<String, Integer>();

			// loop over all levels of this type
			for (int level = levelFrom; level < levelTo; level++) {

				// loop over all nodes of this level
				for (Html5DagGenericNode node : levelMap.get(level)) {
					int minLevel = levelFrom;

					// loop over all the inputs of this node
					for (DagEdge<Html5DagGenericNode> edge : node.getInputs()) {
						Html5DagGenericNode input = edge.getOtherEnd(node);
						Integer levelSeen = seenIdsToLevelMap.get(input.getUniqueId());
						if (levelSeen == null) {
							// input is of a previous type, min level stays
						} else {
							if (levelSeen + 1 > minLevel) {
								minLevel = levelSeen + 1;
							}
						}
					}

					// if a smaller level was found, move node down
					if (minLevel < level) {
						levelChanges.add(new LevelChange(node, level, minLevel));
					}

					// remember level of this node
					seenIdsToLevelMap.put(node.getUniqueId(), minLevel);
				}
			}
		}

		// apply level changes
		for (LevelChange levelChange : levelChanges) {
			levelMap.get(levelChange.levelSrc).remove(levelChange.node);
			levelMap.get(levelChange.levelDest).add(levelChange.node);
		}
	}

	// ------------------------------------------------------- object overrides

	@Override
	public String toString() {
		return getUniqueId();
	}

}
