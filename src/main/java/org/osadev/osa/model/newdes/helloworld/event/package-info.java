/**
 * A slightly revisited (enriched) version of the classic <a href="http://fractal.ow2.org/">Fractal</a>
 * hello world example.
 * 
 * <p>In this package version we illustrate the simulation features provided
 * by the OSA extension to Fractal. 
 * 
 * <p> OSA (Open Simulation Architecture) is
 * a simulation framework that extends Fractal components with simulation
 * features. The simulation features are provided by a simulation API that is
 * implemented directly within the <i>membrane</i> of each simulation component
 * (in Fractal's jargon a membrane is a collection of controllers that are
 * added to each component and implement the non-functionnal part of the component).
 * 
 * <p>The simulation features are based on a virtual clock, that allows for the 
 * scheduling of <i>events</i> at a given time in the simulation history. The code that
 * uses the simulation features is called a simulation model, because it is assumed
 * to reflect the timing of events observed in a real system. However programming 
 * with virtual timing can prove to be useful for other purposes than just simulation 
 * even though that was the authors' first intent.
 * 
 * <p> Events in OSA can be of two kinds: clock driven or synchronization driven.
 * Clock-driven events correspond to the case in which an action was scheduled 
 * or delayed until a given virtual time has been reached. Synchronization driven
 * events correspond to the case in which an active entity of the model (typically 
 * a thread) waits for another one, using synchronization primitives. Notice in
 * particular that the existing synchronization mechanisms such as the Java class
 * monitor or {@link java.util.concurrent.Semaphore} CANNOT be used within OSA
 * models, because they prevent the OSA simulation engine to work properly. (Future 
 * versions of OSA will try to remove this limitation).
 * 
 * <p> In order to control the scheduling of events, OSA actually provides two 
 * incremental APIs:
 * <ul>
 * <li> The {@link org.osadev.osa.simapis.basic.EventDrivenAPI} is an <i>asynchronous</i>
 * API that allows for the fast scheduling of call-back methods at given simulation times.
 * A first limitation of this API is that all scheduled actions must start and complete
 * at the same virtual (simulation) time. Another limitation of this API (consistent with
 * the previous one) is that it does not support synchronization driven events.
 * <li> The {@link org.osadev.osa.simapis.basic.SimulationProcessAPI} <code>extends</code> the 
 * previous asynchronous API with the notion of a process. A process is very similar
 * to a thread (and is actually most likely implemented by one): it can execute
 * sequences of actions and get results from these actions even if those actions
 * last in (virtual) time. Therefore, when a process starts an action there is a
 * chance that this action may complete at a later time. This process API allows both
 * event and synchronization driven events.
 * </ul>
 * 
 * <h3> The Fractal Hello World example briefly explained </h3>
 * 
 * <p>Before we enter the detailed description of this OSA hello-world example, let's first
 * recall the Fractal example as shown in the following detailed sequence diagram: a Fractal 
 * component, named Hello invokes the Service of another component to which it is 
 * connected (bound in Fractal jargon) using the Fractal interface binding machinery.
 * Following the Separation of Concerns principles, Hello does not know which component is
 * actually going to provide the service, its only relation is with the client side 
 * of the service interface. This client side is connected to the server side of the same
 * interface exposed by another component, using Fractal's  binding machinery. In our 
 * example, the server side of the Service is provided by the World component, but the
 * binding could follow an arbitrary long sequence of intermediate components. The final
 * source and destination components are build using the standard "Primitive" membrane,
 * which is an assembly of basic fractal controllers (Life-Cycle Controller, 
 * Naming Controller, Binding Controller, ...) that wont be further described here.
 * (See Fractal's Documentation).
 * 
 * <p>
 * <center><img src="doc-files/fractal-hello-world.png"></center>
 * 
 * <p>The previous example is a simplified view of the actual application architecture that
 * is missing a root composite level. The following diagram shows the actual 
 * architecture required to run the hello-world example in Fractal. Indeed, in the 
 * previous sequence diagram, both Hello and World appear as "Primitive" component, 
 * that is components with a "Primitive"
 * membrane. However, Fractal requires such components to be part of a higher
 * level "Composite" component before they can be <i>bound</i> to each other. Another
 * missing part of our application architecture is a way to start the execution of
 * the Hello component. For this purpose we need an additional Main interface.
 * 
 * <p> 
 * <center><img src="doc-files/fractal-hello-world-hierarchy.png"></center>
 * 
 * <p>
 * The bootstrap/initialization sequence is also missing in the first diagram.
 * Depending on the Fractal implementation and supporting tools, various options
 * exist, but they mostly fall into two categories: either API based, or ADL based.
 * ADL based solutions, such as those based on <a href="http://fractal.ow2.org/fractaladl/index.html">FractalADL</a>  
 * roughly follow the same path as the API based solutions, except all the steps 
 * are automated based on an architecture specification.  So we'll just discuss
 * the API based operation, so we can then better understand how it is extended
 * by OSA. The following sequence diagram gives a simplified view of the sequence
 * of operations, in which the real Fractal API methods calls have been replaced with
 * simpler ones to save space. For example, the creation of the type of the Hello
 * component, is noted 
 * <code><pre>
 * create_type({Main,Service})</code></pre>
 * instead of
 * <code><pre>
 * ComponentType cType = tf.createFcType(new InterfaceType[] {
 *     tf.createFcItfType("m", "Main", false, false, false),
 *     tf.createFcItfType("s", "Service", true, false, false)
 *   });
 *   </code></pre>
 *
 * <p> 
 * <center><img src="doc-files/fractal-hello-world-boot.png"></center>
 * 
 * <h3> Adding the OSA Scheduler and Events to the picture </h3>
 * 
 * 
 * <ul>
 * <li> <b>Each Fractal component whose content part extends one of OSA's Abstract Model</b>
 * (either {@link org.osadev.osa.simapis.basic.AbstractEventModel} or
 * {@link org.osadev.osa.simapis.basic.AbstractProcessModel}), becomes a <i>simulation model</i>.
 * As such it is created either with a "simBasicPrimitive" Fractal membrane, or a
 * "simPrimitive" membrane, respectively for the event-driven or process-oriented models. 
 * This membrane adds a new Simulation Controller that
 * exposes three interfaces: one is mainly intended for the content part of the model 
 * to which it provides the Modeling API, another one provides a low level scheduling API,
 * used for example by the ADL to schedule exogeneous events, and the last one is used 
 * for the control of the simulation by the OSA coordination component called the 
 * super-scheduler, introduced hereafter .
 * <p> 
 * <center><img src="doc-files/simulation-hello-inner.png"></center>
 * <li> <b>A global coordinator component called the "SuperScheduler"</b> is in charge
 * of the synchronization between each simulation model component (see diagram below). 
 * This way the synchronization
 * can be done following a two level scheduling scheme: Within each simulation model
 * the simulation controller implements a local scheduler, in charge of queuing and
 * sorting the pending events for the local component using a priority queue. The scheduling
 * time of the pending event at the top of the queue is systematically sent to the 
 * super scheduler, such that the super scheduler can always decide which component
 * to activate next to ensure the execution in chronological order.
 * This two level scheduling architecture requires the simulation model components to
 * send updates to the super-scheduler following a client-server model in which the 
 * server is the super-scheduler. However, in some situations, the client-server roles
 * need to be reversed, in particular when the model is using the most advanced 
 * Process-oriented API. In that case, many threads can be blocked within a given
 * component waiting for a delay to expire or for a synchronization to happen, but 
 * since we have a two level scheduling, the top level scheduler is not always 
 * able to decide which thread needs to resume execution, hence the need for
 * a call-back interface.
 * 
 * <p>
 * <center><img src="doc-files/simulation-hello-world.png"></center>
 * 
 * <li> <b>An event scheduling interface</b> is added to each Model component. In OSA an
 * event is an internal functor object of a given Fractal component: this object can only
 * be created and invoked within the component to which it belongs. OSA events contain 
 * the name of a method, a reference to an internal object of the component on which 
 * to call the method, a list of parameters and a time of execution (see 
 * {@link org.osadev.osa.simapis.basic.EventDrivenAPI}). Since events can only be created
 * internally by components for themselves, a interface is provided to request a component
 * to do so. This interface is implemented by the simulation controller and made available
 * both for other components to allow exogeneous event scheduling, and to the component
 * itself for the scheduling of endogeneous events. When a new event is scheduled, it is
 * added to the local priority queue of the component and if the event date is ealier
 * than the earliest scheduled event so far, this tame is advertised to the super-scheduler as
 * the new activation time for the current component.
 * 
 * <li> <b>A process-oriented scheduling interface</b> <i>MAY</i> be added to a 
 * Model component. Contrary to event-driven operations that are completely processed
 * at a single discrete point in time and require only one thread for execution, 
 * Process-oriented operations may have an arbitrary duration in simulation time and may
 * involve multiple threads. This duration may be caused by two mechanisms:
 * <ul>
 * <li> an explicit time consumption (ie. waiting for a given amount of time)
 * <li> a synchronization between two threads
 * </ul>
 * When an explicit time consumption is requested by the simulation model, the thread
 * is simply put to sleep and an e special event is scheduled at the requested time 
 * that will execute an internal wake-up procedure. This internal procedure is 
 * non-blocking and can therefore be executed directly by the scheduling thread. 
 * The synchronization mechanism is inspired from the Communicating Sequential Process 
 * channels: a channel is identified by an arbitrary name (called a condition) and
 * provides a one way communication channel between a waker thread and one or all
 * the sleeping threads on this condition. The communication is an arbitrary string 
 * message. 
 * Last but not least, the two previous mechanism can be combined into a bounded-wait
 * synchronization, that is a synchronization with a timeout delay. When the timeout
 * goes off, the thread(s) that reach the time limit receive a predefined message.
 * Notice that the synchronization is one-way: contrary to a rendez-vous, if no 
 * thread is sleeping, a waker thread is not put to sleep until a sleeper arrives.
 * However, the waker thread is blocked until the wake-up procedure completes,
 * and gets the number of threads that have been woken up as a result.
 * 
 * <h3>The Event-driven Hello-World explained</h3>
 * 
 * FIXME: Update this doc...
 * 
 * In this basic example, the Hello component does explicitly use the OSA 
 * simulation API, but it still extends the {@link org.osadev.osa.simapis.wrappers.llong.EventModel} 
 * class in order for the OSA logging facility to work properly (the logging
 * mechanism needs the current simulation time). Contrary to the Fractal version, 
 * the OSA Hello does not require an additional interface to start the execution
 * (such as Main or Runnable). Indeed, in OSA, the execution can be started 
 * directly from the program main method using the OSA bootstrap scheduling service.
 * 
 * <p>During the execution of the simulation, new events can be scheduled using 
 * the {@link org.osadev.osa.simapis.basic.EventModelingAPI#scheduleEventMyself(String, Object[], ModelingTimeAPI)}
 * method. Assuming the implementation details of a given component are 
 * not supposed to be known outside of this component, this method is primarily 
 * intended for internal use within a given model, hence the "Myself" suffix.
 * However, no technical restriction would prohibit the use of this interface 
 * externally, if the name of the internal method to be scheduled is known from
 * outside a component.
 * 
 * </ul>
 * @author odalle
 * 
 */
/*
 * @startuml doc-files/simulation-hello-world.png
 * scale 0.7
 * 
 * skinparam titleFontSize 14
 * title OSA Hello World Architecture Details
 * 
 * Component [main]
 * 
 * 
 * node Root {
 * Interface Main as mainin
 * [main] -r-> mainin
 * 
 * folder "OSA Model Level scheduling architecture" {
 * 
 * Component [Local Events#1]
 * 
 * Component [Local Events#2]
 * }
 * 
 * folder "OSA root level scheduling architecture" {
 * Interface "SimulationController#1" as SS1
 * Interface SuperSchedulerItf as SuperSchedItf
 * Interface "SimulationController#2" as SS2
 * 
 * [SuperScheduler] -d-> SS1
 * [SuperScheduler] -d-> SS2
 * [SuperScheduler] -d- SuperSchedItf
 * [SuperScheduler] .> [Time-ordered\nModel Queue] : uses
 * [Condition Wait Queues] <. [SuperScheduler] : uses
 * 
 * 
 * }
 * 
 * folder "OSA sample model" {
 * 
 * Interface Service
 * Component Hello
 * Component World
 * Interface "ModelingAPI#1" as Sim1
 * Interface "ModelingAPI#2" as Sim2
 * 
 * 
 * mainin - [Hello]
 * [Hello] -u-> SuperSchedItf  
 * [World] -u-> SuperSchedItf
 * 
 * [World] -u- SS2
 * [Hello] -u- SS1
 * [Local Events#1] <.. [Hello] : uses
 * 
 *
 * [Hello] -r-> Service
 * Service -r- [World]
 * 
 * [Local Events#2] <.. [World] : uses
 * 
 * 
 * [Hello] -d- Sim1
 * [Hello] --> Sim1
 * [World] -d- Sim2
 * [World] --> Sim2
 * 
 * }
 * 
 * 
 * }
 * 
 * folder "Fractal Architecture" {
 * component [FractalADL or\nFractal API] as Fractal
 * Interface BuildApplicationArchitecture
 * Fractal -r- BuildApplicationArchitecture
 * Fractal -u-> Sim1 : "schedules\nexogeneous\nevents"
 * Fractal -u-> Sim2 
 * }
 * 
 * 
 * [main] -r-> BuildApplicationArchitecture
 * @enduml
 */

/*
 * @startuml doc-files/simulation-hello-inner.png
 * scale 0.7
 * 
 * skinparam titleFontSize 14
 * 
 * Interface MoreControllers...
 * Interface BindingController
 * Interface LifeCycleController
 * Interface SimulationController
 * Interface Main
 * Interface Service
 * Interface EventModelingAPI
 * Interface SimulationInternalSchedulingAPI
 * 
 * title Anatomy of an OSA Simulation Model Component (with a simPrimitive membrane)
 * 
 * node "OSA Fractal Component" {
 *
 * 
 * package "SimPrimitive Membrane" {
 * [More Impl...] -u- MoreControllers...
 * [BC Impl] -u- BindingController
 * [LC Impl] -u- LifeCycleController
 * [SimC Impl] -up- SimulationController
 * [SimC Impl] -d- EventModelingAPI
 * [SimC Impl] -l- SimulationInternalSchedulingAPI
 * }
 * 
 * package "SimulationModel" {
 * [Hello Impl] -d- Main
 * [Hello Impl] -d-> Service
 * [Hello Impl] -d-> EventModelingAPI
 * 
 * }
 * }
 * 
 * 
 * 
 * @enduml
 */

/*
 * @startuml doc-files/fractal-hello-world.png
 * scale 0.7
 * skinparam titleFontSize 14
 * 
 * title 
 * Fractal (not OSA) basic hello world example 
 * (missing the bootstrapping/initialization part)
 * end title
 * 
 * box "Hello //Primitive// Component"
 * participant Hello
 * participant "Client side" as client << (I,#ADD1B2) Service >> 
 * end box
 * participant "(...)"
 * box "World //Primitive// Component"
 * participant "Server side" as server << (I,#ADD1B2) Service >> 
 * participant World
 * end box
 * hide footbox
 * 
 * note over client, server
 * Fractal binding
 * end note
 * Hello -> client: print("hello world")
 * activate Hello
 * client  -> server 
 * server -> World : print("hello world")
 * activate World
 * World --> server : //return//
 * deactivate World
 * server --> client 
 * client --> Hello
 * deactivate Hello
 * @enduml
 */
/* 
 * @startuml doc-files/fractal-hello-world-hierarchy.png
 * scale 0.7
 * skinparam titleFontSize 14
 * 
 * [main] -right-> Main
 * node Root {
 * 	Main - [Hello]
 * [Hello] -right-> Service
 * Service - [World]
 * }
 * 
 * @enduml
 * 
 */
/*
 * @startuml doc-files/fractal-hello-world-boot.png
 * scale 0.7
 * skinparam titleFontSize 14
 * 
 * participant Main
 * participant Fractal
 * participant Root
 * participant "Root->Hello" as Start << (I,#ADD1B2) Main >> 
 * participant Hello
 * participant "Hello->World" as Service << (I,#ADD1B2) Service >> 
 * participant World
 * 
 * title Fractal Bootstrap/initialization sequence
 * 
 * == start of bootstrap/initialization ==
 * Main -> Fractal : create_type({Main})
 * Fractal --> Main : //RootType// reference
 * Main -> Fractal : create_type({Main,Service})
 * Fractal --> Main : //HelloType// reference
 * Main -> Fractal : create_type({Service})
 * Fractal --> Main : //WorldType// reference
 * Main -> Fractal : create_instance(RootType, Composite)
 * Create Root
 * Fractal -> Root : << create(Composite) >>
 * Fractal -> Main : Root reference
 * Main -> Fractal : create_instance(HelloType, Hello, Primitive)
 * Create Hello
 * Fractal -> Hello : << create(Primitive) >>
 * Fractal --> Main : Hello reference
 * Main -> Fractal : create_instance(WorldType, World, Primitive)
 * Create World
 * Fractal -> World : << create(Primitive) >>
 * Fractal --> Main : World reference
 * Main -> Fractal : add(Root, Hello)
 * Fractal -> Root : << contains Hello >>
 * Main -> Fractal : add(Root, World)
 * Fractal -> Root : << contains World >>
 * Main -> Fractal : bind(Hello.Main, Root.Main)
 * Create Start
 * Fractal -> Start : << bind: Root->Hello >>
 * Main -> Fractal : bind(World.Service,Hello.Service)
 * Create Service
 * Fractal -> Service : << bind: Hello->World>>
 * Main -> Fractal : start(Root)
 * Fractal -> Root : <<life-cycle: start>>
 * Root -> Hello : <<life-cycle: start>>
 * Root -> World : <<life-cycle: start>>
 * Main -> Root : main()
 * Root -> Start
 * Start -> Hello : main()
 * == end of bootstrap/initialization ==
 * ... continues with only Hello and World as shown on first diagram ...
 * 
 * @enduml
 */
package org.osadev.osa.model.newdes.helloworld.event;