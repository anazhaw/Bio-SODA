package ch.ethz.semdwhsearch.prototyp1.querygraph;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ch.ethz.dag.DagEdge;
import ch.ethz.rdf.dag.RdfDagNode;

/**
 * An operator.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Operator {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	public final String value;

	private BusinessObject operand1;

	private BusinessObject operand2bo;

	private Unknown operand2unknown;

	public Operator(String value) {
		this(QueryGraph.URI_OPERATOR_PREFIX + (++counter), value);
	}

	private Operator(String uri, String value) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		if (value == null) {
			throw new RuntimeException("value must not be null!");
		}
		this.uri = uri;
		this.value = value;
	}

	// --------------------------------------------------------------- operands

	public void setOperand1(BusinessObject operand1) {
		this.operand1 = operand1;
	}

	public BusinessObject getOperand1() {
		return operand1;
	}

	public BusinessObject getOperand2bo() {
		return operand2bo;
	}

	public void setOperand2bo(BusinessObject operand2bo) {
		this.operand2bo = operand2bo;
	}

	public Unknown getOperand2unknown() {
		return operand2unknown;
	}

	public void setOperand2unknown(Unknown operand2unknown) {
		this.operand2unknown = operand2unknown;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode opNode = dag.getByUniqueId(uri);
		if (opNode == null) {
			opNode = dag.addNode(uri);
		}
		opNode.addEdge(QueryGraph.EDGE_ISTYPE, uri, QueryGraph.URI_OPERATOR_PREFIX + "type");
		opNode.addLiteral(QueryGraph.EDGE_VALUE, value);
		if (operand1 != null) {
			RdfDagNode boNode = dag.getByUniqueId(operand1.uri);
			dag.addEdge(QueryGraph.EDGE_OPERAND1, uri, boNode.getUniqueId());
		}
		if (operand2bo != null) {
			RdfDagNode boNode = dag.getByUniqueId(operand2bo.uri);
			dag.addEdge(QueryGraph.EDGE_OPERAND2, uri, boNode.getUniqueId());
		}
		if (operand2unknown != null) {
			RdfDagNode ukNode = dag.getByUniqueId(operand2unknown.uri);
			dag.addEdge(QueryGraph.EDGE_OPERAND2, uri, ukNode.getUniqueId());
		}
	}

	public static Operator fromDag(RdfDagNode opNode, List<BusinessObject> bos, List<Unknown> uks) {
		String uri = opNode.getUniqueId();
		String value = opNode.getLiteralValue(QueryGraph.EDGE_VALUE);
		Operator op = new Operator(uri, value);
		Collection<DagEdge<RdfDagNode>> op1List = opNode.getOutputs(QueryGraph.EDGE_OPERAND1);
		if (op1List.size() > 0) {
			RdfDagNode boNode = op1List.iterator().next().getOtherEnd(opNode);
			BusinessObject bo = null;
			Iterator<BusinessObject> iter = bos.iterator();
			while (bo == null && iter.hasNext()) {
				BusinessObject boCur = iter.next();
				if (boCur.uri.equals(boNode.getUniqueId())) {
					bo = boCur;
				}
			}
			if (bo == null) {
				throw new RuntimeException("unable to connect operator to business object: op=" + op + ", bo uri="
						+ boNode.getUniqueId());
			}
			op.setOperand1(bo);
		}
		Collection<DagEdge<RdfDagNode>> op2List = opNode.getOutputs(QueryGraph.EDGE_OPERAND2);
		if (op2List.size() > 0) {
			RdfDagNode op2Node = op2List.iterator().next().getOtherEnd(opNode);
			RdfDagNode op2TypeNode = op2Node.getOutputs(QueryGraph.EDGE_ISTYPE).iterator().next().getOtherEnd(op2Node);
			if (op2TypeNode.getUniqueId().equals(QueryGraph.URI_BUSINESSOBJECT_PREFIX + "type")) {
				BusinessObject bo = null;
				Iterator<BusinessObject> iter = bos.iterator();
				while (bo == null && iter.hasNext()) {
					BusinessObject boCur = iter.next();
					if (boCur.uri.equals(op2Node.getUniqueId())) {
						bo = boCur;
					}
				}
				op.setOperand2bo(bo);
			} else {
				Unknown uk = null;
				Iterator<Unknown> iter = uks.iterator();
				while (uk == null && iter.hasNext()) {
					Unknown ukCur = iter.next();
					if (ukCur.uri.equals(op2Node.getUniqueId())) {
						uk = ukCur;
					}
				}
				op.setOperand2unknown(uk);
			}
		}
		return op;
	}

	// ------------------------------------------------------- object overrides

	public String toString() {
		return uri + " value=" + value;
	}

}
