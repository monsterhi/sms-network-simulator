package Models;

import java.util.ArrayList;
import java.util.List;

import Common.BaseDeviceLayerListener;
import Common.SMSEvent;

/**
 * Manages the chain of BSC layers shown in the middle of the window.
 *
 * The user can add a layer or remove the last one at any time, and the
 * simulation keeps at least one layer alive. Messages enter the chain at the
 * first layer and leave it after the last one, on their way to the second
 * BTS layer.
 */
public class BSCLayers {

    private List<BSCLayersListener> listeners = new ArrayList<BSCLayersListener>();
    private BSCLayer last;
    private BSCLayer first;

    /** Creates the first layer if the chain is still empty. */
    public void ensureFirstLayer() {
        if (first == null) {
            first = last = createNewLayer();
            fireOnLayerAdded(last);
        }
    }

    /** Appends a new layer at the end of the chain. */
    public void addLayer() {
        ensureFirstLayer();
        var layer = createNewLayer();
        layer.setPrev(last);
        last.setNext(layer);
        last = layer;
        fireOnLayerAdded(last);
    }

    /**
     * Removes the last layer, unless it is the only one left. The layer stops
     * accepting new messages and passes everything it still holds further at
     * once, so nothing is lost.
     */
    public void removeLayer() {
        ensureFirstLayer();
        if(first != last) {
            var prev = last.getPrev();
            prev.setNext(null);
            last.sendImmediately();
            fireOnLayerRemoved(last);
            last = prev;
        }
    }

    /** Lets a message into the chain through its first layer. */
    public void receiveSMS(String sms) {
        ensureFirstLayer();
        first.receiveSMS(sms);
    }

    public void addListener(BSCLayersListener listener){
        this.listeners.add(listener);
    }

    public void removeListener(BSCLayersListener listener){
        this.listeners.remove(listener);
    }

    /**
     * Builds a layer and wires it to its neighbour: whatever the layer sends
     * goes to the next layer, or leaves the chain when this is the last one.
     */
    private BSCLayer createNewLayer() {
        var layer = new BSCLayer();
        layer.addListener(new BaseDeviceLayerListener<BSC>() {

            @Override
            public void onDeviceSendSMS(SMSEvent evt) {
                if(layer.getNext() != null){
                    layer.getNext().receiveSMS(evt.getSMS());
                }
                else{
                    fireOnSMSSend(evt.getSMS());
                }
            }
            
        });
        return layer;
    }

    private void fireOnLayerRemoved(BSCLayer device){
        for(var listener : listeners)
            listener.onLayerRemoved(
                new BSCLayersEvent(this, device)
            );
    }

    private void fireOnLayerAdded(BSCLayer device){
        for(var listener : listeners)
            listener.onLayerAdded(
                new BSCLayersEvent(this, device)
            );
    }

    private void fireOnSMSSend(String sms){
        for(var listener : listeners)
            listener.onDeviceSendSMS(
                new SMSEvent(this, sms)
            );
    }
}
