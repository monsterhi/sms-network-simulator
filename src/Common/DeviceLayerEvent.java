package Common;

import java.util.EventObject;

/**
 * Carries the device that was just added to or removed from a layer.
 *
 * @param <T> the type of device kept in the layer
 */
public class DeviceLayerEvent<T> extends EventObject{

    private T device;

    public DeviceLayerEvent(Object source, T device) {
        super(source);
        this.device = device;
    }

    public T getDevice(){
        return device;
    }
}
