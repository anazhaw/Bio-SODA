package ch.ethz.semdwhsearch.prototyp1.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of query results (tables) which belong together.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Tables implements Iterable<SqlResult> {

	private final List<SqlResult> tables;

	public Tables() {
		this.tables = new ArrayList<SqlResult>();
	}

	public void addTable(SqlResult table) {
		tables.add(table);
	}

	public List<SqlResult> getTables() {
		return tables;
	}

	// ------------------------------------------------------------- statistics

	public double getCombinedPrecision() {
		double prec = 1.0;
		for (SqlResult table : tables) {
			prec *= table.getPrecision();
		}
		return prec;
	}

	public double getCombinedRecall() {
		double recall = 1.0;
		for (SqlResult table : tables) {
			recall *= table.getRecall();
		}
		return recall;
	}

	/**
	 * @see http://en.wikipedia.org/wiki/Precision_and_recall
	 * @return F-measure
	 */
	public double getFMeasure() {
		double precision = getCombinedPrecision();
		double recall = getCombinedRecall();
		if (precision == 0.0 && recall == 0.0) {
			return -1.0;
		} else {
			return 2 * ((precision * recall) / (precision + recall));
		}
	}

	// ----------------------------------------------------- Iterable interface

	public Iterator<SqlResult> iterator() {
		return tables.iterator();
	}

}
