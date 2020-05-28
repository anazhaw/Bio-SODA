package ch.ethz.dag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base class for all DAG node implementations.
 * 
 * @author Lukas Blunschi
 * 
 * @param <T>
 *            concrete type.
 */
public abstract class DagAbstractNode<T extends DagAbstractNode<T>> implements DagNode<T> {

	private static final Logger logger = LoggerFactory.getLogger(DagAbstractNode.class);

	// --------------------------------------------------------- shared members

	/**
	 * A map of all nodes in this DAG from unique id to the node.
	 * <p>
	 * Every node in the DAG shares this map with all the other nodes.
	 */
	//private
	final SortedMap<String, T> idMap;

	// ---------------------------------------------------------------- members
	
	//private
	int numericId;
	
	//private
	final String uniqueId;
	
	//private
	final TreeSet<DagEdge<T>> inputs;
	
	//private
	final TreeSet<DagEdge<T>> outputs;

	/**
	 * A flag to indicate if this node is connected to the main DAG.
	 */
	boolean connected;

	// ----------------------------------------------------------- construction

	protected DagAbstractNode(SortedMap<String, T> idMap, String uniqueId) {
		this.numericId = idMap.size() + 1;
		this.idMap = idMap;
		this.uniqueId = uniqueId;
		this.inputs = new TreeSet<DagEdge<T>>(new DagEdgeComparator(this));
		this.outputs = new TreeSet<DagEdge<T>>(new DagEdgeComparator(this));
		this.connected = true;
	}

	protected final void addNode(T node) {
		idMap.put(node.getUniqueId(), node);
	}

	protected abstract T getNewNode(SortedMap<String, T> idMap, String uniqueId);

	// -------------------------------------------------------- field accessors

	public int getNumericId() {
		return numericId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public SortedSet<DagEdge<T>> getInputs() {
		return inputs;
	}

	/**
	 * Get inputs of this node for the given edge name.
	 * 
	 * @param edgeName
	 * @return
	 */
	public Collection<DagEdge<T>> getInputs(String edgeName) {
		List<DagEdge<T>> result = new ArrayList<DagEdge<T>>();
		for (DagEdge<T> edge : inputs) {
			if (edge.getName() != null && edge.getName().equals(edgeName)) {
				result.add(edge);
			}
		}
		return result;
	}

	/**
	 * Get inputs of this node for all given edge names.
	 * 
	 * @param edgeNames
	 *            use these edge names.
	 * @return
	 */
	public Collection<DagEdge<T>> getInputs(Set<String> edgeNames) {
		List<DagEdge<T>> result = new ArrayList<DagEdge<T>>();
		for (DagEdge<T> edge : inputs) {
			if (edge.getName() != null && edgeNames.contains(edge.getName())) {
				result.add(edge);
			}
		}
		return result;
	}

	public SortedSet<DagEdge<T>> getOutputs() {
		return outputs;
	}

	/**
	 * Get outputs of this node for the given edge name.
	 * 
	 * @param edgeName
	 * @return
	 */
	public Collection<DagEdge<T>> getOutputs(String edgeName) {
		List<DagEdge<T>> result = new ArrayList<DagEdge<T>>();
		for (DagEdge<T> edge : outputs) {
			if (edge.getName() != null && edge.getName().equals(edgeName)) {
				result.add(edge);
			}
		}
		return result;
	}

	/**
	 * Get outputs of this node for all given edge names.
	 * 
	 * @param edgeNames
	 * @return
	 */
	public Collection<DagEdge<T>> getOutputs(Set<String> edgeNames) {
		List<DagEdge<T>> result = new ArrayList<DagEdge<T>>();
		for (DagEdge<T> edge : outputs) {
			if (edge.getName() != null && edgeNames.contains(edge.getName())) {
				result.add(edge);
			}
		}
		return result;
	}

	// ------------------------------------------------------------ node access

	public T getByUniqueId(String uniqueId) {
		return idMap.get(uniqueId);
	}

	public Set<String> getAllIds() {
		return idMap.keySet();
	}

	public Collection<T> getAllNodes() {
		return idMap.values();
	}

	/**
	 * Copy output subgraph of this node (including the node itself)
	 * 
	 * @return output subgraph (never null)
	 */
	public T copyOutputSubdag() {
		return copyOutputSubdag(-1);
	}

	/**
	 * Copy output subgraph of this node (including the node itself)
	 * 
	 * @param distance
	 *            number of hops to follow while copying. -1 to follow
	 *            indefinitely.
	 * @return output subgraph (never null)
	 */
	public T copyOutputSubdag(int distance) {

		// create new connected dag
		T subdag = getNewNode(new TreeMap<String, T>(), uniqueId);
		subdag.numericId = numericId;
		subdag.connected = true;

		// recursively copy outputs
		T node = idMap.get(uniqueId);
		copyOutputsRecursively(subdag, node, distance);
		return subdag;
	}

	private void copyOutputsRecursively(T subdag, T node, int distance) {

		// edges
		for (DagEdge<T> outputEdge : node.outputs) {
			T output = outputEdge.getOtherEnd(node);
			subdag.addEdge(outputEdge.getName(), node.uniqueId, output.uniqueId);
			subdag.getByUniqueId(output.uniqueId).numericId = getByUniqueId(output.uniqueId).numericId;
			if (distance > 0 || distance < 0) {
				copyOutputsRecursively(subdag, output, distance - 1);
			}
		}

		// members of extensions
		copyNodeMembers(node, subdag.getByUniqueId(node.uniqueId));
	}

	protected abstract void copyNodeMembers(T src, T dst);

	// ---------------------------------------------------------- modifications

	public T addInput(String uniqueId) {
		T input = idMap.get(uniqueId);
		if (input == null) {
			input = getNewNode(idMap, uniqueId);
		}
		T current = idMap.get(this.uniqueId);
		DagEdge<T> edge = new DagEdgeImpl<T>(null, current, input);
		this.inputs.add(edge);
		input.outputs.add(edge);
		input.connected = this.connected;
		return input;
	}

	public T addOutput(String uniqueId) {
		T output = idMap.get(uniqueId);
		if (output == null) {
			output = getNewNode(idMap, uniqueId);
		}
		T current = idMap.get(this.uniqueId);
		DagEdge<T> edge = new DagEdgeImpl<T>(null, current, output);
		this.outputs.add(edge);
		output.inputs.add(edge);
		output.connected = this.connected;
		return output;
	}

	public T addNode(String uniqueId) {
		T node = getNewNode(idMap, uniqueId);
		node.connected = false;
		return node;
	}

	public void addEdge(String uniqueIdFrom, String uniqueIdTo) {
		addEdge(null, uniqueIdFrom, uniqueIdTo);
	}

	public void addEdge(String edgeName, String uniqueIdFrom, String uniqueIdTo) {
		if (uniqueIdFrom == null || uniqueIdTo == null) {
			logger.warn("Adding edge where at least one side is null! from=" + uniqueIdFrom + ", to=" + uniqueIdTo);
		}
		T from = idMap.get(uniqueIdFrom);
		T to = idMap.get(uniqueIdTo);
		if (from == null) {
			if (to == null) {
				// new part
				from = getNewNode(idMap, uniqueIdFrom);
				to = getNewNode(idMap, uniqueIdTo);
				from.connected = false;
				to.connected = false;
			} else {
				// attach to to
				from = getNewNode(idMap, uniqueIdFrom);
				if (!to.connected) {
					from.connected = false;
				}
			}
		} else {
			if (to == null) {
				// attach to from
				to = getNewNode(idMap, uniqueIdTo);
				if (!from.connected) {
					to.connected = false;
				}
			} else {
				// existing nodes
				// - connect and
				// - ensure connected flag is correctly updated
				if (from.connected && !to.connected) {
					// color to
					to.setConnectedRec();
				} else if (!from.connected && to.connected) {
					// color from
					from.setConnectedRec();
				}
			}
		}
		// add edge
		DagEdge<T> edge = new DagEdgeImpl<T>(edgeName, from, to);
		from.outputs.add(edge);
		to.inputs.add(edge);
	}

	// [mscmike] Remove Edge

	/**
	 * Removes an edge from the dag.
	 * 
	 * @param uniqueIdFrom
	 *            id of the source node of the edge.
	 * @param uniqueIdTo
	 *            id of the destination node of the edge.
	 * 
	 * @return The removed edge or null if no edge with specified start and end
	 *         nodes was found.
	 */
	public DagEdge<T> removeEdge(String uniqueIdFrom, String uniqueIdTo) {
		if (uniqueIdFrom == null || uniqueIdTo == null) {
			throw new IllegalArgumentException();
		}
		T from = idMap.get(uniqueIdFrom);
		T to = idMap.get(uniqueIdTo);

		// check if both nodes exist
		if (from == null || to == null) {
			return null;
		}

		// look for edges connecting from and to nodes
		for (DagEdge<T> edge : from.getOutputs()) {
			if (edge.getOtherEnd(from).equals(to)) {

				// remove edge
				from.getOutputs().remove(edge);
				to.getInputs().remove(edge);

				// ensure connected flag is up-to-date
				if (from.getOutputs().size() == 0 && from.getInputs().size() == 0) {
					from.connected = false;
				}
				if (to.getOutputs().size() == 0 && to.getInputs().size() == 0) {
					to.connected = false;
				}

				return edge;
			}
		}
		return null;
	}

	// -----------------------------------

	public T removeNodeByUniqueId(String uniqueId) {
		T node = idMap.remove(uniqueId);
		if (node != null) {
			for (DagEdge<T> edge : node.getInputs()) {
				T input = edge.getOtherEnd(node);
				input.getOutputs().remove(edge);
			}
			for (DagEdge<T> edge : node.getOutputs()) {
				T output = edge.getOtherEnd(node);
				output.getInputs().remove(edge);
			}
		}
		return node;
	}

	/**
	 * Remove all nodes after a given edge. Traversal starts at the current
	 * node.
	 * 
	 * @param edgeName
	 */
	public void removeSubdagAfterEdge(String edgeName) {
		T node = idMap.get(uniqueId);
		List<String> toRemove = new ArrayList<String>();
		removeSubdagAfterEdgeRec(node, edgeName, false, toRemove);
		for (String uniqueId : toRemove) {
			removeNodeByUniqueId(uniqueId);
		}
	}

	private void removeSubdagAfterEdgeRec(T node, String edgeName, final boolean remove, List<String> toRemove) {
		for (DagEdge<T> edge : node.getOutputs()) {
			T output = edge.getOtherEnd(node);
			boolean removeOutput = remove || edge.getName().equals(edgeName);
			removeSubdagAfterEdgeRec(output, edgeName, removeOutput, toRemove);
			if (removeOutput) {
				toRemove.add(output.uniqueId);
			}
		}
	}

	// --------------------------------------------------------- public methods

	public final boolean equalsDag(T dag) {

		// --- 1. equal node ids ---

		// loop over all nodes of this dag
		for (Map.Entry<String, T> entry : this.idMap.entrySet()) {
			T curNode = entry.getValue();

			// ensure given dag contains id of current node
			if (!dag.idMap.containsKey(curNode.getUniqueId())) {
				return false;
			}
		}

		// ensure no other nodes in given dag
		if (dag.idMap.size() != idMap.size()) {
			return false;
		}

		// --- 2. equal connections ---

		// loop over all nodes
		for (Map.Entry<String, T> entry : this.idMap.entrySet()) {
			T curNodeT = entry.getValue();
			T curNodeG = dag.idMap.get(curNodeT.getUniqueId());

			// inputs
			int numInputsT = curNodeT.getInputs().size();
			int numInputsG = curNodeG.getInputs().size();
			if (numInputsT != numInputsG) {
				return false;
			} else {
				Iterator<DagEdge<T>> iterT = curNodeT.getInputs().iterator();
				Iterator<DagEdge<T>> iterG = curNodeG.getInputs().iterator();
				for (int i = 0; i < numInputsT; i++) {
					T inputT = iterT.next().getOtherEnd(curNodeT);
					T inputG = iterG.next().getOtherEnd(curNodeG);
					if (!inputT.getUniqueId().equals(inputG.getUniqueId())) {
						return false;
					}
				}
			}

			// outputs
			int numOutputsT = curNodeT.getOutputs().size();
			int numOutputsG = curNodeG.getOutputs().size();
			if (numOutputsT != numOutputsG) {
				return false;
			} else {
				Iterator<DagEdge<T>> iterT = curNodeT.getOutputs().iterator();
				Iterator<DagEdge<T>> iterG = curNodeG.getOutputs().iterator();
				for (int i = 0; i < numOutputsT; i++) {
					T outputT = iterT.next().getOtherEnd(curNodeT);
					T outputG = iterG.next().getOtherEnd(curNodeG);
					if (!outputT.getUniqueId().equals(outputG.getUniqueId())) {
						return false;
					}
				}
			}
		}

		return true;
	}

	// ---------------------------------------------------------------- sorting

	/**
	 * Test if this DAG is really acyclic.
	 */
	public final boolean isAcyclic() {
		return sortTopological() != null;
	}

	/**
	 * Sort topological.
	 * 
	 * @return list of sorted nodes, null if dag contains cycles.
	 */
	public List<T> sortTopological() {
		List<String> seenIdsCur = new ArrayList<String>();
		Set<String> seenIdsEver = new HashSet<String>();
		List<T> sortedNodes = new ArrayList<T>();
		boolean acyclic = true;
		for (Map.Entry<String, T> entry : idMap.entrySet()) {
			T node = entry.getValue();
			seenIdsCur.clear();
			acyclic &= visitSortTopological(node, seenIdsCur, seenIdsEver, sortedNodes);
		}
		if (acyclic) {
			return sortedNodes;
		} else {
			return null;
		}
	}

	/**
	 * Visit the given node and recurse on all its inputs.
	 * 
	 * @param node
	 *            node to visit.
	 * @param seenIdsCur
	 *            previously seen ids for the current recursion.
	 * @param seenIdsEver
	 *            previously seen ids for all recursions.
	 * @param sortedNodes
	 *            result list.
	 * @return true if acyclic, false otherwise.
	 */
	private boolean visitSortTopological(T node, Collection<String> seenIdsCur, Set<String> seenIdsEver, List<T> sortedNodes) {

		// check for cycles
		String idCur = node.getUniqueId();
		if (seenIdsCur.contains(idCur)) {
			logger.warn("DAG contains cycles! seen IDs = " + seenIdsCur + " current id = " + idCur);
			return false;
		} else {
			seenIdsCur.add(idCur);
		}

		// only continue for not yet visited nodes
		boolean acyclic = true;
		if (!seenIdsEver.contains(idCur)) {
			seenIdsEver.add(idCur);

			// loop over inputs
			List<String> seenIdsCur2 = new ArrayList<String>();
			for (DagEdge<T> edge : node.getInputs()) {
				seenIdsCur2.clear();
				seenIdsCur2.addAll(seenIdsCur);
				acyclic &= visitSortTopological(edge.getOtherEnd(node), seenIdsCur2, seenIdsEver, sortedNodes);
			}

			// add node to sorted nodes
			sortedNodes.add(node);
		}
		return acyclic;
	}

	public Collection<DagNodeAndLevel<T>> getNodesWithLevels() {

		// loop over all nodes
		Map<String, DagNodeAndLevel<T>> seenNodes = new HashMap<String, DagNodeAndLevel<T>>();
		for (T node : idMap.values()) {
			visitNodesWithLevel(node, seenNodes);
		}
		return seenNodes.values();
	}

	private int visitNodesWithLevel(T node, Map<String, DagNodeAndLevel<T>> seenNodes) {

		// only compute for not yet visited nodes
		String idCur = node.getUniqueId();
		
		DagNodeAndLevel<T> nodeAndLevel = seenNodes.get(idCur);
		if (nodeAndLevel == null) {

			// find maximum input level and
			// add 1 to the maximum previous level
			int level = 0;
			for (DagEdge<T> edge : node.getInputs()) {
				// mark current node as seen and continue => 
				// avoids overflow when there are circular dependencies
				nodeAndLevel = new DagNodeAndLevel<T>(node, level);
				seenNodes.put(idCur, nodeAndLevel);
				int curLevel = visitNodesWithLevel(edge.getOtherEnd(node), seenNodes) + 1;
				if (curLevel > level) {
					level = curLevel;
				}
			}

			// store result
			nodeAndLevel = new DagNodeAndLevel<T>(node, level);
			seenNodes.put(idCur, nodeAndLevel);
		}

		// return current level
		return nodeAndLevel.level;
	}

	// ----------------------------------------------------------- connectivity

	public final boolean isDagConnected() {
		for (T node : idMap.values()) {
			if (!node.connected) {
				return false;
			}
		}
		return true;
	}

	protected final void setConnectedRec() {
		Set<String> seenIds = new HashSet<String>();
		setConnectedRecInternal(seenIds);
	}
	
	//private
	final void setConnectedRecInternal(Set<String> seenIds) {

		// connect myself
		this.connected = true;
		seenIds.add(this.getUniqueId());
		T current = idMap.get(this.uniqueId);

		// loop over unseen inputs
		for (DagEdge<T> edge : getInputs()) {
			T input = edge.getOtherEnd(current);
			if (!seenIds.contains(input.getUniqueId())) {
				input.setConnectedRecInternal(seenIds);
			}
		}

		// loop over unseen outputs
		for (DagEdge<T> edge : getOutputs()) {
			T output = edge.getOtherEnd(current);
			if (!seenIds.contains(output.getUniqueId())) {
				output.setConnectedRecInternal(seenIds);
			}
		}
	}

	/**
	 * Find all entry nodes (nodes not having any inputs)
	 * 
	 * @return IDs of all entry nodes
	 */
	public final Set<String> getEntryNodes() {
		Set<String> ids = new HashSet<String>();
		for (T node : idMap.values()) {
			if (node.inputs.size() == 0) {
				ids.add(node.getUniqueId());
			}
		}
		return ids;
	}

	// ---------------------------------------------------------- serialization

	/**
	 * Print this DAG in N-Triples.
	 * 
	 * @return all edges in N-Triple format.
	 */
	public String toNTriples() {
		StringBuffer txt = new StringBuffer();
		for (T node : getAllNodes()) {

			// outputs
			for (DagEdge<T> edge : node.getOutputs()) {
				T otherEnd = edge.getOtherEnd(node);
				txt.append("<").append(node.getUniqueId()).append("> ");
				txt.append("<").append(edge.getName()).append("> ");
				txt.append("<").append(otherEnd.getUniqueId()).append("> .\n");
			}

		}
		return txt.toString();
	}

	// ------------------------------------------------------- object overrides

	@Override
	public String toString() {
		return getUniqueId();
	}

}
