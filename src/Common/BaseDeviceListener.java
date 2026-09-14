package Common;

import java.util.EventObject;

/**
 * Empty implementation of {@link DeviceListener}. Extend it to override only
 * the events that matter instead of writing all three every time.
 */
public class BaseDeviceListener implements DeviceListener {

    @Override
    public void onTerminate(EventObject evt) {
    }

    @Override
    public void onSendSMS(SMSEvent evt) {
    }

    @Override
    public void onReceiveSMS(SMSEvent evt) {
    }
}
