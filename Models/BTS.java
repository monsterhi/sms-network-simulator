package Models;

import Common.Station;

/**
 * Base transceiver station, the outer layer of the network. It stands
 * between the devices and the BSC controllers and always holds a message
 * for the same fixed time.
 */
public class BTS extends Station {

    public BTS(int number) {
        super(number);
    }

    private final int periodInSeconds = 3;

    @Override
    public int getSMSProcessPeriodInSeconds() {
        return periodInSeconds;
    }
}
