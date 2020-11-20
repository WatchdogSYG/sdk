package flinderstemi.util.listeners;

import android.view.View;

//TODO Quality - Check the Auto-start toggleable state on initialisation.

/**
 * This Listener should be set on a button at a time when it is on low battery and is trying to charge.
 * It should toggle the auto-start function of the routine. On initialisation, the Listener assumes that the auto-start function is on.
 */
public class ChargingLowOnClickListener implements View.OnClickListener {

    private BatteryStateListener bsl;

    public ChargingLowOnClickListener(BatteryStateListener bsl) {
        this.bsl = bsl;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        bsl.toggleAutoStart();
    }
}
