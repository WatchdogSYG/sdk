package flinderstemi;

//TODO this javadoc
/**
 * JDoc
 */

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaRecorder;
import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.sample.MainActivity;
import com.robotemi.sdk.sample.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import flinderstemi.util.listeners.BatteryStateListener;
import flinderstemi.util.listeners.DetectionListener;
import flinderstemi.util.listeners.IdleSpeechListener;
import flinderstemi.util.listeners.PatrolLocationListener;
import flinderstemi.util.listeners.ReturnToChargeLocationListener;
import flinderstemi.util.listeners.TTSSequenceListener;
import flinderstemi.util.listeners.WaitSpeechListener;

public class StateMachine implements Runnable {

    /*******************************************************************************************
     *                                        Calibration                                      *
     ******************************************************************************************/

    private final long idleTimeDuration = 15000L;
    Context c;
    Resources r;

    /*******************************************************************************************
     *                                        Callbacks                                        *
     ******************************************************************************************/

    MainActivity main;

    /*******************************************************************************************
     *                                        State Vars                                       *
     ******************************************************************************************/

    //robot and state variables
    Robot robot;

    private int state;

    public static final int GREETING = 0;
    public static final int PATROLLING = 1;
    public static final int TERMINATED = 2;
    public static final int STUCK = 3;
    public static final int RETURNING = 4;

    //state sentinel indices
    private int speechIndex;
    private int locationIndex;
    private int locationLoopIndex;
    final int maxPatrolLoops = GlobalVariables.MAX_PATROL_LOOPS;
    final int initialState = GREETING;

    //send messages to other thread through these variables
    private boolean completeSpeechSub;
    private boolean completePatrolSub;

    //DEBUG
    MediaRecorder mr;

    /*******************************************************************************************
     *                                   Controlled Listeners                                  *
     ******************************************************************************************/

    private DetectionListener dl;//the positioning of this variable allows adding and removing of a DetectionListener when we want so we can avoid interrupting TTS when looking for a user interaction
    private PatrolLocationListener pll;//The location listener when patrolling that can be added and removed when temi diverges from the patrolling information
    private BatteryStateListener bsl;
    private IdleSpeechListener isl;

    /*******************************************************************************************
     *                                    Other Variables                                      *
     ******************************************************************************************/

    public String[] wakeCondition;
    private List<TtsRequest> speechQueue;
    private List<String> locations;

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

    public void addDetectionListener() {
        robot.addOnDetectionStateChangedListener(dl);
        Log.d(GlobalVariables.LISTENER, "Added DetectionListener implements OnDetectionStateChangedListener");
        Log.v(GlobalVariables.LISTENER, "Added DetectionListener: " + dl.toString());
    }

    public void removeDetectionListener() {
        robot.removeOnDetectionStateChangedListener(dl);
        Log.d(GlobalVariables.LISTENER, "Removed DetectionListener");
        Log.v(GlobalVariables.LISTENER, "Removed DetectionListener implements OnDetectionStateChangedListener: " + dl.toString());
    }

    //TODO move pll management to this context only
    public void setPLL(PatrolLocationListener pll) {
        this.pll = pll;
        robot.addOnGoToLocationStatusChangedListener(pll);
        Log.d(GlobalVariables.LISTENER, "Added PatrolLocationListener");
        Log.v(GlobalVariables.LISTENER, "Added PatrolLocationListener extends OnGoToLocationStatusChangedListener: " + pll.toString());

    }

    public void removePLL() {
        robot.removeOnGoToLocationStatusChangedListener(pll);
        Log.d(GlobalVariables.LISTENER, "Removed PatrolLocationListener");
        Log.v(GlobalVariables.LISTENER, "Removed PatrolLocationListener extends OnGoToLocationStatusChangedListener: " + pll.toString());
    }

    public void setBSL(BatteryStateListener bsl) {
        this.bsl = bsl;
        robot.addOnBatteryStatusChangedListener(bsl);
        Log.d(GlobalVariables.LISTENER, "Added BatteryStateListener");
        Log.v(GlobalVariables.LISTENER, "Added BatteryStateListener extends OnBatteryStatusChangedListener: " + bsl.toString());
    }

    public void removeBSL(BatteryStateListener batteryStateListener) {
        robot.removeOnBatteryStatusChangedListener(batteryStateListener);
        Log.d(GlobalVariables.LISTENER, "Removed BatteryStateListener");
        Log.v(GlobalVariables.LISTENER, "Removed BatteryStateListener extends OnBatteryStatusChangedListener: " + batteryStateListener.toString());
    }

    //no args since isl should be singleton
    public void setISL() {
        robot.addTtsListener(isl);
        Log.d(GlobalVariables.LISTENER, "Set IdleSpeechListener");
        Log.v(GlobalVariables.LISTENER, "Set IdleSpeechListener extends TtsListener: " + isl.toString());
    }

    public void removeISL() {
        robot.removeTtsListener(isl);
        Log.d(GlobalVariables.LISTENER, "Removed IdleSpeechListener");
        Log.v(GlobalVariables.LISTENER, "Removed IdleSpeechListener extends TtsListener: " + isl.toString());
    }

    public void setState(int s) {
        int r = state;
        state = s;
        Log.v(GlobalVariables.STATE, "Changed state from " + r + " to " + s);
    }

    /*******************************************************************************************
     *                                     Constructor(s)                                      *
     ******************************************************************************************/

    /**
     * @param robot
     * @param main
     */
    public StateMachine(@NotNull Robot robot, @NotNull MainActivity main) {
        Log.d(GlobalVariables.SEQUENCE, "Constructing State Machine");
        Log.v(GlobalVariables.SEQUENCE,
                "Constructing StateMachine(Robot robot, MainActivity main)\n" +
                        "StateMachine\t=\t" + this.toString() + "\n" +
                        "robot\t=\t" + robot.toString() + "\n" +
                        "StateMachine\t=\t" + main.toString());

        this.main = main;
        this.robot = robot;
        //robot instanceof  ? (() robot) : null;

        Log.v(GlobalVariables.STATE, "StateMachine getApplicationContext(), getResources()");
        c = main.getApplicationContext();
        r = c.getResources();

        main.updateThought(r.getString(R.string.constructingStateMachine));

        //initialise variables
        completeSpeechSub = false;
        completePatrolSub = false;

        //initial conditions
        state = initialState;
        speechIndex = 0;
        locationIndex = 1;//not homebase at 0
        locationLoopIndex = 0;
        Log.d(GlobalVariables.STATE, "Set Initial Conditions");
        Log.v(GlobalVariables.STATE,
                "ICL\n" +
                        "state\t\t\t=\t" + state + "\n" +
                        "speechIndex\t\t=\t" + speechIndex + "\n" +
                        "locationIndex\t=\t" + locationIndex + "\n" +
                        "locationLoopIndex\t=\t" + locationLoopIndex);

        //setup initial greeting
        speechQueue = new ArrayList<TtsRequest>();
        speechQueue.add(TtsRequest.create(r.getString(R.string.greeting1), false));
        speechQueue.add(TtsRequest.create(r.getString(R.string.greeting2), false));
        Log.d(GlobalVariables.SPEECH, "Populated initial speechQueue");
        Log.v(GlobalVariables.SPEECH, speechQueue.toString());

        locations = robot.getLocations();
        Log.d(GlobalVariables.LOCATION, "robot.getLocations()");
        Log.v(GlobalVariables.LOCATION, "Locations = " + locations.toString());

        dl = new DetectionListener(robot, this);
        isl = new IdleSpeechListener(robot);

        Robot.TtsListener sl = new TTSSequenceListener(robot, this);
        Log.d(GlobalVariables.LISTENER, "Instantiated new TTSSequenceListener(robot, this) implements TtsListener");
        Log.v(GlobalVariables.LISTENER, "Instantiated new TTSSequenceListener: " + sl.toString());

        robot.addTtsListener(sl);
        Log.d(GlobalVariables.LISTENER, "Added TTSSequenceListener implements TtsListener");
        Log.v(GlobalVariables.LISTENER, "Added TTSSequenceListener: " + sl.toString());

        Log.d(GlobalVariables.SEQUENCE, "Completed Constructing StateMachine(Robot robot, MainActivity main)");

        //DEBUG


        try {
            mr = new MediaRecorder();

            mr.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mr.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

            String s = "rec.3gp";
            File f = new File(c.getFilesDir() + File.separator + s);

            mr.setOutputFile(c.getFilesDir() + File.separator + s);
            Log.v(GlobalVariables.SYSTEM, c.getFilesDir() + File.separator + s);
            mr.prepare();
            mr.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /*******************************************************************************************
     *                                     Functionality                                       *
     ******************************************************************************************/

    /**
     * Sets the state to StateMachine.TERMINATED and notify()s the State thread.
     */
    public void stop() {
        Log.i(GlobalVariables.SEQUENCE, "Stopping robot movement");
        robot.stopMovement();
        synchronized (this) {
            setState(TERMINATED);
            this.notify();
        }
    }

    //Speaks the request in speechQueue at the specified index.
    private void speakSpeechQueue(int i) {
        Log.v(GlobalVariables.SPEECH, "speechQueue index\t=\t" + i);
        speak(speechQueue.get(i));
    }

    //TODO move this to a global context
    private void speak(@NotNull TtsRequest ttsRequest) {
        Log.v(GlobalVariables.SPEECH, "TtsRequest UUID = " + ttsRequest.getId().toString());
        Log.d(GlobalVariables.SPEECH, "robot.speak( \"" + ttsRequest.getSpeech() + "\" )");
        robot.speak(ttsRequest);
    }

    //Synchronises on this StateMachine and TTS announces how long the machine will wait for. A WaitSpeechListener is created that instantiates a times and wakes this thread when the time is up.
    private void waitFor(long millis) {
        //start the timer and schedule the task (WaitspeechListener now contains the timer and timertask)
        synchronized (this) {
            try {
                //announce that we are going to wait, we will need a listener for this so the waiting can begin after the speech request ends
                speak(TtsRequest.create(r.getString(R.string.waitSpeech1) + " " + idleTimeDuration / 1000L + " " + r.getString(R.string.waitSpeech2), false));

                Log.d(GlobalVariables.LISTENER, "Instantiating new WaitSpeechListener(millis, main, dl, this, robot)");
                WaitSpeechListener wsl = new WaitSpeechListener(millis, main, dl, this, robot);
                Log.v(GlobalVariables.LISTENER, "WaitSpeechListener: " + wsl.toString());
                Log.d(GlobalVariables.LISTENER, "Adding new WaitSpeechListener: " + wsl.toString());
                robot.addTtsListener(wsl);

                Log.d(GlobalVariables.SEQUENCE, "synchronised stateMachine.wait(): " + this.toString());
                this.wait();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private void wake() {//TODO define this with a String[] as an arg instead of using a global variable
        Log.v(GlobalVariables.STATE, "wakeCondition\t=\t" + Arrays.toString(wakeCondition));
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
                                //TERMINATE
                                //TODO make this robust
                                state = STUCK;
                                System.out.println("FLINTEMI: Something went wrong with the movement to a location, try again.");
                        }
                    case "BATTERYWAKE":
                        //setPLL(pll);
                        break;
                    case "DETECTION":
                        System.out.println(wakeCondition[1]);
                        switch (wakeCondition[1]) {
                            case "IDLE":
                                //TODO
                                this.main.updateThought("DetectionState: IDLE");
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
            case TERMINATED:
                break;
            case STUCK:
                break;
            case RETURNING:
                setState(PATROLLING);
                removeBSL(bsl);
                setPLL(pll);
                try {
                    Log.d(GlobalVariables.SEQUENCE, "Attempting to start MediaPlayer:\t" + main.getMediaPlayer().toString() + "\t isPlaying=" + main.getMediaPlayer().isPlaying());
                    if (!main.getMediaPlayer().isPlaying()) {
                        main.getMediaPlayer().start();
                    }
                    Log.d(GlobalVariables.SEQUENCE, "Start()ed MediaPlayer:\t" + main.getMediaPlayer().toString() + "\t isPlaying=" + main.getMediaPlayer().isPlaying());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        setWakeCondition(null);
    }

    private void nextAction() {
        //don't want to use reflection yet
        Log.d(GlobalVariables.STATE, "state\t=\t" + state);
        switch (state) {
            case GREETING:
                Log.d(GlobalVariables.SEQUENCE, "switch (state = GREETING = " + GREETING + ")");

                //speak the next line
                speakSpeechQueue(speechIndex);
                Log.v(GlobalVariables.STATE, "speechIndex\t=\t" + speechIndex);

                //check if we are done speaking
                if (speechIndex >= speechQueue.size() - 1) {
                    //we have reached the end of the ttsrequest queue, start patrolling
                    completeSpeechSub = true; //this will remove the ttsstatuslistener on the main thread once this goes into the waiting state
                    Log.v(GlobalVariables.STATE, "completeSpeechSub\t=\ttrue");
                    setState(PATROLLING);

                    //debug
                    mr.stop();
                    mr.release();
                }
                break;
            case PATROLLING:
                Log.d(GlobalVariables.SEQUENCE, "switch (state = PATROLLING = " + PATROLLING + ")");
                //TODO print message when no locations are saved as this throws an indexoutofbounds exception

                main.updateThought("Going to the next waypoint ...");
                speak(TtsRequest.create("I'm going to the next waypoint now. Goodbye.", false));

                Log.i(GlobalVariables.LOCATION, "Going to the next location");
                Log.d(GlobalVariables.LOCATION, "GoToLocation:\n" +
                        "locationIndex\t=\t" + locationIndex + "\n" +
                        "name\t\t\t=\t" + locations.get(locationIndex));
                robot.goTo(locations.get(locationIndex));
                setISL();
                //if there are no more loops to be done, go to next state. The equality operator allows for a maxPatrolLoops of -1 to result in infinite looping until manual termination.
                if (locationLoopIndex == maxPatrolLoops) {
                    Log.d(GlobalVariables.SEQUENCE, "Completed all loops, returning to HB.");
                    main.updateThought("Completed all patrol loops. I will now return to the home base.");

                    Log.v(GlobalVariables.STATE, "completePatrolSub\t=\ttrue");
                    completePatrolSub = true;

                    setState(TERMINATED);
                }
                break;
            case TERMINATED:
                Log.d(GlobalVariables.SEQUENCE, "switch (state = TERMINATED = " + TERMINATED + ")");
                speak(TtsRequest.create("Routine Terminated", true)); //this may overwrite the previous ttsrequest
                break;
            case STUCK:
                Log.d(GlobalVariables.SEQUENCE, "switch (state = STUCK = " + STUCK + ")");
                speak(TtsRequest.create("Help, I am stuck. Please notify a staff member of this error. Help, I am stuck. Please notify a staff member of this error. Help, I am stuck. Please notify a staff member of this error.", true));
                setState(TERMINATED);
                break;
            case RETURNING:
                Log.d(GlobalVariables.SEQUENCE, "switch (state = RETURNING = " + RETURNING + ")");
                removePLL();
                robot.stopMovement();
                Log.i(GlobalVariables.LOCATION, "Robot Movement Stopped");

                speak(TtsRequest.create("I'm running out of battery so I will return to the home base to charge myself. Goodbye.", true));

                robot.goTo(locations.get(0));

                robot.addOnGoToLocationStatusChangedListener(new ReturnToChargeLocationListener(main, robot, this, main.getStartButton(), main.getMediaPlayer()));//TODO move context?
                break;
            default:
                Log.d(GlobalVariables.SEQUENCE, "switch (state = DEFAULT)");
                break;
        }
    }

    /*******************************************************************************************
     *                                     Thread Loop                                         *
     ******************************************************************************************/

    /**
     *
     */
    @Override
    public void run() {
        Log.i(GlobalVariables.SEQUENCE, "StateMachine is now running.");
        Log.d(GlobalVariables.SEQUENCE, "StateMachine run()");
        Log.v(GlobalVariables.SEQUENCE, "StateMachine run(): " + this.toString());

        while (state != TERMINATED) {
            Log.v(GlobalVariables.SEQUENCE, "while loop top");

            //TODO check if terminated first otherwise a stuck temi will remain stuck?
            if (BatteryStateListener.batteryState(robot.getBatteryData().getBatteryPercentage()) == BatteryStateListener.LOW) {
                Log.i(GlobalVariables.SEQUENCE, "Low Battery. Returning to Home Base.");
                state = RETURNING;
                Log.d(GlobalVariables.STATE, "state\t=\tRETURNING");
            }

            Log.d(GlobalVariables.SEQUENCE, "Do action at state\t=\t" + state);
            nextAction();
            Log.d(GlobalVariables.SEQUENCE, "Finished action at state\t=\t" + state);

            Log.d(GlobalVariables.SEQUENCE, "synchronise routine");
            synchronized (this) {
                try {
                    Log.d(GlobalVariables.SEQUENCE, "try wait");
                    if (state != TERMINATED) {
                        wait();
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                Log.d(GlobalVariables.SEQUENCE, "wake()");
                wake();
            }
        }
        //TERMINATED
        Log.d(GlobalVariables.SEQUENCE, "Terminated: Reached end of Routine");
    }
}
