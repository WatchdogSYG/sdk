package flinderstemi.util;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.sample.MainActivity;
import com.robotemi.sdk.sample.R;

/**
 * A class that provides methods for formatting and utilities when starting the routine.
 */
public class RoutineStartFormatHelper {

    private static Button startButton;
    private static Button stopButton;
    private static Button returnButton;
    private static TextView textViewVariable;
    private static StateMachine routine;
    private static MainActivity main;
    private static MediaPlayer mp;

    public RoutineStartFormatHelper(Button startButton, Button stopButton, Button returnButton, TextView textViewVariable, StateMachine routine, Robot robot, MainActivity main, MediaPlayer mp) {
        startButton.setVisibility(View.GONE);
        stopButton.setEnabled(true);
        returnButton.setEnabled(true);
        textViewVariable.setPadding(200, 0, 0, 0);
        textViewVariable.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        routine = new StateMachine(robot, main);
        System.out.println("FLINTEMI: Create Initialisation Routine");

        Context appContext = main.getApplicationContext();
        Resources resources = appContext.getResources();
        ;
        main.updateThought(resources.getString(R.string.cInit));

        synchronized (routine) {
            new Thread(routine).start();
        }
        //TODO set the correct file for music
        mp = MediaPlayer.create(appContext, R.raw.dragonforcettfaf);
        //mp = MediaPlayer.create(this, R.raw.bensound_theelevatorbossanova);
        mp.setLooping(true);
        mp.start();
    }

}
