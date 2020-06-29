package flinderstemi.util;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.sample.MainActivity;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class StateMachineTest {
    @Before
    public void setUp() throws Exception {
        Robot robot = Robot.getInstance();


        //this listener does not provide a way to select a specific tts request?
        StateMachine initialisation = new StateMachine(robot);
        System.out.println("FLINTEMI: Create Initialisation Routine");
        //textView.setText("Current Action: Initialising");

        synchronized (initialisation) {
            new Thread(initialisation).start();
            System.out.println("FLINTEMI: Started");
            Robot.TtsListener l = new MainActivity.ttsListener(robot, initialisation);
            System.out.println("FLINTEMI: Add new Listener");
            robot.addTtsListener(l);
            System.out.println("FLINTEMI: Added new Listener");
        }
    }

    @Test
    public void testIsCompleteSpeechSub() {
        assertTrue(true);
    }

    void testIsCompletePatrolSub() {
    }

    void testIsWaiting() {
    }

    void testSetWaiting() {
    }

    void testWaitFor() {
    }

    void testTryActionAgain() {
    }

    void testRun() {
    }
}
