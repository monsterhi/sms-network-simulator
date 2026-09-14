package Models;

/** The two states a sending device can be switched between in the UI. */
public enum VBDStateEnum {
    /** The thread keeps running but sends nothing. */
    Waiting,
    /** The device sends messages at its current frequency. */
    Active
}
