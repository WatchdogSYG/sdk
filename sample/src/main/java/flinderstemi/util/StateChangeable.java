package flinderstemi.util;

import java.util.List;

/**
 * An interface describing the methods that should be called when listeners fire or the state is changed.
 */
public interface StateChangeable {
    //TODO make this not be an Object list

    /**
     * @param listenerList a list of listeners this state will respond to. The listenerList is of generic type Object since the Temi SDK does not extend a class for its listeners.
     */
    public void switchTo(List<Object> listenerList);

    public void switchFrom();

    public void hearTTS(String status);
}
