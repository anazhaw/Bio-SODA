package ch.ethz.semdwhsearch.prototyp1.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * A struct to hold a foreign key.
 * <p>
 * TODO do lexicographical sorting when constructing a FK.
 * 
 * @author Lukas Blunschi
 * 
 */
public class ForeignKey implements Comparable<ForeignKey> {

	public final ColumnName left;

	public final ColumnName right;

	public ForeignKey(ColumnName left, ColumnName right) {
		this.left = left;
		this.right = right;
	}

	public List<ColumnName> getColumnNames() {
		List<ColumnName> columnNames = new ArrayList<ColumnName>();
		columnNames.add(left);
		columnNames.add(right);
		return columnNames;
	}

	public List<TableName> getTableNames() {
		List<TableName> tableNames = new ArrayList<TableName>();
		tableNames.add(left.getTableName());
		tableNames.add(right.getTableName());
		return tableNames;
	}

	// --------------------------------------------------- comparable interface

	public int compareTo(ForeignKey o) {
		return this.toString().compareTo(o.toString());
	}

	// ------------------------------------------------------- object overrides

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ForeignKey) {
			ForeignKey fk = (ForeignKey) obj;
			boolean llrr = left.equals(fk.left) && right.equals(fk.right);
			boolean lrrl = left.equals(fk.right) && right.equals(fk.left);
			return llrr || lrrl;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return left.hashCode() + right.hashCode();
	}

	@Override
	public String toString() {
		// make sure to produce the string in lexicographical order
		String leftStr = left.toString();
		String rightStr = right.toString();
		if (leftStr.compareTo(rightStr) < 0) {
			return leftStr + "=" + rightStr;
		} else {
			return rightStr + "=" + leftStr;
		}
	}

}
