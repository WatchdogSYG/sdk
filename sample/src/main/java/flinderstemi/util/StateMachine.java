package flinderstemi.util;

//TODO this javadoc
/**
 * JDoc
 */

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.util.ArrayList;
import java.util.List;

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
    final int maxPatrolLoops = 3;
    final int initialState = 0;

    //send messages to other thread through these variables
    private boolean completeSpeechSub;
    private boolean completePatrolSub;

    public boolean isCompleteSpeechSub() {
        return completeSpeechSub;
    }

    public boolean isCompletePatrolSub() {
        return completePatrolSub;
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
            case 2://found

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
                    wait();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        robot.speak(TtsRequest.create("Routine Complete.", true));
    }
}
