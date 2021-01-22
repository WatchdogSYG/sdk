package flinderstemi;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.sample.R;

//TODO JavaDoc

/**
 *
 */
public class Global {
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
    public static final String SYSTEM = "FLINTEMI_SYSTEM";

    //res
    public static int MAX_PATROL_LOOPS;

    public static int SOC_HIGH;
    public static int SOC_LOW;
    public static int SOC_BUFFER;

    public static final String L_HOME_BASE = "home base";

    private static String eNull;
    private static String eGrinning;
    private static String eMask;
    private static String eWorried;
    private static String eSleeping;
    private static String eThinking;
    private static String eTear;
    private static String eRobot;


    public Global(Activity activity, Robot robot) {
        appContext = activity.getApplicationContext();
        resources = appContext.getResources();

        MAX_PATROL_LOOPS = resources.getInteger(R.integer.maxPatrolLoops);
        SOC_HIGH = resources.getInteger(R.integer.SOCH);
        SOC_LOW = resources.getInteger(R.integer.SOCL);
        SOC_BUFFER = resources.getInteger(R.integer.SOCBuffer);

        this.eNull = null;
        this.eGrinning = resources.getString(R.string.eGrinningSmiling);
        this.eMask = resources.getString(R.string.eMask);
        this.eWorried = resources.getString(R.string.eWorried);
        this.eSleeping = resources.getString(R.string.eSleeping);
        this.eThinking = resources.getString(R.string.eThinking);
        this.eTear = resources.getString(R.string.eTear);
        this.eRobot = resources.getString(R.string.eRobot);
    }

    public static class Emoji {
        public static String eNull = Global.eNull;
        public static String eGrinning = Global.eGrinning;
        public static String eMask = Global.eMask;
        public static String eWorried = Global.eWorried;
        public static String eSleeping = Global.eSleeping;
        public static String eThinking = Global.eThinking;
        public static String eTear = Global.eTear;
        public static String eRobot = Global.eRobot;
    }
}
