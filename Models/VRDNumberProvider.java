package Models;

/**
 * Lets a sending device ask for a recipient without knowing anything about
 * the receiving layer.
 */
public interface VRDNumberProvider {

    /** @return a recipient number, or 0 when there is no receiver at all */
    long provide();
}
