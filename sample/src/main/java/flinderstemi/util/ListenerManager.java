package flinderstemi.util;

//TODO JDoc

import com.robotemi.sdk.listeners.OnBatteryStatusChangedListener;

import java.util.ArrayList;

/**
 * A class that handles the addition, logging and removal of listeners.
 */
public class ListenerManager {

    private ArrayList<TemiListener> l;

    public void add(TemiListener temiListener){
        l.add(temiListener);
    }
    public TemiListener get(int i){
        return l.get(i);
    }
    public ListenerManager() {

    }
}