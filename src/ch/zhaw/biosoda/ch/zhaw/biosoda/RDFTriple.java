package ch.zhaw.biosoda;

public class RDFTriple {
	String subj;
	String prop;
	String obj;
	
	public RDFTriple(String subj, String prop, String obj) {
		this.subj = subj;
		this.prop = prop;
		this.obj = obj;
	}
	
	public RDFTriple() {
		this.subj ="";
		this.prop = "";
		this.obj = "";
	}
	
	public void addSubj(String subj) {
		this.subj = subj;
	}
	
	public void addProp(String prop) {
		this.prop = prop;
	}
	
	public void addObj(String obj) {
		this.obj = obj;
	}
	
	public String getSubj() {
		return subj;
	}
	
	public String getObj() {
		return obj;
	}
	
	public String getProp() {
		return prop;
	}
	
	public String toString() {
		return subj + " "+ prop + " " + obj;
	}
}
