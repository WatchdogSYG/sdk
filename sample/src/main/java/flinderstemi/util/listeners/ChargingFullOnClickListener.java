package flinderstemi.util.listeners;

import android.view.View;

public class ChargingFullOnClickListener implements View.OnClickListener {

    private BatteryStateListener bsl;

    public ChargingFullOnClickListener(BatteryStateListener bsl) {

        this.bsl = bsl;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        bsl.fullWakeStateMachine();
    }
}
