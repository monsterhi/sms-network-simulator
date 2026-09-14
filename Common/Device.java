package Common;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * Base class for everything in the network that owns a phone number:
 * sending devices (VBD), receiving devices (VRD) and stations (BTS, BSC).
 *
 * A device does not know who is listening to it. It only fires events,
 * and the layer that owns the device reacts to them. This keeps the
 * model classes independent from the Swing panels that display them.
 */
public class Device {

    private long number;
    private List<DeviceListener> listeners = new ArrayList<DeviceListener>();

    public Device(long number){
        this.number = number;
    }

    public long getNumber() {
        return number;
    }

    public void addListener(DeviceListener listener){
        this.listeners.add(listener);
    }

    public void removeListener(DeviceListener listener){
        this.listeners.remove(listener);
    }

    /** Tells the owning layer that this device should be removed. */
    protected void fireOnTerminate(){
        for(var listener : listeners)
            listener.onTerminate(
                new EventObject(this)
            );
    }

    /** Hands an SMS over to the next layer of the network. */
    protected void fireOnSendSMS(String sms){
        for(var listener : listeners)
            listener.onSendSMS(
                new SMSEvent(this, sms)
            );
    }

    /** Notifies that an SMS arrived, so the UI can refresh its counters. */
    protected void fireOnReceiveSMS(String sms){
        for(var listener : listeners)
            listener.onReceiveSMS(
                new SMSEvent(this, sms)
            );
    }
}
