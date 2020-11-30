package flinderstemi.util.listeners;

import android.view.View;

import com.robotemi.sdk.sample.MainActivity;

import flinderstemi.util.StateMachine;

//TODO Quality - Check the Auto-start toggleable state on initialisation.

/**
 * This Listener should be set on a button at a time when the robot is on battery and is trying to charge.
 * If the button is pressed, start the patrol regardless of current SOC.
 */
public class ChargingHighOnClickListener implements View.OnClickListener {

    MainActivity main;
    StateMachine stateMachine;

    public ChargingHighOnClickListener(MainActivity main, StateMachine stateMachine) {
        this.main = main;
        this.stateMachine = stateMachine;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (stateMachine != null) {
            synchronized (stateMachine) {
                stateMachine.notify();
            }
        } else {
            main.startRoutineFresh();
        }
    }
}