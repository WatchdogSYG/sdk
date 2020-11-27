package flinderstemi.util.listeners;

import android.view.View;

import com.robotemi.sdk.sample.MainActivity;

import flinderstemi.util.StateMachine;

public class ChargingFullOnClickListener implements View.OnClickListener {

    private MainActivity main;
    private StateMachine stateMachine;

    private void setStateMachine(StateMachine sm) {
        this.stateMachine = sm;
    }

    public ChargingFullOnClickListener(MainActivity main, StateMachine stateMachine) {
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
        fullWakeStateMachine(main, stateMachine);
    }

    public void fullWakeStateMachine(MainActivity main, StateMachine stateMachine) {
        if (stateMachine != null) {
            //sm doesnt exist, make a new one from fresh
            main.startRoutineFresh();
            setStateMachine(stateMachine);

        } else {
            //sm does exist, notify it
            stateMachine.notify();
        }
    }
}
