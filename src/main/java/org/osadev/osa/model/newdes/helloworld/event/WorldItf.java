/** ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++--> 
<!--                Open Simulation Architecture (OSA)                  -->
<!--                                                                    -->
<!--      This software is distributed under the terms of the           -->
<!--           CECILL-C FREE SOFTWARE LICENSE AGREEMENT                 -->
<!--  (see http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.html) -->
<!--                                                                    -->
<!--  Copyright © 2006-2015 Université Nice Sophia Antipolis            -->
<!--  Contact author: Olivier Dalle (olivier.dalle@unice.fr)            -->
<!--                                                                    -->
<!--  Parts of this software development were supported and hosted by   -->
<!--  INRIA from 2006 to 2015, in the context of the common research    -->
<!--  teams of INRIA and I3S, UMR CNRS 7172 (MASCOTTE, COATI, OASIS and -->
<!--  SCALE).                                                           -->
<!--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++**/
package org.osadev.osa.model.newdes.helloworld.event;

import org.objectweb.fractal.fraclet.annotations.Interface;


@Interface(name = "world")
public interface WorldItf {
	
	/**
	 * World with print action is delayed by a fixed amount of time using
	 * event-driven scheduling.
	 * 
	 * <p> This method uses OSA event-driven API to get the current 
	 * simulation time and schdule the execution of another method
	 * at a later time.
	 * 
	 * <p>Notice how the actual message to be printed by the business
	 * code of this component is passed using a parameter array in
	 * the call to {@link fr.inria.osa.simapis.basic.EventModelingAPI#scheduleMyself(String, Object[], long)} API
	 * method.
	 * 
	 */
	void printWorld(String msg);
 
}
