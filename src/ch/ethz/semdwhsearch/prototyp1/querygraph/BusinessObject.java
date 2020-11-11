package ch.ethz.semdwhsearch.prototyp1.querygraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.dag.DagEdge;
import ch.ethz.rdf.dag.RdfDagNode;

/**
 * A business object.
 * 
 * @author Lukas Blunschi
 * 
 */
public class BusinessObject implements HasTables, HasScore {

	// ---------------------------------------------------------------- counter

	private static int counter = 0;

	static void resetCounter() {
		counter = 0;
	}

	// ---------------------------------------------------------------- members

	public final String uri;

	public final String key;

	public final String value;

	public final String srcLink;

	private double score;

	private final List<Table> tables;
	
	public  boolean negated = false;
	
	public String className = null;
	
	public String propName = null;
	
	public Double pageRank = null;
	
	public String operator = null; // > < >= <=

	public BusinessObject(String key, String value, String srcLink, String className, String propName, Double pageRank, boolean negated, String operator) {
		this(QueryGraph.URI_BUSINESSOBJECT_PREFIX + (++counter), key, value, srcLink, className, propName, pageRank, negated, operator);
	}
	
	private BusinessObject(String uri, String key, String value, String srcLink, String className, String propName, Double pageRank, boolean negated, String operator) {
		if (uri == null) {
			throw new RuntimeException("uri must not be null!");
		}
		if (key == null) {
			throw new RuntimeException("key must not be null!");
		}
		if (value == null) {
			throw new RuntimeException("value must not be null!");
		}
		if (srcLink == null) {
			throw new RuntimeException("srcLink must not be null!");
		}
		this.uri = uri;
		this.key = key;
		this.value = value;
		this.srcLink = srcLink;
		this.score = -1.0;
		this.tables = new ArrayList<Table>();
		this.negated = negated;
		this.className = className;
		this.propName = propName;
		this.pageRank = pageRank;
		this.operator = operator;
	}

	// -------------------------------------------------------------------- uri

	public String getUri() {
		return uri;
	}

	// ------------------------------------------------------------------ score

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	// ------------------------------------------------------------------ table

	public Table addTable(Table table) {
		for (Table tableCur : tables) {
			if (tableCur.equals(table)) {
				return tableCur;
			}
		}
		// add
		this.tables.add(table);
		return table;
	}

	public void replaceTable(Table tableNew) {
		for (int i = 0; i < tables.size(); i++) {
			if (tables.get(i).equals(tableNew)) {
				tables.set(i, tableNew);
			}
		}
	}

	public Table getTable(String name) {
		for (Table tableCur : tables) {
			
			if (tableCur.name.equals(name)) {
				return tableCur;
			}
		}
		return null;
	}

	public List<Table> getTables() {
		return tables;
	}

	/**
	 * Get source column of this business object.
	 * <p>
	 * If this business object points to a table or some other node in the
	 * metadata graph except a column node, then this method returns null.
	 * 
	 * @return column matching this business object, null otherwise.
	 */
	public Column getSourceColumn() {
		for (Table tableCur : tables) {
			for (Column columnCur : tableCur.getColumns()) {
				if (srcLink.equals(columnCur.srcLink)) {
					return columnCur;
				}
			}
		}
		return null;
	}

	// ---------------------------------------------------------- relationships

	public Set<Relationship> getRelationships() {
		Set<Relationship> result = new HashSet<Relationship>();
		for (Table tableCur : tables) {
			result.addAll(tableCur.getRelationships());
		}
		return result;
	}

	// ---------------------------------------------------------- serialization

	public void toDag(RdfDagNode dag) {
		RdfDagNode boNode = dag.getByUniqueId(uri);
		if (boNode == null) {
			boNode = dag.addNode(uri);
		}
		boNode.addEdge(QueryGraph.EDGE_ISTYPE, uri, QueryGraph.URI_BUSINESSOBJECT_PREFIX + "type");
		boNode.addEdge(QueryGraph.EDGE_SRCLINK, uri, srcLink);
		boNode.addLiteral(QueryGraph.EDGE_KEY, key);
		boNode.addLiteral(QueryGraph.EDGE_VALUE, value);
		boNode.addLiteral(QueryGraph.EDGE_IS_NEGATED, negated == true? "true": "false");
		boNode.addLiteral(QueryGraph.CLASS_NAME, className);
		boNode.addLiteral(QueryGraph.PROP_NAME, propName);
		boNode.addLiteral(QueryGraph.PAGE_RANK, pageRank.toString());
		boNode.addLiteral(QueryGraph.OPERATOR, operator);
		if (score >= 0.0) {
			boNode.addLiteral(QueryGraph.EDGE_HASRANKING, String.valueOf(score));
		}
		for (Table table : tables) {
			table.toDag(boNode);
			boNode.addEdge(QueryGraph.EDGE_TABLE, uri, table.uri);
		}
	}

	public static BusinessObject fromDag(RdfDagNode boNode) {
		String uri = boNode.getUniqueId();
		String key = boNode.getLiteralValue(QueryGraph.EDGE_KEY);
		String value = boNode.getLiteralValue(QueryGraph.EDGE_VALUE);
		RdfDagNode srcLinkNode = boNode.getOutputs(QueryGraph.EDGE_SRCLINK).iterator().next().getOtherEnd(boNode);
		String srcLink = srcLinkNode.getUniqueId();
		String className = boNode.getLiteralValue(QueryGraph.CLASS_NAME);
		String propName = boNode.getLiteralValue(QueryGraph.PROP_NAME);		
		Double pageRank = Double.parseDouble(boNode.getLiteralValue(QueryGraph.PAGE_RANK));
		String operator = boNode.getLiteralValue(QueryGraph.OPERATOR);
		BusinessObject bo = new BusinessObject(uri, key, value, srcLink, className, propName, pageRank, false, operator);
		String scoreText = boNode.getLiteralValue(QueryGraph.EDGE_HASRANKING);
		boolean negated = boNode.getLiteralValue(QueryGraph.EDGE_IS_NEGATED).equals("true") ? true : false;
		bo.negated = negated;
		if (scoreText != null) {
			bo.setScore(Double.parseDouble(scoreText));
		}
		for (DagEdge<RdfDagNode> edge : boNode.getOutputs(QueryGraph.EDGE_TABLE)) {
			RdfDagNode tableNode = edge.getOtherEnd(boNode);
			Table table = Table.fromDag(tableNode);
			bo.addTable(table);
		}
		return bo;
	}

	// ------------------------------------------------------- object overrides

	@Override
	public int hashCode() {
		//TODO: here we would actually want to compare in the same way we do TERMS, by class and property name
		//also probably NOT taking into account the value, since it's irrelevant, we want to create a filter on class and prop
		//if (className != null and propName != null)
		//return(className + propName).hashCode();
		//else
		return (value + srcLink).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		//TODO: here also should actually compare class and property names
		if (obj instanceof BusinessObject) {
			BusinessObject bo = (BusinessObject) obj;
			return bo.key.equals(key) && bo.value.equals(value) && bo.srcLink.equals(srcLink);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return uri + " value=" + value + ", srcLink=" + srcLink + ", class="+ className + ", prop="+ propName;
	}

}

