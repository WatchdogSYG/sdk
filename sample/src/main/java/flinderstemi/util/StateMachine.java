package flinderstemi.util;

//TODO this javadoc
/**
 * JDoc
 */

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StateMachine implements Runnable {

    //robot and state variables
    Robot robot;
    int state;

    //lists of actions
    List<TtsRequest> speechQueue;
    List<String> locations;

    //state sentinel indices
    int speechIndex;
    int locationIndex;
    int locationLoopIndex;
    final int maxPatrolLoops = 100;
    final int initialState = 0;
    private int previousState;

    //send messages to other thread through these variables
    private boolean completeSpeechSub;
    private boolean completePatrolSub;
    private boolean manualWaiting;

    public boolean isCompleteSpeechSub() {
        return completeSpeechSub;
    }

    public boolean isCompletePatrolSub() {
        return completePatrolSub;
    }


    public void arrived() {
        previousState = state;
        state = 2;
    }

    public void waitFor(int seconds) {
        Timer timer = new Timer();
        timer.schedule(new WaitTask(), 1000 * seconds);
    }

    class WaitTask extends TimerTask {
        public void run() {
            manualWaiting = true;
            state = previousState;
            this.cancel();
        }
    }


    //TODO should this be public? also it crashes
    public void tryActionAgain() {
        System.out.println("FLINTEMI: Manually repeat state.");

        //how do we handle a repeat at each state?
        //the indices of state and sub-state are always incremented before this can be called by the MainActivity thread.
        // Therefore, we can look at the state we are programming for and decrement the relevant value.
        switch (state) {
            case 1:
                //decrement speechIndex
                speechIndex--;
                System.out.println("State set to state=" + state + ", speechIndex=" + speechIndex);
                break;
            case 2:
                //decrement locationIndex
                locationIndex--;
                System.out.println("State set to state=" + state + ", locationIndex=" + locationIndex);
        }

        //now the while loop will start again and call the fns of the (sub)state +1-1.
    }

    public StateMachine(Robot robot) {
        System.out.println("FLINTEMI: InitialisationSequence Constructor");
        completeSpeechSub = false;
        completePatrolSub = false;
        this.robot = robot;
        state = initialState;
        speechIndex = 0;
        locationIndex = 1;//not homebase at 0
        locationLoopIndex = 0;
        manualWaiting = false;

        speechQueue = new ArrayList<TtsRequest>();
        speechQueue.add(TtsRequest.create("Starting custom routine. Beep boop.", true));
        speechQueue.add(TtsRequest.create("I will start my patrol.", true));

        locations = robot.getLocations();
        System.out.println("FLINTEMI Locations=" + locations.toString());
        System.out.println("End constructor");
    }

    private void speech(int i) {
        robot.speak(speechQueue.get(i));
    }

    private void nextAction() {
        //don't want to use reflection yet
        switch (state) {
            case 0://initial speaks
                System.out.println("FLINTEMI: speak line at speechIndex=" + speechIndex);
                speech(speechIndex);
                System.out.println("FLINTEMI: speechIndex++");
                speechIndex++;
                System.out.println("FLINTEMI: speechIndex=" + speechIndex);
                if (speechIndex >= speechQueue.size()) {
                    //we have reached the end of the ttsrequest queue
                    System.out.println("FLINTEMI: completeSpeechSub set to true");
                    completeSpeechSub = true; //this will remove the ttsstatuslistener on the main thread once this goes into the waiting state
                    state = 1;
                }
                break;
            case 1://patrolling
                //TODO fix the slight synch problem where the Routine complete message occurs when the last goto is started and not when it is finished
                if (locationIndex == locations.size() - 1) {
                    //this is the last location in the loop, go to the last location
                    System.out.println("FLINTEMI: going to location=" + locationIndex + ", name=" + locations.get(locationIndex));
                    robot.goTo(locations.get(locationIndex));
                    System.out.println("FLINTEMI: Reset the location index to 1");
                    locationIndex = 1;//set it to the first location (0==homebase)
                    locationLoopIndex++;
                    System.out.println("FLINTEMI: Loops complete=" + locationLoopIndex);
                    //if there are no more loops to be done, go to next state
                    if (locationLoopIndex == maxPatrolLoops) {
                        System.out.println("FLINTEMI: Completed all loops");
                        completePatrolSub = true;
                        state = 3;
                    }
                } else {
                    //go to the next location
                    System.out.println("FLINTEMI: going to location=" + locationIndex + ", name=" + locations.get(locationIndex));
                    robot.goTo(locations.get(locationIndex));
                    locationIndex++;
                    System.out.println("FLINTEMI: Increment locationIndex to locationIndex=" + locationIndex);
                }
                break;
            case 2://stop and wait
                System.out.println("FLINTEMI: Timer for 10 seconds.");
                waitFor(10);
                break;
            case 3://terminate

                break;
            default:

                break;
        }
    }

    @Override
    public void run() {
        System.out.println("FLINTEMI: Run");
        //first index is 0

        while (state != 3) {
            System.out.println("FLINTEMI: Do action at index=" + state);
            nextAction();
            System.out.println("FLINTEMI: Finished action at index=" + state);//TODO fix print bug on state change
            System.out.println("FLINTEMI: synchronise routine");

            synchronized (this) {
                try {
                    System.out.println("FLINTEMI: try state machine wait");
                    //if we are not using a timer to wait, wait forever until notified
                    if (!manualWaiting) {
                        wait();
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        robot.speak(TtsRequest.create("Routine Complete.", true));
    }
}
