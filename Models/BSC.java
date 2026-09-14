package Models;

import Common.Station;

/**
 * Base station controller, used in the middle layers of the network.
 * Unlike a BTS it keeps every message for a random time, which makes the
 * traffic in the simulation look uneven.
 */
public class BSC extends Station{

    public BSC(int number) {
        super(number);
    }

    /** A fresh random delay between 5 and 14 seconds for each message. */
    @Override
    public int getSMSProcessPeriodInSeconds() {
        return 5 + (int)(Math.random() * 10);
    }
}
