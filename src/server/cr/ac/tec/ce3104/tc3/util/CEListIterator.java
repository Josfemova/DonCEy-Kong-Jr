package cr.ac.tec.ce3104.tc3.util;
import java.io.Serializable;
import java.util.Iterator;

public class CEListIterator<T> implements Iterator<T>, Serializable {
    private TNode<T> current;
    public CEListIterator(TNode<T> first) {
        current = first;
    }
    @Override
    public boolean hasNext() {
        return current!=null;
    }
    @Override
    public T next() {
        TNode<T> temp = current;
        current = current.next;
        return temp.data;
    }

    public TNode<T> getCurrent() {
        return current;
    }

    public void setCurrent(TNode<T> current) {
        this.current = current;
    }
}
