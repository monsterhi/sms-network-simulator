package Common;

/**
 * Receives the events fired by a {@link DeviceLayer}: devices appearing and
 * disappearing, and messages leaving the layer.
 *
 * @param <T> the type of device kept in the layer
 */
public interface DeviceLayerListener<T> {
    
    void onDeviceRemoved(DeviceLayerEvent<T> evt);

    void onDeviceAdded(DeviceLayerEvent<T> evt);

    void onDeviceSendSMS(SMSEvent evt);
}