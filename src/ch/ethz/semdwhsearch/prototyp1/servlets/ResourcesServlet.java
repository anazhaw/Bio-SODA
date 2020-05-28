package ch.ethz.semdwhsearch.prototyp1.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.ethz.semdwhsearch.prototyp1.tools.ServletResponseTools;

/**
 * A servlet to retrieve static resources.
 * 
 * @author Lukas Blunschi
 */
public class ResourcesServlet extends HttpServlet {

	/**
	 * Required serial version UID.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Choose the directory containing the resources.
	 * Example: for directory 'resouces' choose '/resources'.
	 */
	private String directory;
	
	public ResourcesServlet(String directory){
		this.directory = directory;
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// print paths
		// request URI, e.g. /semdwhsearch/resources/css/semdwhsearch.css
		// context path, e.g. /semdwhsearch
		// servlet path, e.g. /resources
		String uri = req.getRequestURI();
		String ctxPath = req.getContextPath();
		// String servletPath = req.getServletPath();

		// get requested resource
		String docroot = req.getSession().getServletContext().getRealPath("/");
		if (docroot == null) {
			docroot = ".";
		}
		// if (docroot.endsWith("/")) docroot = docroot.substring(1);
		String path = docroot + this.directory + uri.substring(ctxPath.length());
		File resource = new File(path);

		// send result
		if (resource.exists()) {
			// 7200s = 2h
			ServletResponseTools.streamFile(resource, req, resp, 7200);
		} else {
			ServletResponseTools.sendNotFound(req, resp);
		}

	}

}
