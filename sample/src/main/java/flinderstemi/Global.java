package flinderstemi;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.robotemi.sdk.sample.R;

/**
 * This class contains parameters, Strings and Emoji chars from the Android resource xmls.
 * Instantiate this as early as possible in case a class accesses this.
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

    public static int MAX_PATROL_LOOPS;

    public static int SOC_HIGH;
    public static int SOC_LOW;
    public static int SOC_BUFFER;

    public static final String L_HOME_BASE = "home base";

    //private static String eNull;
    private static String eGrinning;
    //private static String eMask;
    private static String eWorried;
    private static String eSleeping;
    private static String eThinking;
    private static String eTear;
    private static String eRobot;

    /**
     * Initialises the static variables that can be used throughout this application.
     *
     * @param activity The main <code>Activity</code> of this application that will use the parameters and {@link Emoji} definitions.
     */
    public Global(Activity activity) {
        appContext = activity.getApplicationContext();
        resources = appContext.getResources();

        MAX_PATROL_LOOPS = resources.getInteger(R.integer.maxPatrolLoops);
        SOC_HIGH = resources.getInteger(R.integer.SOCH);
        SOC_LOW = resources.getInteger(R.integer.SOCL);
        SOC_BUFFER = resources.getInteger(R.integer.SOCBuffer);

        //eNull = null;
        eGrinning = resources.getString(R.string.eGrinningSmiling);
        //eMask = resources.getString(R.string.eMask);
        eWorried = resources.getString(R.string.eWorried);
        eSleeping = resources.getString(R.string.eSleeping);
        eThinking = resources.getString(R.string.eThinking);
        eTear = resources.getString(R.string.eTear);
        eRobot = resources.getString(R.string.eRobot);
    }

    /**
     * Access this class to use the prescribed emoji for the FaceTextView character.
     */
    public static class Emoji {
        //public static String eNull = Global.eNull;//unused
        public static String eGrinning = Global.eGrinning;
        //public static String eMask = Global.eMask;//unused
        public static String eWorried = Global.eWorried;
        public static String eSleeping = Global.eSleeping;
        public static String eThinking = Global.eThinking;
        public static String eTear = Global.eTear;
        public static String eRobot = Global.eRobot;
    }
}
