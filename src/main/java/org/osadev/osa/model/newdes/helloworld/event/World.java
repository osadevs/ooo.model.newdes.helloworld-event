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


import org.objectweb.fractal.fraclet.annotations.Component;
import org.objectweb.fractal.fraclet.extensions.Membrane;

import org.osadev.osa.logger.newdes.SimulationLogger;
import org.osadev.osa.simapis.exceptions.IllegalEventMethodException;
import org.osadev.osa.simapis.exceptions.UnknownEventMethodException;
import org.osadev.osa.simapis.modeling.ModelingTimeAPI;
import org.osadev.osa.simapis.wrappers.llong.EventModel;

@Component
@Membrane(controller = "simBasicPrimitive")
public class World extends EventModel implements WorldItf {

	public World() {
		super(new SimulationLogger<Long>(World.class));
	}
	
	/**
	 * Does the server side actual work, that is print a message as instructed
	 * by the client.
	 * 
	 * <p> 
	 * This method is not declared by any interface. Since it is not made 
	 * available to other Fractal component it can only be called internally by this
	 * component or another object that has an explicit reference to this class.
	 * 
	 * @param message
	 */
	public void doPrint(final String message) {
		logger_.info("{} World", message);
		ModelingTimeAPI<Long> end = getSimulationTime();
		logger_.debug("World: finished work at t={}", end.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.osadev.osa.model.newdes.helloworld.event.WorldItf#printWorld(java.lang.String)
	 */
	public void printWorld(String msg) {
		logger_.debug("calling printworld({})",msg);
		ModelingTimeAPI<Long> current = getSimulationTime();
		Object[] params = {(Object)msg};
		scheduleEventMyselfNoE("doPrint", params, current.getDelayed(10L) );
	}

}
