package Common;

import java.util.EventObject;

/** Carries one SMS, already encoded as a PDU hexadecimal string. */
public class SMSEvent extends EventObject{

    private String sms;

    public SMSEvent(Object source, String sms) {
        super(source);
        this.sms = sms;
    }

    public String getSMS(){
        return sms;
    }
}
