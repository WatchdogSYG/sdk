package flinderstemi.util;

import android.content.Context;
import android.content.res.Resources;

import com.robotemi.sdk.sample.R;

//TODO JavaDoc

/**
 *
 */
public class GlobalVariables {
    public Context appContext;
    public static Resources resources;

    public static int MAX_PATROL_LOOPS = resources.getInteger(R.integer.maxPatrolLoops);

    public static int SOC_HIGH;
    public static int SOC_LOW;
    public static int SOC_BUFFER;

    public GlobalVariables() {
        resources = appContext.getResources();

        MAX_PATROL_LOOPS = resources.getInteger(R.integer.maxPatrolLoops);
        SOC_HIGH = resources.getInteger(R.integer.SOCH);
        SOC_LOW = resources.getInteger(R.integer.SOCL);
        SOC_BUFFER = resources.getInteger(R.integer.SOCBuffer);

    }
}
