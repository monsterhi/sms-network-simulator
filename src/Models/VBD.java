package Models;

import Common.Device;

import SMS.HEX;
import SMS.PDU;

/**
 * Virtual sending device.
 *
 * Each VBD runs its own thread and keeps sending the same text, over and
 * over, to a randomly chosen receiver. The user controls it from the UI:
 * the slider changes the pause between messages, the combo box switches
 * the device between ACTIVE and WAITING, and the button shuts it down.
 */
public class VBD extends Device implements Runnable {

    private Thread thread;
    private boolean stopThread = false;

    private String message;

    /** Pause between two messages, in milliseconds. */
    private int frequency = 300;
    private VBDStateEnum state = VBDStateEnum.Active;
    private VRDNumberProvider vrdNumberProvider;
    private int sentCount = 0;

    public VBD(long number, String message, VRDNumberProvider vrdNumberProvider){
        super(number);
        this.message = message;
        this.vrdNumberProvider = vrdNumberProvider;

        this.thread = new Thread(this);
        thread.start();
    }

    /** Stops the thread and asks the layer to remove this device. */
    public void terminate() {
        stopThread = true;
        fireOnTerminate();
    }

    public void run(){
        while(true){
            try {
                if (stopThread) {
                    break;
                }
                if(state == VBDStateEnum.Active){
                    // Pick a random receiver and encode the text as a PDU,
                    // exactly as a real phone would before sending.
                    var toNumber =  vrdNumberProvider.provide();
                    if(toNumber > 0){
                        var sms = HEX.toHexadecimal(
                            PDU.createSMS(message, getNumber(), toNumber)
                        );
                        fireOnSendSMS(sms);
                        sentCount++;
                    }
                    else{
                        System.out.println("Please create at least one VRD");
                    }
                }
                Thread.sleep(frequency);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getMessage(){
        return message;
    }

    public VBDStateEnum getState() {
        return this.state;
    }

    public void setState(VBDStateEnum value){
        state = value;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public void setFrequency(int value){
        this.frequency = value;
    }

    public int getSentCount(){
        return sentCount;
    }
}
