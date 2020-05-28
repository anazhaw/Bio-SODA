package ch.ethz.semdwhsearch.prototyp1.classification.index.impl;

import java.util.ArrayList;

public class SQLTableStructure {
	String tableName;
	ArrayList<SQLFieldStructure> fields = new ArrayList<SQLFieldStructure>();
	String pK;
	String otherConstraints;
	
	public SQLTableStructure(String tbl, ArrayList<SQLFieldStructure> fields, String pk, String others) {
		this.tableName = tbl;
		this.fields = fields;
		this.pK = pk;
		this.otherConstraints = others;
	}
	
	public String toString() {
		StringBuffer sql = new StringBuffer();
		sql.append("create table " + tableName + " (");
		for(SQLFieldStructure field: this.fields) {
			sql.append(field.toString() + ", ");
		}
		sql.append("  PRIMARY KEY ( " + pK + ") ");
		if(!this.otherConstraints.isEmpty())
			sql.append(", "+ this.otherConstraints);
		sql.append(")");
		return sql.toString();
	}
	
	public String generateInsertStatement() {
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO " + this.tableName);
		
		sql.append("  ( " );
		for(SQLFieldStructure field: fields) {
			sql.append(field.fieldName + ", ");
		}
		
		//remove the trailing "," 
		sql.deleteCharAt(sql.length() - 2);
		
		sql.append(" )") ;
		sql.append("    VALUES");
		return sql.toString();
	}
}
