package cr.ac.tec.ce3104.tc3.util;
import java.io.Serializable;

public class TNode<T> implements Serializable {
    TNode<T> next;
    TNode<T> prev;
    T data;
    public TNode(T data){
        this.data = data;
        this.next = this.prev = null;
    }
}
