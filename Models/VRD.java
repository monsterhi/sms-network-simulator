package Models;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Common.Device;

/**
 * Virtual receiving device.
 *
 * It counts the messages delivered to its number. When the user ticks the
 * check box in the UI, the device thread resets that counter every ten
 * seconds.
 */
public class VRD extends Device implements Runnable{

    /** Ticks of the thread loop that make up the ten second reset period. */
    private static final int CLEAR_AFTER_TICKS = 100;

    private Thread thread;
    private Lock lock = new ReentrantLock();

    private int receivedCount = 0;
    private boolean clearReceivedCount = false;


    public VRD(long number){
        super(number);
        thread = new Thread(this);
        thread.start();
    }

    public void terminate(){
        fireOnTerminate();
    }

    /** Accepts a delivered message and updates the counter shown in the UI. */
    public void receive(String sms){
        this.lock.lock();
        try{
            this.receivedCount++;
            fireOnReceiveSMS(sms);
        }
        finally{
            this.lock.unlock();
        }
    }

    public int getReceivedCount(){
        return receivedCount;
    }

    @Override
    public void run() {
        // One loop pass takes 100 ms, so 100 passes are the required 10 seconds.
        int i =  0;
        while(true){
            try {
                if(clearReceivedCount && i >= CLEAR_AFTER_TICKS){
                    this.lock.lock();
                    try{
                        receivedCount = 0;
                        i = 0;
                    }
                    finally{
                        this.lock.unlock();
                    }
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    public void setClearReceivedCount(boolean value){
        this.clearReceivedCount = value;
    }

    public boolean getClearReceivedCount(){
        return this.clearReceivedCount;
    }
}
