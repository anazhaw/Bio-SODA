package ch.ethz.semdwhsearch.prototyp1.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Utilities to pipe on stream into another.
 * 
 * @author Lukas Blunschi
 */
public class IOUtils {

	/**
	 * Read given input stream into a string.
	 * 
	 * @param in
	 * @param charsetName
	 * @return content of input stream as a string or null if an error occurred.
	 */
	public static String readToString(InputStream in, String charsetName) {
		try {
			BufferedReader inBuf = new BufferedReader(new InputStreamReader(in, charsetName));
			StringBuffer buf = new StringBuffer();
			String line = null;
			boolean first = true;
			while ((line = inBuf.readLine()) != null) {
				if (first) {
					first = false;
				} else {
					buf.append("\n");
				}
				buf.append(line);
			}
			return buf.toString();
		} catch (IOException ioe) {
			return null;
		}
	}

	/**
	 * Pipe one stream into another. no close operations are applied to the
	 * streams.
	 * 
	 * @param in
	 *            read from this.
	 * @param out
	 *            write to this.
	 * @return number of bytes piped or -1 if an exception occured.
	 */
	public static long pipe(InputStream in, OutputStream out) {
		try {
			long numBytes = 0;
			byte[] buffer = new byte[4096];
			int num = -1;
			while ((num = in.read(buffer)) > 0) {
				numBytes += num;
				out.write(buffer, 0, num);
			}
			return numBytes;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Copy a file.
	 * 
	 * @param src
	 * @param dst
	 * @param force
	 *            only copy, if destination does not exist yet.
	 * @return length of new file or -1 if error occured.
	 */
	public static long copy(File src, File dst, boolean force) {
		try {
			boolean mayCopy = false;
			if (force) {
				if (dst.exists()) {
					if (dst.delete()) {
						if (dst.createNewFile()) {
							mayCopy = true;
						}
					}
				} else {
					if (dst.createNewFile()) {
						mayCopy = true;
					}
				}
			} else {
				if (!dst.exists()) {
					if (dst.createNewFile()) {
						mayCopy = true;
					}
				}
			}
			if (mayCopy) {
				FileInputStream in = new FileInputStream(src);
				FileOutputStream out = new FileOutputStream(dst);
				IOUtils.pipe(in, out);
				in.close();
				out.close();
				return dst.length();
			} else {
				return -1L;
			}
		} catch (Exception e) {
			return -1L;
		}
	}

	/**
	 * Copy a file. No existence tests are applied.
	 * 
	 * @param src
	 * @param dst
	 * @return length of new file or -1 if error occured.
	 */
	public static long copy(File src, File dst) {
		try {
			FileInputStream in = new FileInputStream(src);
			FileOutputStream out = new FileOutputStream(dst);
			IOUtils.pipe(in, out);
			in.close();
			out.close();
			return dst.length();
		} catch (Exception e) {
			return -1L;
		}
	}

	/**
	 * Delete given file if it is empty.
	 * 
	 * @param file
	 * @return false on error, true otherwise.
	 */
	public static boolean removeIfEmpty(File file) {
		try {
			if (file.exists() && file.isFile()) {
				if (file.length() == 0L) {
					return file.delete();
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
