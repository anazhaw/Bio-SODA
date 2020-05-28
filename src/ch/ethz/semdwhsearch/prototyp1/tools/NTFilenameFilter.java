package ch.ethz.semdwhsearch.prototyp1.tools;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Look for .nt files.
 * 
 * @author Lukas Blunschi
 * 
 */
public class NTFilenameFilter implements FilenameFilter {

	public boolean accept(File dir, String name) {
		return name.endsWith(".nt");
	}

}
