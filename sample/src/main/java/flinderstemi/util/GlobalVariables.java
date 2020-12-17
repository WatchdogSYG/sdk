package flinderstemi.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.sample.R;

//TODO JavaDoc

/**
 *
 */
public class GlobalVariables {
    public Context appContext;
    public static Resources resources;

    public static final String SEQUENCE = "FLINTEMI_SEQUENCE";
    public static final String LISTENER = "FLINTEMI_LISTENER";
    public static final String LOCATION = "FLINTEMI_LOCATION";
    public static final String SPEECH = "FLINTEMI_SPEECH";
    public static final String UI = "FLINTEMI_UI";
    public static final String BATTERY = "FLINTEMI_BATTERY";
    public static final String ABORT = "FLINTEMI_ABORT";//use in log E channel
    public static final String STATE = "FLINTEMI_STATE";

    //res
    public static int MAX_PATROL_LOOPS;

    public static int SOC_HIGH;
    public static int SOC_LOW;
    public static int SOC_BUFFER;

    public static final String L_HOME_BASE = "home base";

    public GlobalVariables(Activity activity, Robot robot) {
        appContext = activity.getApplicationContext();
        resources = appContext.getResources();

        MAX_PATROL_LOOPS = resources.getInteger(R.integer.maxPatrolLoops);
        SOC_HIGH = resources.getInteger(R.integer.SOCH);
        SOC_LOW = resources.getInteger(R.integer.SOCL);
        SOC_BUFFER = resources.getInteger(R.integer.SOCBuffer);
    }
}
