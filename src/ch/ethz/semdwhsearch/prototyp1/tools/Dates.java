package ch.ethz.semdwhsearch.prototyp1.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Dates {

	public static final DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

	public static final DateFormat dateFormatterSQL = new SimpleDateFormat("yyyy-MM-dd");

	public static final DateFormat timeFormatterSQL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

}
