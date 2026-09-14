package Common;

import java.util.ArrayList;
import java.util.List;

/**
 * One vertical column of stations, drawn as a single layer in the middle
 * panel of the window.
 *
 * The layer balances the load itself: a message always goes to the station
 * with the shortest queue, and when every station is busy a new one is
 * created and shown in the UI.
 *
 * @param <T> the concrete station type, BTS or BSC
 */
public abstract class StationLayer<T extends Station> extends DeviceLayer<T>{

    /** Queue length above which a station is considered busy. */
    private static final int MAX_QUEUE_SIZE = 5;

    protected List<T> stations = new ArrayList<T>();

    /** Builds a station of the right type with the next free number. */
    public abstract T createStation();

    /**
     * Routes one message to the least loaded station of this layer,
     * adding a new station if all of them are full.
     */
    public void receiveSMS(String sms){
        T station;
        var optional = this.stations.stream()
            .filter(d -> d.getSMSQueueSize() < MAX_QUEUE_SIZE)
            .sorted((d1, d2) -> d1.getSMSQueueSize() - d2.getSMSQueueSize())
            .findFirst();
        if(optional.isPresent()) {
            station = optional.get();
        }
        else {
            // Every station is busy, so the layer grows by one.
            station = createStation();
            station.addListener(new BaseDeviceListener() {

                @Override
                public void onSendSMS(SMSEvent evt) {
                    fireOnSMSSend(evt.getSMS());
                }
                
            });
            stations.add(station);
            fireOnDeviceAdded(station);
        }
        station.receiveSMS(sms);
    }

    /** Flushes every station of the layer, used when the layer is removed. */
    public void sendImmediately(){
        for(var station : stations){
            station.sendImmediately();
        }
    }
}