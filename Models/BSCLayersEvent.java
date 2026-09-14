package Models;

import java.util.EventObject;

/** Carries the BSC layer that was just added to or removed from the chain. */
public class BSCLayersEvent extends EventObject{
    
    private BSCLayer layer;

    public BSCLayersEvent(Object source, BSCLayer layer) {
        super(source);
        this.layer = layer;
    }

    public BSCLayer getLayer(){
        return layer;
    }
}
