package ch.ethz.semdwhsearch.prototyp1.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Measure time.
 * 
 * @author Lukas Blunschi
 * 
 */
public class StopWatch {

	private static final Logger logger = LoggerFactory.getLogger(StopWatch.class);

	private final long begin;

	private final String marker;

	public StopWatch(String marker) {
		this.marker = marker;
		this.begin = System.nanoTime();
	}

	/**
	 * Stop and report duration.
	 * 
	 * @param taskDescription
	 *            short task description, e.g. 'Loading RDF models'.
	 */
	public void stopAndReport(String taskDescription) {
		long duration = System.nanoTime() - begin;
		StringBuffer buf = new StringBuffer();
		while (buf.length() < 40) {
			buf.append(marker);
		}
		logger.info(buf.toString() + " " + taskDescription + " took " + Doubles.formatter.format(duration / 1E9) + "s.");
	}

}
