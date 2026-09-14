package Common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.time.LocalDateTime;

/**
 * Base class for a network station (BTS or BSC).
 *
 * A station keeps incoming messages in a queue and holds each of them for a
 * number of seconds defined by the subclass. Its own thread wakes up ten
 * times per second, sends out every message whose waiting time is over, and
 * goes back to sleep.
 *
 * The queue is guarded by a lock because the station thread and the sender
 * threads touch it at the same time.
 */
public abstract class Station extends Device implements Runnable{

    private Thread thread;
    private Lock lock = new ReentrantLock();
    private boolean stopThread = false;

    private List<SMSItem> smsQueue = new ArrayList<>();
    private int processedCount = 0;

    /** Creates the station and immediately starts its thread. */
    public Station(int number){
        super(number);
        this.thread = new Thread(this);
        thread.start();
    }

    public void run(){
        while(true){
            if (stopThread) {
                break;
            }
            try {
                this.lock.lock();
                try{
                    // Collect every message that has already waited long enough,
                    // then pass it on to the next layer.
                    var now = LocalDateTime.now();
                    var smsesToSend = smsQueue.stream()
                        .filter(s -> s.addedAt.plusSeconds(s.periodInSeconds).isBefore(now))
                        .toList();
                    for(var smsToSend : smsesToSend){
                        smsQueue.remove(smsToSend);
                        processedCount++;
                        fireOnSendSMS(smsToSend.sms);
                    }
                }
                finally{
                    this.lock.unlock();
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops the station and flushes the whole queue at once, ignoring the
     * waiting timers. Used when the user removes a BSC layer, so that no
     * message is lost with it.
     */
    public void sendImmediately(){
        stopThread = true;
        this.lock.lock();
        try{
            for(var smsToSend : smsQueue){
                processedCount++;
                fireOnSendSMS(smsToSend.sms);
            }
            smsQueue.clear();
        }
        finally{
            this.lock.unlock();
        }
    }

    public int getSMSQueueSize() {
        return this.smsQueue.size();
    }

    /** How long this station holds a message before sending it further. */
    public abstract int getSMSProcessPeriodInSeconds();

    /** Puts a message into the queue and starts its waiting time. */
    public void receiveSMS(String sms){
        this.lock.lock();
        try{
            smsQueue.add(
                new SMSItem(sms, LocalDateTime.now(), this.getSMSProcessPeriodInSeconds())
            );
            fireOnReceiveSMS(sms);
        }
        finally{
            this.lock.unlock();
        }
    }  
    
    public int getProcessedCount() {
        return processedCount;
    }

    /** One queued message together with the moment it arrived. */
    private static class SMSItem{
        String sms;
        LocalDateTime addedAt;
        int periodInSeconds;

        public SMSItem(String sms, LocalDateTime addedAt, int periodInSeconds){
            this.addedAt = addedAt;
            this.sms = sms;
            this.periodInSeconds = periodInSeconds;
        }
    }
}
