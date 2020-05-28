package ch.ethz.semdwhsearch.prototyp1.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tools to work with servlet responses.
 * <p>
 * Servlet response tools help you at the very end of a servlet response. They
 * provide functions to stream a file or a stringbuffer as well as a function to
 * send special responses back to the client.
 * 
 * @author Lukas Blunschi
 */
public class ServletResponseTools {

	private static final Logger logger = LoggerFactory.getLogger(ServletResponseTools.class);

	private static SimpleDateFormat cacheFormatter;

	static {
		/* e.g. tue, 23 dec 2014 21:04:22 GMT */
		cacheFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		cacheFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Stream given file to client.
	 * 
	 * @param file
	 *            file to stream.
	 * @param req
	 *            current HTTP request.
	 * @param resp
	 *            current HTTP response.
	 * @param expireTime
	 *            duration in seconds until resource expires.
	 * @return number of bytes streamed or -1 if exception while streaming.
	 * @throws IOException
	 */
	public static long streamFile(File file, HttpServletRequest req, HttpServletResponse resp, int expireTime)
			throws IOException {
		// find content type from filename
		String filename = file.getName();
		String contentType = req.getSession().getServletContext().getMimeType(filename);

		// logging
		logger.info(req.getMethod() + " 200 " + file.length() + " " + ServletTools.decodeURI(req.getRequestURI()));
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(contentType);
		if (expireTime > 0) {
			Date expiringTime = new Date(System.currentTimeMillis() + expireTime * 1000);
			resp.addHeader("cache-control", "public");
			resp.addHeader("expires", cacheFormatter.format(expiringTime));
		}
		InputStream in = new FileInputStream(file);
		OutputStream out = resp.getOutputStream();
		long numBytes = IOUtils.pipe(in, out);
		in.close();
		out.close();
		return numBytes;
	}

	/**
	 * Stream given string to client.
	 * 
	 * @param html
	 *            string to stream.
	 * @param contenttype
	 *            content type, e.g. text/html or text/css.
	 * @param encoding
	 *            caracter encoding e.g. 'utf-8'.
	 * @param calcTime
	 *            time to generate response [in ms].
	 * @param req
	 *            current HTTP request.
	 * @param resp
	 *            current HTTP response.
	 * @throws IOException
	 *             if string could not be written to the response.
	 */
	public static void streamStringBuffer(StringBuffer html, String contenttype, String encoding, double calcTime,
			HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		
		String decodedURI = ServletTools.decodeURI(req.getRequestURI());
		
		logger.info(req.getMethod() + " 200 " + html.length() + " " + calcTime + "ms " + decodedURI);
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(contenttype);
		resp.setCharacterEncoding(encoding);
		PrintWriter pout = resp.getWriter();
		pout.write(html.toString());
		pout.close();
	}

	/**
	 * Send reload response (303) to client.
	 * 
	 * @param req
	 *            current HTTP request.
	 * @param resp
	 *            current HTTP response.
	 * @param location
	 *            location header string.
	 */
	public static void sendReload(HttpServletRequest req, HttpServletResponse resp, String location) {
		String decodedURI = ServletTools.decodeURI(req.getRequestURI());
		logger.info(req.getMethod() + " 303 " + decodedURI);
		resp.setStatus(HttpServletResponse.SC_SEE_OTHER);

		// encode location in ASCII
		String encodedLocation = null;
		try {
			encodedLocation = new URI(location).toASCIIString();
		} catch (Exception e) {
			encodedLocation = location;
		}
		resp.addHeader("location", encodedLocation);
	}

	/**
	 * Send not found response (404) to client.
	 * 
	 * @param req
	 *            current HTTP request.
	 * @param resp
	 *            current HTTP response.
	 */
	public static void sendNotFound(HttpServletRequest req, HttpServletResponse resp) {
		String decodedURI = ServletTools.decodeURI(req.getRequestURI());
		logger.info(req.getMethod() + " 404 " + decodedURI);
		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		String message = "The requested document '" + decodedURI + "' was not found.";
		saveWrite(resp, message);
	}

	/**
	 * Send internal server error (500) to client.
	 * 
	 * @param req
	 * @param resp
	 * @param message
	 */
	public static void sendInternalServerError(HttpServletRequest req, HttpServletResponse resp, String message) {
		String decodedURI = ServletTools.decodeURI(req.getRequestURI());
		logger.info(req.getMethod() + " 500 " + decodedURI);
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		saveWrite(resp, message);
	}

	/**
	 * Write to response without throwing an exception.
	 * 
	 * @param resp
	 *            current HTTP response.
	 * @param message
	 *            message to write.
	 */
	private static void saveWrite(HttpServletResponse resp, String message) {
		resp.setContentType("text/plain");
		resp.setCharacterEncoding("utf-8");
		PrintWriter pout = null;
		try {
			pout = resp.getWriter();
			pout.write(message);
		} catch (IOException ioe) {
			logger.error("Failure writing response: " + ioe.getMessage());
		} finally {
			if (pout != null) {
				pout.close();
			}
		}
	}

}
