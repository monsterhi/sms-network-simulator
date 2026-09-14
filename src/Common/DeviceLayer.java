package Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for a group of devices of the same kind, for example all VBDs
 * or all stations of one BTS column.
 *
 * A layer is the link between two neighbours in the chain: it listens to its
 * own devices and re-fires their messages upwards, so the next layer can
 * pick them up.
 *
 * @param <T> the type of device kept in this layer
 */
public class DeviceLayer<T> {

    private List<DeviceLayerListener<T>> listeners = new ArrayList<DeviceLayerListener<T>>();

    public void addListener(DeviceLayerListener<T> listener){
        this.listeners.add(listener);
    }

    public void removeListener(DeviceLayerListener<T> listener){
        this.listeners.remove(listener);
    }

    protected void fireOnDeviceRemoved(T device){
        for(var listener : listeners)
            listener.onDeviceRemoved(
                new DeviceLayerEvent<T>(this, device)
            );
    }

    protected void fireOnDeviceAdded(T device){
        for(var listener : listeners)
            listener.onDeviceAdded(
                new DeviceLayerEvent<T>(this, device)
            );
    }

    protected void fireOnSMSSend(String sms){
        for(var listener : listeners)
            listener.onDeviceSendSMS(
                new SMSEvent(this, sms)
            );
    }
}