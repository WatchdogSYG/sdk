package flinderstemi.util;

//TODO this javadoc
/**
 * JDoc
 */

import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.util.ArrayList;
import java.util.List;

import flinderstemi.util.listeners.DetectionListener;
import flinderstemi.util.listeners.TTSSequenceListener;
import flinderstemi.util.listeners.WaitSpeechListener;

public class StateMachine implements Runnable {

    /*******************************************************************************************
     *                                        Calibration                                      *
     ******************************************************************************************/

    final long idleTimeDuration = 15000L;
    final int socLow = 44; //adjust this for testing
    final int socHigh = 95; //adjust this to manage uptime as the charging of the battery will taper as SOC->100

    /*******************************************************************************************
     *                                        Callbacks                                        *
     ******************************************************************************************/

    SetTextViewCallback stvc;

    /*******************************************************************************************
     *                                        Variables                                        *
     ******************************************************************************************/

    //robot and state variables
    Robot robot;
    int state;

    //state names
    final int GREETING = 0;
    final int PATROLLING = 1;
    final int CONVERSING = 2;
    final int TERMINATED = 3;
    final int PAUSED = 4;
    final int RETURNING = 5;

    //lists of actions
    List<TtsRequest> speechQueue;
    List<String> locations;

    //state sentinel indices
    int speechIndex;
    int locationIndex;
    int locationLoopIndex;
    final int maxPatrolLoops = GlobalVariables.MAX_PATROL_LOOPS;
    final int initialState = GREETING;

    public String[] wakeCondition;

    DetectionListener dl;//the positioning of this variable allows adding and removing of a DetectionListener when we want so we can avoid interrupting TTS when looking for a user interaction

    //send messages to other thread through these variables
    private boolean completeSpeechSub;
    private boolean completePatrolSub;
    private boolean waiting;

    /*******************************************************************************************
     *                                   Getters & Setters                                     *
     ******************************************************************************************/

    public void setWakeCondition(String[] wakeCondition) {
        this.wakeCondition = wakeCondition;
    }

    public boolean isCompleteSpeechSub() {
        return completeSpeechSub;
    }

    public boolean isCompletePatrolSub() {
        return completePatrolSub;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    /*******************************************************************************************
     *                                     Constructor(s)                                      *
     ******************************************************************************************/

    public StateMachine(Robot robot, SetTextViewCallback stvc) {
        System.out.println("FLINTEMI: Constructor StateMachine(Robot robot)");

        this.stvc = stvc;

        this.stvc.updateThought("Constructing State Machine");


        //initialise variables
        completeSpeechSub = false;
        completePatrolSub = false;
        this.robot = robot;
        state = initialState;
        speechIndex = 0;
        locationIndex = 1;//not homebase at 0
        locationLoopIndex = 0;

        //initial greeting
        speechQueue = new ArrayList<TtsRequest>();
        speechQueue.add(TtsRequest.create("Starting custom routine. Beep boop.", false));
        speechQueue.add(TtsRequest.create("I will start my patrol.", false));

        locations = robot.getLocations();
        System.out.println("FLINTEMI: Locations = " + locations.toString());


        Robot.TtsListener sl = new TTSSequenceListener(robot, this);
        dl = new DetectionListener(robot, this);

        System.out.println("FLINTEMI: Add new Listener");
        robot.addTtsListener(sl);
        System.out.println("FLINTEMI: Added new Listener");


        System.out.println("End Constructor StateMachine(Robot robot)");
    }

    /*******************************************************************************************
     *                                     Functionality                                       *
     ******************************************************************************************/

    /**
     * Sets the state to StateMachine.TERMINATED and wakes the State thread
     */
    public void stop() {
        robot.stopMovement();
        synchronized (this) {
            state = TERMINATED;
            this.notify();
        }
    }

    /**
     * Speaks the request in <code>speechQueue</code> at the specified index.
     *
     * @param i the index of the TTS request
     */
    private void speech(int i) {
        robot.speak(speechQueue.get(i));
    }

    public void waitFor(long millis) {
        //start the timer and schedule the task (WaitspeechListaner now contains the timer and timertask)
        synchronized (this) {
            try {
                //announce
                robot.speak(TtsRequest.create("I will wait here for " + idleTimeDuration / 1000L + " seconds. Please, feel free to use my hand sanitiser dispenser.", false));

                //announce that we are going to wait, we will need a listener for this so the waiting can begin after the speech request ends
                robot.addTtsListener(new WaitSpeechListener(millis, stvc, dl, this, robot));

                System.out.println("FLINTEMI: wait()");
                this.wait();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public void wake() {//TODO define this with a String[] as an arg instead of using a global variable
        switch (state) {
            case GREETING://The listeners active in this state should be only the TTSSequenceListener
                switch (wakeCondition[1]) {
                    case "COMPLETED":
                        speechIndex++;
                        break;
                }
                break;
            case PATROLLING://The listeners active in this state should be the TTSListener, GoToLocationListener and DetectionStateListener
                switch (wakeCondition[0]) {
                    case "TTS":
                        switch (wakeCondition[1]) {
                            case "COMPLETED"://I hate how the sdk uses "completed" here instead of a consistent string as below
                                //get ready to speak the next string in line
                                speechIndex++;
                                break;
                        }
                        break;
                    case "LOCATION":
                        switch (wakeCondition[1]) {
                            case "COMPLETE"://I hate how the sdk uses "complete" here instead of a consistent string as above
                                //get ready to go to the next location in line
                                int prev = locationIndex;
                                locationIndex++;

                                //if we have reached the end of our location array, start again and increment the loop number
                                if (locationIndex >= locations.size()) {
                                    System.out.println("FLINTEMI: Reset the location index to 1 from " + prev);
                                    locationIndex = 1;//set it to the first location (0==homebase)
                                    System.out.println("FLINTEMI: Loops complete=" + locationLoopIndex);
                                    locationLoopIndex++;
                                }

                                //wait for 10s (needs to be calibrated)
                                waitFor(idleTimeDuration);
                                break;
                            case "ABORT":
                                //something went wrong with the movement to a location, try it again. i.e. dont increment the state or action
                                System.out.println("FLINTEMI: Something went wrong with the movement to a location, try again.");
                        }
                        break;
                    case "DETECTION":
                        System.out.println(wakeCondition[1]);
                        switch (wakeCondition[1]) {
                            case "IDLE":
                                //TODO
                                this.stvc.updateThought("DetectionState: IDLE");
                                break;
                            case "LOST":
                                //TODO
                                // this.stvc.updateThought("DetectionState: LOST");
                                break;
                            case "DETECTED":
                                //TODO
                                // this.stvc.updateThought("DetectionState: DETECTED");
                                break;
                        }
                        break;
                    default:
                        System.out.println("FLINTEMI: default wake switch");
                        break;
                }
                break;
            case CONVERSING:
                break;
            case TERMINATED:
                break;
            case PAUSED:
                break;
        }
    }

    private void nextAction() {
        //don't want to use reflection yet
        switch (state) {
            case GREETING:
                System.out.println("FLINTEMI: speak line at speechIndex=" + speechIndex);
                speech(speechIndex);
                System.out.println("FLINTEMI: speechIndex++");
                System.out.println("FLINTEMI: speechIndex=" + speechIndex);
                if (speechIndex >= speechQueue.size() - 1) {
                    //we have reached the end of the ttsrequest queue
                    System.out.println("FLINTEMI: completeSpeechSub set to true");
                    completeSpeechSub = true; //this will remove the ttsstatuslistener on the main thread once this goes into the waiting state

                    state = PATROLLING;
                }
                break;
            case PATROLLING://patrolling
                //TODO print message when no locations are saved as this throws an indexoutofbounds exception

                System.out.println("FLINTEMI: going to location=" + locationIndex + ", name=" + locations.get(locationIndex));
                stvc.updateThought("Going to the next waypoint ...");
                robot.speak(TtsRequest.create("I'm going to the next waypoint now. Goodbye.", false));

                robot.goTo(locations.get(locationIndex));

                //if there are no more loops to be done, go to next state. The equality operator allows for a mexPatrolLoops of -1 to result in infinite looping until manual termination.
                if (locationLoopIndex == maxPatrolLoops) {
                    System.out.println("FLINTEMI: Completed all loops");
                    stvc.updateThought("Completed all patrol loops. I will now return to the home base.");
                    completePatrolSub = true;
                    state = TERMINATED;
                }
                break;
            case CONVERSING:


                break;
            case TERMINATED:
                System.out.println("FLINTEMI: State set to 3 (terminate)");
                //robot.speak(TtsRequest.create("Routine Terminated", true)); //this may overwrite the previous ttsrequest
                break;
            case PAUSED:


                break;
            case RETURNING:
                robot.stopMovement();
                robot.speak(TtsRequest.create("I'm running out of battery so I will return to the home base to charge myself. Goodbye.", true));
                robot.goTo(locations.get(0));
                state = TERMINATED;
                break;
            default:
                break;
        }
    }

    /*******************************************************************************************
     *                                     Thread Loop                                         *
     ******************************************************************************************/

    @Override
    public void run() {
        System.out.println("FLINTEMI: Run");

        System.out.println("FLINTEMI: Started");


        while (state != TERMINATED) {
            System.out.println("FLINTEMI: Do action at state=" + state);

            int soc = robot.getBatteryData().getBatteryPercentage();
            if (soc <= socLow) {
                state = RETURNING;
                Log.d("Battery", Integer.toString(soc));
                Log.i("Logic", "Low battery, returning to base and set state=RETURNING");
                nextAction();
            } else {
                nextAction();
            }

            System.out.println("FLINTEMI: Finished action at state=" + state);//TODO fix print bug on state change
            System.out.println("FLINTEMI: synchronise routine");

            synchronized (this) {
                try {
                    System.out.println("FLINTEMI: try state machine wait");
                    if (state != TERMINATED) {
                        wait();
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                wake();
            }
        }
        //TERMINATED
    }
}
