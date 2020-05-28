package ch.ethz.semdwhsearch.prototyp1.metadata;

import java.util.HashSet;
import java.util.Set;

import ch.ethz.dag.DagEdge;
import ch.ethz.html5.dag.Html5DagGenericNode;
import ch.ethz.html5.dag.Parameter;
import ch.ethz.semdwhsearch.prototyp1.metadata.mapping.MetadataMapping;

/**
 * Dynamically created base data node.
 * 
 * @author Lukas Blunschi
 * 
 */
public class BasedataNode {

	private final MetadataMapping mapping;

	private final String pnPCName;

	private final String pnPTName;

	private final String pnLogAttr;

	private final String pnPhysCol;

	private final Set<String> pnSetTable;

	private final String basePath;

	public final String uri;

	public final ColumnName columnName;

	public final String value;

	public BasedataNode(String uri) {

		// metadata
		Metadata metadata = MetadataSingleton.getInstance().getMetadata();
		this.mapping = metadata.getMapping();

		// config
		this.pnPCName = mapping.getSchemaPropNamePhysicalColumnName();
		this.pnPTName = mapping.getSchemaPropNamePhysicalTableName();
		this.pnLogAttr = mapping.getSchemaPropNameLogicalAttr();
		this.pnPhysCol = mapping.getSchemaPropNamePhysicalColumn();
		this.pnSetTable = new HashSet<String>();
		pnSetTable.add(pnLogAttr);
		pnSetTable.add(pnPhysCol);

		// paths
		this.basePath = mapping.getBasedataBasePath();
		this.uri = uri;

		// derived members
		this.columnName = getColumnName();
		this.value = getValue();
	}

	public BasedataNode(ColumnName columnName, String value) {
		this(columnName.name, value);
	}

	public BasedataNode(String columnName, String value) {

		// metadata
		Metadata metadata = MetadataSingleton.getInstance().getMetadata();
		this.mapping = metadata.getMapping();

		// config
		this.pnPCName = mapping.getSchemaPropNamePhysicalColumnName();
		this.pnPTName = mapping.getSchemaPropNamePhysicalTableName();
		this.pnLogAttr = mapping.getSchemaPropNameLogicalAttr();
		this.pnPhysCol = mapping.getSchemaPropNamePhysicalColumn();
		this.pnSetTable = new HashSet<String>();
		pnSetTable.add(pnLogAttr);
		pnSetTable.add(pnPhysCol);

		// paths
		this.basePath = mapping.getBasedataBasePath();
		String path = columnName.replaceAll("\\.", "/");
		this.uri = basePath + path + "/" + value;

		// derived members
		this.columnName = getColumnName();
		this.value = getValue();
	}

	private ColumnName getColumnName() {
		String tmp = uri.substring(basePath.length());
		int pos1 = tmp.indexOf("/");
		int pos2 = tmp.indexOf("/", pos1 + 1);
		String tableNameStr = tmp.substring(0, pos1);
		String columnNameShortStr = tmp.substring(pos1 + 1, pos2);
		// TODO this is a hack
		String srcLink = mapping.getSchemaBasePath() + "logical/" + tableNameStr;
		ColumnName columnName = new ColumnName(tableNameStr, columnNameShortStr, uri, srcLink);
		return columnName;
	}

	private String getValue() {
		return uri.substring(basePath.length() + columnName.name.length() + 1);
	}

	/**
	 * Get URI to column where this base data node is attached to.
	 * 
	 * @param dag
	 * @return URI or null if table/column is not found in given schema DAG.
	 */
	public String getUriToColumn(Html5DagGenericNode dag) {

		// find column node to attach to
		// - 1. find correct table (table names are unique)
		// - 2. find correct column (column names within one table are unique)
		String uriToColumn = null;
		Parameter paramTable = new Parameter(pnPTName, columnName.getTableName().name);
		Html5DagGenericNode tableNode = dag.getByParameter(paramTable);
		if (tableNode != null) {
			for (DagEdge<Html5DagGenericNode> edge : tableNode.getOutputs(pnSetTable)) {
				Html5DagGenericNode columnNodeCur = edge.getOtherEnd(tableNode);
				String colNameCur = columnNodeCur.getParameterValue(pnPCName);
				if (colNameCur.toLowerCase().equals(columnName.getColumnNameShort().toLowerCase())) {
					uriToColumn = columnNodeCur.getUniqueId();
					break;
				}
			}
		}

		return uriToColumn;
	}

	public void addToDag(Html5DagGenericNode dag, String uriToColumn) {

		// TODO handle case where table does not exist in schema (interm,
		// temporary tables).

		// add node to dag
		dag.addEdge("basedatalink", uri, uriToColumn);

		// set name
		Html5DagGenericNode node = dag.getByUniqueId(uri);
		node.setCaption(value);
		node.setType(Metadata.TYPE_BD);
		node.setStyle(Metadata.STYLE_BD);

		// set filter conditions
		// - add value to entry point node
		node.getParameters().add(new Parameter(mapping.getDosPropNameValue(), value));
	}

}
