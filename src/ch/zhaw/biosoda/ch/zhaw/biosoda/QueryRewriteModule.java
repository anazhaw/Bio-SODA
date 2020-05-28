package ch.zhaw.biosoda;

import java.util.HashMap;
import java.util.HashSet;

class QueryRewrite {
	HashSet<String> selectVars;
	String query;
	HashSet<String> filterConditions;
	
	public QueryRewrite(HashSet<String> vars, String queryStr, HashSet<String> filters) {
		this.selectVars = vars;
		this.query = queryStr;
		this.filterConditions = filters;
	}
}
public class QueryRewriteModule {
	
	HashMap<String, QueryRewrite> mappings = new HashMap<String, QueryRewrite>();
	
	public QueryRewriteModule () {
		HashSet<String> selectVars = new HashSet<String>();
		selectVars.add("?protein1");
		selectVars.add("?protein2");
		HashSet<String> filters = new HashSet<String>();
		filters.add("?node1 != ?node2");
		
		mappings.put("hasOrtholog", new QueryRewrite(selectVars,
				"?cluster a <http://purl.org/net/orth#OrthologsCluster>. \n" + 
				"   ?cluster <http://purl.org/net/orth#hasHomologousMember> ?node1. \n" + 
				" ?cluster <http://purl.org/net/orth#hasHomologousMember> ?node2. \n" + 
				"  ?node2 <http://purl.org/net/orth#hasHomologousMember>* ?protein2. \n" + 
				" ?node1 <http://purl.org/net/orth#hasHomologousMember>* ?protein1. \n" + 
				"  ?protein1 a <http://purl.org/net/orth#Protein>. \n" + 
				"   ?protein2 a <http://purl.org/net/orth#Protein>",
				filters));
		
		
		mappings.put("hasParalog", new QueryRewrite(selectVars," ?cluster a orth:ParalogsCluster. \n" + 
				"   ?cluster <http://purl.org/net/orth#hasHomologousMember> ?node1. \n" + 
				" ?cluster <http://purl.org/net/orth#hasHomologousMember> ?node2. \n" + 
				"  ?node2 <http://purl.org/net/orth#hasHomologousMember>* ?protein2. \n" + 
				" ?node1 <http://purl.org/net/orth#hasHomologousMember>* ?protein1. \n" + 
				"  ?protein1 a <http://purl.org/net/orth#Protein>. \n" + 
				"   ?protein2 a <http://purl.org/net/orth#Protein> ", filters));
	}
	
	public QueryRewrite getMappingForKeyword(String kw) {
		return mappings.get(kw);
	}

}
