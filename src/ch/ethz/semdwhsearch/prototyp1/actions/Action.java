package ch.ethz.semdwhsearch.prototyp1.actions;

import javax.servlet.http.HttpServletRequest;

import ch.ethz.semdwhsearch.prototyp1.actions.results.Result;

/**
 * Every action has to implement this interface.
 * 
 * @author Lukas Blunschi
 * 
 */
public interface Action {

	/**
	 * Execute this action.
	 * 
	 * @param req
	 * @return
	 */
	Result execute(HttpServletRequest req);

}
