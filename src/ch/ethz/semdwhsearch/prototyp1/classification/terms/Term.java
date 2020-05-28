package ch.ethz.semdwhsearch.prototyp1.classification.terms;

import org.apache.jena.rdf.model.Model;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ch.ethz.semdwhsearch.prototyp1.constants.Constants;
import ch.ethz.semdwhsearch.prototyp1.metadata.MetadataSingleton;
import ch.ethz.semdwhsearch.prototyp1.metadata.mapping.MetadataMapping;
import ch.zhaw.biosoda.SPARQLUtilsLocal;
import ch.zhaw.biosoda.SPARQLUtilsRemote;

/**
 * A term.
 * <p>
 * Every term has a type.
 * <p>
 * Examples: 'High Tech' would be one term of type BUSINESS_OBJECT.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Term {

	public final TermType type;

	public String key;

	public String value;
	
	public double pageRank;
	
	public final HashMap<String, List<String>> propValues = new HashMap<String, List<String>>();

	public final TermOrigin origin;

	public String originName;
	
	public MatchType matchType = MatchType.EXACT;
	
	public String filteredClass = null;
	
	public String filteredProp = null;

	public boolean isNegated = false;

	public String label = "";

	public Term(TermType type, String value, TermOrigin origin, String originName) {
		this(type, value, value, origin, originName, Constants.DEFAULT_PAGERANK);
	}

	public Term(TermType type, String key, String value, TermOrigin origin, String originName, String className, String propName, Double pageRank){
		this.type = type;
		this.key = key;
		if (key == null && (type == TermType.RDF_MATCH || type == TermType.VALUE)) {
			throw new RuntimeException("Terms of type BO and VAL need to have a key.");
		}
		
		this.value = value == null ? "" : value;
		
		this.originName = originName == null ? "" : originName;		
		
		this.origin = origin;
		
		this.filteredClass = className;
		
		this.filteredProp = propName;

		this.pageRank = pageRank;
	}

	public Term(TermType type, String key, String value, TermOrigin origin, String originName){
		this.type = type;
		this.key = key;
		if (key == null && (type == TermType.RDF_MATCH || type == TermType.VALUE)) {
			throw new RuntimeException("Terms of type BO and VAL need to have a key.");
		}
		
		this.value = value == null ? "" : value;
		
		this.originName = originName == null ? "" : originName;		
		
		this.origin = origin;
		
	}

	public void setClassProp(String className, String propName) {
		this.filteredClass = className;
		this.filteredProp = propName;
	}
	
	public void setFilter() {
		this.matchType = MatchType.FILTER;
	}

	public void setExactMatch() {
		this.matchType = MatchType.EXACT;
	}
		
			
	public Term(TermType type, String key, String value, TermOrigin origin, String originName, double pageRank) {
		this(type, key, value, origin, originName);
		this.pageRank = pageRank;
	}
	
	public String toClickableHtml() {
		return "<a href=" + value + ">"+ value + "</a>";
	}
	
	public String toHtml() {
		return "<span class='" + type + "'>" + value + "</span>";
	}

	// ------------------------------------------------------- object overrides

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Term) {
			Term term = (Term) obj;
			boolean sameType = term.type == type;
			boolean sameKey = term.key.equals(key);
			boolean sameValue = term.value.equals(value);
			if (sameType && sameKey && sameValue && term.origin == origin && term.originName.equals(originName)) {
				return true;
			} else if(term.matchType == MatchType.FILTER && this.matchType == MatchType.FILTER && (!term.filteredClass.equals("null")) && (!term.filteredProp.equals("null")) && term.filteredClass.equals(this.filteredClass) && 
						term.filteredProp.equals(this.filteredProp) && (!this.filteredClass.contains("http://www.w3.org/2002/07/owl")) &&(!this.filteredClass.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns")))  {
					return true;
			} else {
				return false;
			} 
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		// TODO is this good enough?
		if(filteredClass != null && (!filteredClass.equals("null")) && (!filteredClass.contains("http://www.w3.org/2002/07/owl")) &&(!this.filteredClass.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns")))
			return matchType.toInt() + filteredClass.hashCode() + filteredProp.hashCode();
		else
			if (filteredClass == null)
				return value.hashCode();
			else
				return filteredClass.hashCode() + filteredProp.hashCode() + key.hashCode() +  value.hashCode();
	}

	@Override
	public String toString() {
		return "Key: " + key + " value: "+ value +  " originName: " + originName + " page rank " + pageRank + " filtered class: "+ filteredClass + " prop: "+ filteredProp + " is negated: "+ isNegated + " is filter " + matchType.equals(MatchType.FILTER);
	}
	
	public static String getPropertyThatMatchesRemote(String endpoint, String uri, String keyword) {
		String queryStringLabel = 
				"SELECT ?prop WHERE { <" + uri + "> ?prop ?val . FILTER (contains("+ " lcase(str(?val)), " + "\"" +  keyword.toLowerCase() + "\"" + " )) " //", lcase(str(?val)) ))  "
				+ "}";

		MetadataMapping mapping = MetadataSingleton.getInstance().getMetadata().getMapping();
		List<String> rdfLabels = null;
		try {
			rdfLabels = SPARQLUtilsRemote.execRemoteQuery(queryStringLabel, endpoint);
		} catch (Exception e){
			return "";
		}
		HashSet<String> matchingLabels = new HashSet<String>(rdfLabels);

		int index = 0;
		if(matchingLabels.size() > 0) {
			if(matchingLabels.contains("<http://www.w3.org/2000/01/rdf-schema#label>"))
				return "<http://www.w3.org/2000/01/rdf-schema#label>";
			for(String prop : matchingLabels) {
				if(mapping.getGeneralPropNamesCaptionSet().contains(SPARQLUtilsRemote.getLiteralFromString(prop)))
					return prop;
			}
		}
		else {
			return "";
		}
		return rdfLabels.get(index);
	}
	
	public static String getPropertyThatMatches(Model m, String uri, String keyword) {

		String queryStringLabel = 
				"SELECT ?prop WHERE { <" + uri + "> ?prop ?val . FILTER (contains(lcase(str(?val)), '" + keyword.toLowerCase() +"' ))  "
				+ "}";

		List<String> rdfLabels = SPARQLUtilsLocal.execQuery(queryStringLabel, m);

		if (rdfLabels.size() == 0) {
			queryStringLabel = 
					"SELECT ?prop WHERE { <" + uri + "> ?prop ?val . FILTER (contains(lcase(str(?val)), '" + keyword.replace(Constants.PUNCTUATION_FOR_SPLITS, " ").toLowerCase() +"' ))  "
					+ "}";

		    rdfLabels = SPARQLUtilsLocal.execQuery(queryStringLabel, m);
		}
		
		if(rdfLabels.size() > 0 && (!rdfLabels.get(0).equals("NULL")))
			return rdfLabels.get(0);
		//TODO: REDESIGN
		return "";
	}

	public void setNegated() {
		this.isNegated  = true;
		
	}

	public boolean isNegated() {
		return this.isNegated;
	}
}
