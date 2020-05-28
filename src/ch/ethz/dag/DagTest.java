package ch.ethz.dag;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Another test for our DAG.
 * 
 * @author Lukas Blunschi
 * 
 */
public class DagTest extends TestCase {

	public static void main(String[] args) {
		DagTest test = new DagTest();
		test.testCreate();
		test.testConnect();
		test.testEqualsDag();
		test.testSorting();
		test.testAcyclic();
		test.testLevels();
		test.testRemove();
	}

	public void testCreate() {
		getSimpleDag();
		getExampleDag1();
		getExampleCycle();
		// passes if no exception
	}

	public void testConnect() {
		// http://en.wikipedia.org/wiki/Directed_acyclic_graph
		DagGenericNode dag = new DagGenericNode("5");
		dag.addEdge("5", "11");
		dag.addEdge("11", "2");
		dag.addEdge("11", "9");
		dag.addEdge("11", "10");

		dag.addEdge("3", "8");
		dag.addEdge("8", "9");
		dag.addEdge("3", "10");

		dag.addEdge("7", "11");
		dag.addEdge("7", "8");
		if (!dag.isDagConnected()) {
			fail();
		}
		if (!dag.equalsDag(getExampleDag1())) {
			fail();
		}

		// ensure deterministic sorting
		List<DagGenericNode> sortedNodesDag = dag.sortTopological();
		List<DagGenericNode> sortedNodesExample = getExampleDag1().sortTopological();
		for (int i = 0; i < sortedNodesExample.size(); i++) {
			if (!sortedNodesExample.get(i).getUniqueId().equals(sortedNodesDag.get(i).getUniqueId())) {
				System.err.println("Example 1 sorted: " + getExampleDag1().sortTopological());
				System.err.println("Manual  1 sorted: " + dag.sortTopological());
				fail();
			}
		}
	}

	public void testEqualsDag() {
		DagGenericNode dag1 = getExampleDag1();
		DagGenericNode dag2 = getExampleDag1();
		if (!dag1.equalsDag(dag2)) {
			fail();
		}
		dag2.addEdge("5", "10");
		if (dag1.equalsDag(dag2)) {
			fail();
		}
	}

	public void testSorting() {
		List<DagGenericNode> sortedNodes = getExampleDag1().sortTopological();
		String[] result = new String[] { "5", "7", "11", "3", "10", "2", "8", "9" };
		for (int i = 0; i < result.length; i++) {
			if (!sortedNodes.get(i).getUniqueId().equals(result[i])) {
				System.err.println("Sorted: " + sortedNodes);
				fail();
			}
		}
	}

	public void testAcyclic() {

		// DAG cycle 1
		DagGenericNode dag1 = getExampleCycle();
		assertNull(dag1.sortTopological());

		// DAG cycle 2
		DagGenericNode dag2 = getExampleCycle2();
		assertNull(dag2.sortTopological());
	}

	public void testLevels() {
		DagGenericNode dag = null;
		Set<String> answer = null;
		Collection<DagNodeAndLevel<DagGenericNode>> result = null;

		// simple example
		dag = getSimpleDag();
		answer = new HashSet<String>();
		answer.add("11:1");
		answer.add("12:1");
		answer.add("1:0");
		result = dag.getNodesWithLevels();
		for (DagNodeAndLevel<DagGenericNode> nodeAndLevel : result) {
			if (!answer.contains(nodeAndLevel.toString())) {
				System.out.println(nodeAndLevel);
				fail();
			}
		}
		// simple example
		dag = getExampleDag1();
		answer = new HashSet<String>();
		answer.add("7:0");
		answer.add("5:0");
		answer.add("3:0");
		answer.add("11:1");
		answer.add("8:1");
		answer.add("2:2");
		answer.add("9:2");
		answer.add("10:2");
		result = dag.getNodesWithLevels();
		for (DagNodeAndLevel<DagGenericNode> nodeAndLevel : result) {
			if (!answer.contains(nodeAndLevel.toString())) {
				System.out.println(nodeAndLevel);
				fail();
			}
		}
	}

	public void testRemove() {
		DagGenericNode dag = getSimpleDag();
		dag.removeNodeByUniqueId("11");
		DagGenericNode dag2 = new DagGenericNode("1");
		dag2.addEdge("1", "12");
		if (!dag.equalsDag(dag2)) {
			fail();
		}
	}

	private DagGenericNode getSimpleDag() {
		DagGenericNode node1 = new DagGenericNode("1");
		node1.addOutput("11");
		node1.addOutput("12");
		return node1;
	}

	private DagGenericNode getExampleDag1() {
		// http://en.wikipedia.org/wiki/Directed_acyclic_graph
		DagGenericNode node5 = new DagGenericNode("5");
		DagGenericNode node11 = node5.addOutput("11");
		node11.addOutput("2");
		node11.addOutput("9");
		node11.addOutput("10");
		DagGenericNode node7 = node11.addInput("7");
		DagGenericNode node8 = node7.addOutput("8");
		node8.addEdge("8", "9");
		DagGenericNode node3 = node8.addInput("3");
		node3.addEdge("3", "10");
		return node3;
	}

	private DagGenericNode getExampleCycle() {
		DagGenericNode dag = new DagGenericNode("1");
		dag.addEdge("1", "2");
		dag.addEdge("1", "3");
		dag.addEdge("2", "3");
		dag.addEdge("3", "4");
		dag.addEdge("4", "1");
		return dag;
	}

	private DagGenericNode getExampleCycle2() {
		DagGenericNode dag = new DagGenericNode("1");
		dag.addEdge("2", "1");
		dag.addEdge("3", "2");
		dag.addEdge("2", "3");
		return dag;
	}

}
