package Common;

import java.util.EventObject;

/**
 * Receives the events fired by a single {@link Device}.
 * See {@link BaseDeviceListener} if only one of the methods is needed.
 */
public interface DeviceListener {

    void onTerminate(EventObject evt);

    void onSendSMS(SMSEvent evt);

    void onReceiveSMS(SMSEvent evt);
}

