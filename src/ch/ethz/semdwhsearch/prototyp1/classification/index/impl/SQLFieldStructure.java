package ch.ethz.semdwhsearch.prototyp1.classification.index.impl;

public class SQLFieldStructure {
	String fieldName;
	String type;
	String constraints;
	
	public SQLFieldStructure(String name, String type, String constraints) {
		this.fieldName = name;
		this.type = type;
		this.constraints = constraints;
	}
	
	public String toString() {
		return fieldName + " "+ type + " " + constraints;
	}
}
