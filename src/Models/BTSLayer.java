package Models;

import Common.StationLayer;

/**
 * A column of BTS stations. The application uses two of them: one next to
 * the senders and one next to the receivers.
 */
public class BTSLayer extends StationLayer<BTS>{
    
    private int lastNumber = 0;

    @Override
    public BTS createStation() {
        return new BTS(++lastNumber);
    }
}