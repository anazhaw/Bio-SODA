package ch.ethz.semdwhsearch.prototyp1;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import ch.ethz.semdwhsearch.prototyp1.servlets.ResourcesServlet;
import ch.ethz.semdwhsearch.prototyp1.servlets.SemDwhSearchServlet;
import ch.ethz.semdwhsearch.prototyp1.servlets.SparqlServlet;

/**
 * Run SemDwhSearch as a standalone application using Jetty as its HTTP server.
 * 
 * @author Lukas Blunschi, Ana Sima
 */
public class RunStandaloneSemDwhSearch {

	/**
	 * Entry point of standalone version.
	 * 
	 * @param args
	 *            ignored
	 */
	public static void main(String[] args) {
		int port = 8081;
		if (args.length > 0) {
			for (int i = 0; i < args.length; i += 2) {

				// port
				if (args[i].equals("-p")) {
					try {
						port = Integer.parseInt(args[i + 1]);
					} catch (Exception e) {
						System.err.println("Unable to parse port from command line.");
					}
				}
			}
		}
		new RunStandaloneSemDwhSearch().run(port);
	}

	/**
	 * Run it.
	 */
	private void run(final int port) {

		// context name and port
		String contextName = "biosoda";
		System.out.println("Context name:      " + contextName);
		System.out.println("Port:              " + port);

		// start jetty
		try {
			Server server = new Server(port);
			Context context = new Context(server, "/" + contextName, Context.SESSIONS);
			
			
			// add servlet(s)
			context.addServlet(new ServletHolder(new SemDwhSearchServlet()), "/");
			context.addServlet(new ServletHolder(new ResourcesServlet("/resources")), "/css/*");
			context.addServlet(new ServletHolder(new ResourcesServlet("/resources")), "/images/*");
			context.addServlet(new ServletHolder(new ResourcesServlet("/resources")), "/js/*");
			context.addServlet(new ServletHolder(new ResourcesServlet("/resources")), "/js/libs/*");
			context.addServlet(new ServletHolder(new SparqlServlet()), "/sparql/*");		

			// start HTTP server
			server.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
