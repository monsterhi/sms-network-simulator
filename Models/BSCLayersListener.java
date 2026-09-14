package Models;

import Common.SMSEvent;

/**
 * Receives the events of the BSC chain: layers appearing and disappearing,
 * and messages leaving the last layer.
 */
public interface BSCLayersListener {
    
    void onLayerRemoved(BSCLayersEvent evt);

    void onLayerAdded(BSCLayersEvent evt);

    void onDeviceSendSMS(SMSEvent evt);
}
