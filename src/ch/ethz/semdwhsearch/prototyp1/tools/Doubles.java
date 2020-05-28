package ch.ethz.semdwhsearch.prototyp1.tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Tools to work with doubles.
 * 
 * @author Lukas Blunschi
 * 
 */
public class Doubles {

	/**
	 * 0.00
	 */
	public static final NumberFormat formatter = new DecimalFormat("0.00");

	/**
	 * 0.0
	 */
	public static final NumberFormat formatterShort = new DecimalFormat("0.0");

}
