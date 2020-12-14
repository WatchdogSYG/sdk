package flinderstemi.util.listeners;

import android.util.Log;
import android.view.View;

import com.robotemi.sdk.sample.MainActivity;

import flinderstemi.util.GlobalVariables;
import flinderstemi.StateMachine;

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
            //sm does exist, notify it
            Log.d(GlobalVariables.SEQUENCE, "StateMachine exists. notify()");
            synchronized (stateMachine) {
                stateMachine.setWakeCondition(new String[]{"BATTERYWAKE"});
                Log.v(GlobalVariables.SEQUENCE, stateMachine.toString() + " notify()");
                stateMachine.notify();
            }
        } else {
            //sm doesnt exist, make a new one from fresh
            Log.d("SEQUENCE", "StateMachine is null. main.startRoutineFresh()");
            setStateMachine(main.startRoutineFresh());
        }
    }
}
