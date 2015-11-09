package uk.me.westmacott.islands;

public class DoublePointTriplet {

    public final DoublePoint prev;
    public final DoublePoint here;
    public final DoublePoint next;

    public DoublePointTriplet(DoublePoint prev, DoublePoint here, DoublePoint next) {
        this.prev = prev;
        this.here = here;
        this.next = next;
    }
}
