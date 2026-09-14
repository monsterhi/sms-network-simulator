package Models;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import Common.BaseDeviceListener;
import Common.DeviceLayer;

import SMS.HEX;
import SMS.PDU;

/**
 * Holds all receiving devices and delivers incoming messages to them.
 * It is also the place where senders ask for a random recipient.
 */
public class VRDLayer extends DeviceLayer<VRD>{

    private List<VRD> devices = new ArrayList<VRD>();

    /** Numbers are handed out one after another, starting from this one. */
    private long lastVRDNumber = 48881777000L;

    /** Creates a new receiving device with the next free number. */
    public void addVRD(){
        var vrd = new VRD(++lastVRDNumber);
        vrd.addListener(new BaseDeviceListener() {

            @Override
            public void onTerminate(EventObject evt) {
                devices.remove(evt.getSource());
                fireOnDeviceRemoved((VRD)evt.getSource());
            }

        });
        devices.add(vrd); 
        fireOnDeviceAdded(vrd);
    }

    /**
     * Reads the recipient number out of the PDU and hands the message to that
     * device. If no device with such a number exists any more, the loss is
     * reported in the console.
     */
    public void receiveSMS(String sms) {
        var toNumber = PDU.getToNumberFromSMS(
            HEX.fromHexadecimal(sms)            
        );
        var optional = devices.stream()
            .filter(d -> d.getNumber() == toNumber)
            .findFirst();
        if(optional.isPresent()) {
            optional.get().receive(sms);
            System.out.println("Device: +" + toNumber + " received: " + sms);
        }
        else{
            System.out.println("Device: +" + toNumber + " not found");
        }
    }

    /**
     * Picks a random recipient for a sender.
     *
     * @return the chosen number, or 0 when no receiving device exists yet
     */
    public long getRandomNumber() {
        if(devices.size() == 0){
            return 0;
        }
        int index = (int)(Math.random() * devices.size());
        return devices.get(index).getNumber();
    }
}
