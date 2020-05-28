package ch.ethz.semdwhsearch.prototyp1.querygraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.dag.DagEdge;
import ch.ethz.rdf.dag.RdfDagNode;
import ch.ethz.semdwhsearch.prototyp1.constants.Constants;

/**
 * A query graph.
 * 
 * @author Lukas Blunschi
 * 
 */
public class QueryGraph {

	// -------------------------------------------------------------- constants

	public static final String URI_PREFIX = "http://ethz.ch/";

	public static final String URI_QGRAPH = URI_PREFIX + "qgraph";

	public static final String EDGE_CONTAINS = URI_PREFIX + "qgraph_contains";

	public static final String EDGE_ISTYPE = URI_PREFIX + "is_type";

	public static final String EDGE_VALUE = URI_PREFIX + "value";

	public static final String EDGE_MESSAGE = URI_PREFIX + "message";

	public static final String EDGE_PROBLEM = URI_PREFIX + "problem";

	public static final String EDGE_HASQUERY = URI_PREFIX + "has_query";

	public static final String EDGE_HASSQLS = URI_PREFIX + "has_sqls";

	public static final String EDGE_HASINFO = URI_PREFIX + "has_info";

	public static final String EDGE_HASRANKING = URI_PREFIX + "has_ranking";

	public static final String EDGE_JOINCONDITION = URI_PREFIX + "join_condition";

	public static final String EDGE_KEY = URI_PREFIX + "key";

	public static final String EDGE_RELATIONSHIP = URI_PREFIX + "relationship";

	public static final String EDGE_COLUMN = URI_PREFIX + "column";

	public static final String EDGE_TABLE = URI_PREFIX + "table";

	public static final String EDGE_SRCLINK = URI_PREFIX + "src_link";

	public static final String EDGE_BELONGS = URI_PREFIX + "belongs";

	public static final String EDGE_OPERAND1 = URI_PREFIX + "operand1";

	public static final String EDGE_OPERAND2 = URI_PREFIX + "operand2";

	public static final String EDGE_COLUMNNAME = URI_PREFIX + "column_name";

	public static final String EDGE_TABLENAME = URI_PREFIX + "table_name";

	public static final String EDGE_FILTERVALUE = URI_PREFIX + "filter_value";

	public static final String EDGE_ISPK = URI_PREFIX + "is_pk";

	public static final String EDGE_TABLE1 = URI_PREFIX + "table1";

	public static final String EDGE_TABLE2 = URI_PREFIX + "table2";

	public static final String EDGE_JOIN = URI_PREFIX + "join";

	public static final String EDGE_ISREDUCED = URI_PREFIX + "is_reduced";
	
	public static final String EDGE_IS_NEGATED = URI_PREFIX + "is_negated";

	public static final String URI_UNKNOWN_PREFIX = URI_PREFIX + "uk";

	public static final String URI_OPERATOR_PREFIX = URI_PREFIX + "op";

	public static final String URI_BUSINESSOBJECT_PREFIX = URI_PREFIX + "bo";

	public static final String URI_VALUE_PREFIX = URI_PREFIX + "val";

	public static final String URI_CONCEPTUALENTITY_PREFIX = URI_PREFIX + "ce";

	public static final String URI_CONCEPTUALATTRIBUTE_PREFIX = URI_PREFIX + "ca";

	public static final String URI_PK_PREFIX = URI_PREFIX + "pk";

	public static final String URI_COLUMN_PREFIX = URI_PREFIX + "col";

	public static final String URI_TABLE_PREFIX = URI_PREFIX + "table";

	public static final String URI_KEY_PREFIX = URI_PREFIX + "key";

	public static final String URI_JOINCONDITION_PREFIX = URI_PREFIX + "joincond";

	public static final String URI_RELATIONSHIP_PREFIX = URI_PREFIX + "rel";

	public static final String URI_JOINSET_PREFIX = URI_PREFIX + "joinset";

	public static final String URI_JOIN_PREFIX = URI_PREFIX + "join";

	public static final String URI_PROBLEM_PREFIX = URI_PREFIX + "problem";

	public static final String CLASS_NAME = "class";

	public static final String PROP_NAME = "prop";
	
	public static final String PAGE_RANK = "pageRank";

	// ---------------------------------------------------------------- members

	private final List<Unknown> unknowns;

	private final List<Operator> operators;

	private final List<BusinessObject> businessObjects;

	private final List<Value> values;

	private final List<JoinSet> joinsets;

	private String query;

	private String sqls;

	private String info;

	private final List<Problem> problems;

	private double score;

	public QueryGraph() {
		this.unknowns = new ArrayList<Unknown>();
		this.operators = new ArrayList<Operator>();
		this.businessObjects = new ArrayList<BusinessObject>();
		this.values = new ArrayList<Value>();
		this.joinsets = new ArrayList<JoinSet>();
		this.query = null;
		this.sqls = null;
		this.info = null;
		this.problems = new ArrayList<Problem>();
		this.score = Constants.INITIAL_SCORE;
	}

	public static void resetCounters() {
		Unknown.resetCounter();
		BusinessObject.resetCounter();
		Operator.resetCounter();
		Value.resetCounter();
		JoinSet.resetCounter();
		Join.resetCounter();
		Table.resetCounter();
		Column.resetCounter();
		Relationship.resetCounter();
		JoinCondition.resetCounter();
		Key.resetCounter();
		Problem.resetCounter();
	}

	// --------------------------------------------------------------- unknowns

	public Unknown addUnknown(String value) {
		Unknown uk = new Unknown(value);
		unknowns.add(uk);
		return uk;
	}

	public void addUnknown(Unknown uk) {
		unknowns.add(uk);
	}

	public List<Unknown> getUnknowns() {
		return unknowns;
	}

	// -------------------------------------------------------------- operators

	public Operator addOperator(String opName) {
		Operator op = new Operator(opName);
		operators.add(op);
		return op;
	}

	public void addOperator(Operator op) {
		operators.add(op);
	}

	public List<Operator> getOperators() {
		return operators;
	}

	// ------------------------------------------------------- business objects

	public BusinessObject addBusinessObject(String key, String value, String srcLink) {
		BusinessObject bo = new BusinessObject(key, value, srcLink, false);
		businessObjects.add(bo);
		return bo;
	}
	
	public BusinessObject addBusinessObject(String key, String value, String srcLink, String className, String propName, Double pageRank, boolean negated) {
		BusinessObject bo = new BusinessObject(key, value, srcLink, className, propName, pageRank, negated);
		businessObjects.add(bo);
		return bo;
	}

	public BusinessObject addBusinessObject(String key, String value, String srcLink, boolean negated) {
		BusinessObject bo = new BusinessObject(key, value, srcLink, negated);
		businessObjects.add(bo);
		return bo;
	}

	public void addBusinessObject(BusinessObject bo) {
		businessObjects.add(bo);
	}

	public List<BusinessObject> getBusinessObjects() {
		return businessObjects;
	}

	// ----------------------------------------------------------------- values

	public Value addValue(String key, String value, String srcLink) {
		Value val = new Value(key, value, srcLink);
		values.add(val);
		return val;
	}

	public void addValue(Value val) {
		values.add(val);
	}

	public List<Value> getValues() {
		return values;
	}

	// --------------------------------------------------------------- joinsets

	public JoinSet addJoinSet() {
		JoinSet joinset = new JoinSet();
		joinsets.add(joinset);
		return joinset;
	}

	public void addJoinSet(JoinSet joinset) {
		joinsets.add(joinset);
	}

	public List<JoinSet> getJoinSets() {
		return joinsets;
	}

	public boolean hasReducedJoinSet() {
		boolean hasReduced = false;
		for (JoinSet joinSet : joinsets) {
			hasReduced |= joinSet.isReduced();
		}
		return hasReduced;
	}

	// ------------------------------------------------------------------ query

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	// ------------------------------------------------------------------- sqls

	public String getSqls() {
		return sqls;
	}

	public void setSqls(String sqls) {
		this.sqls = sqls;
	}

	// ------------------------------------------------------------------- info

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	// ------------------------------------------------------------------ score

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	// --------------------------------------------------------------- problems

	public Problem addProblem(String message) {
		Problem problem = new Problem(message);
		problems.add(problem);
		return problem;
	}

	public void addProblem(Problem problem) {
		problems.add(problem);
	}

	public List<Problem> getProblems() {
		return problems;
	}

	/**
	 * Get problems
	 * 
	 * @return list of problem messages (maybe empty, but never null).
	 */
	public List<String> getProblemMessages() {
		List<String> messages = new ArrayList<String>();
		for (Problem problem : problems) {
			messages.add(problem.message);
		}
		return messages;
	}

	// ---------------------------------------------------------------- helpers

	public Set<Table> getAllTables() {
		Set<Table> result = new HashSet<Table>();
		for (BusinessObject bo : businessObjects) {
			result.addAll(bo.getTables());
		}
		for (Value val : values) {
			result.addAll(val.getTables());
		}
		return result;
	}

	/**
	 * Get table.
	 * 
	 * @param name
	 *            table of the table to fetch.
	 * @return table or null if no such table exists in this query graph.
	 */
	public Table getTable(String name) {
		for (Table table : getAllTables()) {
			if (table.name.equals(name)) {
				return table;
			}
		}
		return null;
	}

	private void mergeTables() {

		// case 1: merge bo.tables with val.tables
		// loop over tables of each business object
		for (BusinessObject bo : businessObjects) {
			for (Table tableBo : bo.getTables()) {

				// loop over tables of each value
				for (Value val : values) {
					for (Table tableVal : val.getTables()) {

						// look for tables with same name (this is unique)
						if (tableBo.name.equalsIgnoreCase(tableVal.name)) {

							// TODO do I have to merge member variables as well?

							val.replaceTable(tableBo);
						}
					}
				}

			}
		}

		// case 2: merge bo.tables with bo.tables
		for (BusinessObject bo1 : businessObjects) {
			for (Table tableBo1 : bo1.getTables()) {
				for (BusinessObject bo2 : businessObjects) {
					for (Table tableBo2 : bo2.getTables()) {
						if (tableBo1 == tableBo2) { // comparison by ID
							continue;
						}
						if (tableBo1.name.equalsIgnoreCase(tableBo2.name)) {
							bo2.replaceTable(tableBo1);
						}
					}
				}
			}
		}

		// case 3: merge val.tables with val.tables
		for (Value val1 : values) {
			for (Table tableVal1 : val1.getTables()) {
				for (Value val2 : values) {
					for (Table tableVal2 : val2.getTables()) {
						if (tableVal1 == tableVal2) { // comparison by ID
							continue;
						}
						if (tableVal1.name.equalsIgnoreCase(tableVal2.name)) {
							val2.replaceTable(tableVal1);
						}
					}
				}
			}
		}
	}

	// ---------------------------------------------------------- serialization

	public RdfDagNode getRdf() {
		RdfDagNode dag = new RdfDagNode(URI_QGRAPH);

		// types
		dag.addNode(URI_UNKNOWN_PREFIX + "type");
		dag.addNode(URI_OPERATOR_PREFIX + "type");
		dag.addNode(URI_BUSINESSOBJECT_PREFIX + "type");
		dag.addNode(URI_VALUE_PREFIX + "type");
		dag.addNode(URI_JOINSET_PREFIX + "type");
		dag.addNode(URI_PK_PREFIX + "type");

		// unknowns
		for (int ukIndex = 0; ukIndex < unknowns.size(); ukIndex++) {
			Unknown uk = unknowns.get(ukIndex);
			uk.toDag(dag);
			dag.addEdge(QueryGraph.EDGE_CONTAINS, QueryGraph.URI_QGRAPH, uk.uri);
		}

		// business objects
		for (int boIndex = 0; boIndex < businessObjects.size(); boIndex++) {
			BusinessObject bo = businessObjects.get(boIndex);
			bo.toDag(dag);
			dag.addEdge(QueryGraph.EDGE_CONTAINS, QueryGraph.URI_QGRAPH, bo.uri);
		}

		// operators
		for (int opIndex = 0; opIndex < operators.size(); opIndex++) {
			Operator op = operators.get(opIndex);
			op.toDag(dag);
			dag.addEdge(QueryGraph.EDGE_CONTAINS, QueryGraph.URI_QGRAPH, op.uri);
		}

		// values
		for (int valIndex = 0; valIndex < values.size(); valIndex++) {
			Value val = values.get(valIndex);
			val.toDag(dag);
			dag.addEdge(QueryGraph.EDGE_CONTAINS, QueryGraph.URI_QGRAPH, val.uri);
		}

		// joinsets
		for (int jsIndex = 0; jsIndex < joinsets.size(); jsIndex++) {
			JoinSet joinset = joinsets.get(jsIndex);
			joinset.toDag(dag);
			dag.addEdge(QueryGraph.EDGE_CONTAINS, QueryGraph.URI_QGRAPH, joinset.uri);
		}

		// query
		if (query != null) {
			dag.addLiteral(EDGE_HASQUERY, query);
		}

		// sql
		if (sqls != null) {
			dag.addLiteral(EDGE_HASSQLS, sqls);
		}

		// info
		if (info != null) {
			dag.addLiteral(EDGE_HASINFO, info);
		}

		// problems
		for (int prIndex = 0; prIndex < problems.size(); prIndex++) {
			Problem problem = problems.get(prIndex);
			problem.toDag(dag);
			dag.addEdge(QueryGraph.EDGE_PROBLEM, QueryGraph.URI_QGRAPH, problem.uri);
		}

		// score
		if (score >= 0.0) {
			dag.addLiteral(EDGE_HASRANKING, String.valueOf(score));
		}

		return dag;
	}

	public static QueryGraph parse(RdfDagNode dag) {
		QueryGraph qg = new QueryGraph();
		RdfDagNode qgNode = dag.getByUniqueId(QueryGraph.URI_QGRAPH);

		// query
		String query = qgNode.getLiteralValue(QueryGraph.EDGE_HASQUERY);
		if (query != null) {
			qg.setQuery(query);
		}

		// sqls
		String sqls = qgNode.getLiteralValue(QueryGraph.EDGE_HASSQLS);
		if (sqls != null) {
			qg.setSqls(sqls);
		}

		// info
		String info = qgNode.getLiteralValue(QueryGraph.EDGE_HASINFO);
		if (info != null) {
			qg.setInfo(info);
		}

		// score
		String scoreStr = qgNode.getLiteralValue(QueryGraph.EDGE_HASRANKING);
		if (scoreStr != null) {
			qg.setScore(Double.parseDouble(scoreStr));
		}

		// loop over problems
		for (DagEdge<RdfDagNode> edge : qgNode.getOutputs(QueryGraph.EDGE_PROBLEM)) {
			RdfDagNode prNode = edge.getOtherEnd(qgNode);
			Problem problem = Problem.fromDag(prNode);
			qg.addProblem(problem);
		}

		// loop over contains
		// 1st: unknowns, business objects and values
		for (DagEdge<RdfDagNode> edge : qgNode.getOutputs(QueryGraph.EDGE_CONTAINS)) {
			RdfDagNode nodeCont = edge.getOtherEnd(qgNode);
			RdfDagNode nodeType = nodeCont.getOutputs(QueryGraph.EDGE_ISTYPE).iterator().next().getOtherEnd(nodeCont);
			if (nodeType.getUniqueId().equals(QueryGraph.URI_UNKNOWN_PREFIX + "type")) {
				Unknown uk = Unknown.fromDag(nodeCont);
				qg.addUnknown(uk);
			} else if (nodeType.getUniqueId().equals(QueryGraph.URI_BUSINESSOBJECT_PREFIX + "type")) {
				BusinessObject bo = BusinessObject.fromDag(nodeCont);
				qg.addBusinessObject(bo);
			} else if (nodeType.getUniqueId().equals(QueryGraph.URI_VALUE_PREFIX + "type")) {
				Value val = Value.fromDag(nodeCont);
				qg.addValue(val);
			} else if (nodeType.getUniqueId().equals(QueryGraph.URI_JOINSET_PREFIX + "type")) {
				JoinSet joinset = JoinSet.fromDag(nodeCont);
				qg.addJoinSet(joinset);
			}
		}

		// loop over contains
		// 2nd: operators
		for (DagEdge<RdfDagNode> edge : qgNode.getOutputs(QueryGraph.EDGE_CONTAINS)) {
			RdfDagNode nodeCont = edge.getOtherEnd(qgNode);
			RdfDagNode nodeType = nodeCont.getOutputs(QueryGraph.EDGE_ISTYPE).iterator().next().getOtherEnd(nodeCont);
			if (nodeType.getUniqueId().equals(QueryGraph.URI_OPERATOR_PREFIX + "type")) {
				Operator op = Operator.fromDag(nodeCont, qg.getBusinessObjects(), qg.getUnknowns());
				qg.addOperator(op);
			}
		}

		// merge tables
		qg.mergeTables();

		return qg;
	}

}

