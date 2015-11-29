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
import org.objectweb.fractal.fraclet.annotations.Requires;
import org.objectweb.fractal.fraclet.extensions.Membrane;

import org.osadev.osa.logger.newdes.SimulationLogger;
import org.osadev.osa.simapis.wrappers.llong.EventModel;

/**
 * Hello part of the hello-world example.
 * 
 * <p> Implements the business part of the Hello component. The
 * control part is generated automatically using AOP techniques 
 * such as AspectJ or Spoon visitors.
 * 
 * 
 * <p>This example is an extended version of the Fractal hello-world
 * example, in which an hello client component is bound to a world 
 * server component from which it requests the execution of the printing
 * service (that outputs hello-world).
 * 
 * <p>In this OSA version of hello-world, we use the same client-server
 * architecture, but a few additional features are added:
 * <ul>
 * <li> A logging facility is used for outputs. This Logging facility is
 * adapted to the needs of simulation with the automated addition of the
 * current simulation time-stamp to each log.
 * <li> Fraclet annotations are used to alleviate the coding such that
 * the Fractal default methods are automatically generated
 * <li> The Fractal component membrane is set to one of the specific OSA
 * primitive simulation membranes, either <code>simBasicPrimitive</code>
 * to access the Event-Driven API, or <code>simPRimitive</code> to
 * access the Process-Oriented API.
 * <li> The connection between the content (business) part of the 
 * component and the controller implementation of the API is done 
 * using a controller annotation. For convenience, two abstract classes
 * are provided for extension, one for each OSA API. In this
 * particular example, we only use the Event-Driven API and therefore
 * we extend the {@link org.osadev.osa.simapis.basic.AbstractEventModel} 
 * class.
 * </ul> 
 * 
 * @author odalle
 *
 */
@Component
@Membrane(controller = "simBasicPrimitive")
public class Hello extends EventModel implements HelloItf{
	
	
	/**
	 * Constructor.
	 * 
	 * <p>The constructor is a good place to initialize the logging facility.
	 */
	public Hello() {
		super(new SimulationLogger<Long>(Hello.class));
	}

	@Requires(name = "world")
	private WorldItf addWorld;

	/*
	 * (non-Javadoc)
	 * @see org.osadev.osa.model.newdes.helloworld.HelloItf#generateHello(java.lang.String)
	 */
	public void generateHello(String param) {
		logger_.debug("Running generateHello()...");
		addWorld.printWorld("Hello");
	}

}
