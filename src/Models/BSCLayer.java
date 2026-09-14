package Models;

import Common.StationLayer;

/**
 * A column of BSC stations. Layers know their neighbours, so together they
 * form a chain that a message travels through from the first layer to the
 * last one.
 */
public class BSCLayer extends StationLayer<BSC>{
    
    private BSCLayer next;
    private BSCLayer prev;
    private int lastNumber = 0;

    @Override
    public BSC createStation() {
        return new BSC(++lastNumber);
    }

    /** The layer a message goes to next, or null for the last layer. */
    public BSCLayer getNext(){
        return next;
    }

    public void setNext(BSCLayer next){
        this.next = next;
    }

    public BSCLayer getPrev(){
        return prev;
    }

    public void setPrev(BSCLayer prev){
        this.prev = prev;
    }
}
