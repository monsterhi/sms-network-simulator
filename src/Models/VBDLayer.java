package Models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import Common.BaseDeviceListener;
import Common.DeviceLayer;
import Common.SMSEvent;

import SMS.PDU;

/**
 * Holds all sending devices and forwards everything they send to the first
 * BTS layer. It also gives every new device the next free phone number.
 */
public class VBDLayer extends DeviceLayer<VBD>{

    private List<VBD> devices = new ArrayList<VBD>();

    /** Numbers are handed out one after another, starting from this one. */
    private long lastVBDNumber = 48881555000L;
    private VRDNumberProvider vrdNumberProvider;
    
    public VBDLayer(VRDNumberProvider vrdNumberProvider){
        this.vrdNumberProvider = vrdNumberProvider;
    }

    /**
     * Creates a new sending device for the given text and starts it at once.
     */
    public void addVBD(String message){
        var vbd = new VBD(++lastVBDNumber, message, vrdNumberProvider);
        vbd.addListener(new BaseDeviceListener() {

            @Override
            public void onTerminate(EventObject evt) {
                devices.remove(evt.getSource());
                fireOnDeviceRemoved((VBD)evt.getSource());
            }

            @Override
            public void onSendSMS(SMSEvent evt) {
                fireOnSMSSend(evt.getSMS());
            }

        });
        devices.add(vbd); 
        fireOnDeviceAdded(vbd);
    }

    /**
     * Writes a binary report about every device when the window is closed.
     * For each VBD the file holds its number encoded as in a PDU, the number
     * of messages it sent as four bytes, and the text it was sending.
     */
    public void saveInfo() throws IOException {
        File file = new File("VBDData.bin");
        FileOutputStream fos = new FileOutputStream(file);
        for(int i = 0; i < devices.size(); i++){
            fos.write(PDU.encodeNumber(devices.get(i).getNumber(), 11));
            fos.write(ByteBuffer.allocate(4).putInt(devices.get(i).getSentCount()).array());
            fos.write(PDU.encodeMessage(devices.get(i).getMessage()));
        }
        fos.close();
    }
}
