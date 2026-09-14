package Common;

/**
 * Empty implementation of {@link DeviceLayerListener}. Extend it to override
 * only the events that matter.
 *
 * @param <T> the type of device kept in the layer
 */
public class BaseDeviceLayerListener<T> implements DeviceLayerListener<T>{

    @Override
    public void onDeviceRemoved(DeviceLayerEvent<T> evt) {
    }

    @Override
    public void onDeviceAdded(DeviceLayerEvent<T> evt) {
    }

    @Override
    public void onDeviceSendSMS(SMSEvent evt) {
    }
}
