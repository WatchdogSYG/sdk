package flinderstemi.util;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.util.List;

import flinderstemi.listeners.TTSListener;

public class TalkingState {
    private Robot robot;

    private int action = 0;
    private TTSListener ttsl;
    private List<String> content;

    public TalkingState(List<String> content, TTSListener ttsl) {
        robot = Robot.getInstance();
        this.content = content;
        this.ttsl = ttsl;
    }

    public int switchTo() {
        System.out.println("FLINTEMI: Remove TTSListener");
        robot.removeTtsListener(ttsl);
        //TODO choose transition state properly
        return 3;
    }

    public void hearTTS(String status) {
        switch (status) {
            case "COMPLETED":
                iterate(1);
                break;
            case "CANCELLED":
                iterate(0);
                break;
        }
    }

    private void speech(int i) {
        System.out.println("FLINTEMI: Speaking: " + content.get(i));
        robot.speak(TtsRequest.create(content.get(i), false));
    }

    public void iterate(int increment) {
        System.out.println("FLINTEMI: Speak the line at action index=" + action);
        speech(action);
        System.out.println("FLINTEMI: Increment action...");
        action = action + increment;
        System.out.println("FLINTEMI: New value of action=" + action);

        if (action >= content.size()) {
            //we have reached the end of the ttsrequest queue
            System.out.println("FLINTEMI: End of TTSRequest queue");

            //we should now remove listeners and increment the state
            switchTo();
        }
    }
}
