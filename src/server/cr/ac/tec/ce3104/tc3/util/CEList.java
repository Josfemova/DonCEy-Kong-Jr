package cr.ac.tec.ce3104.tc3.util;

import java.io.Serializable;
import java.util.Iterator;

import org.json.simple.JSONArray;

public class CEList<T> implements Iterable<T>, Serializable {
    private TNode<T> first;
    private TNode<T> last;
    private int size;

    public CEList() {
        first = last = null;
    }

    public CEList(T elements[]) {
        for (T element : elements) {
            add(element);
        }
    }

    public void add(T value) {
        if (first == null) {
            first = new TNode<T>(value);
            first.prev = null;
            last = first;
        } else {
            TNode<T> elemento = new TNode<>(value);
            elemento.prev = last;
            last.next = elemento;
            last = elemento;
        }
        size += 1;
    }

    public void insert(T value, int index) {
        if (index == 0) {
            if (first != null) {
                first.prev = new TNode<>(value);
                first.prev.next = first;
                first = first.prev;
            } else {
                first = last = new TNode<>(value);
            }
            size += 1;
        } else if (index == size() - 1) {
            TNode<T> newNode = new TNode<>(value);
            last.prev.next = newNode;
            newNode.prev = last.prev;
            newNode.next = last;
            last.prev = newNode;
            size += 1;
        } else if (index < size() - 1) {

            TNode<T> current = first;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            TNode<T> newNode = new TNode<>(value);
            current.prev.next = newNode;
            newNode.prev = current.prev;
            current.prev = newNode;
            newNode.next = current;
            size += 1;
        } else if (index == size()) {
            add(value);
        }
    }

    public T get(int index) {
        TNode<T> current = first;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    public void remove(int index) {
        if (index == 0) {
            removeFirst();
        } else if (index == size) {
            removeLast();
        } else {
            TNode<T> current = first;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            current.prev.next = current.next;
            current.next.prev = current.prev;
        }
    }

    /**
     * Elimina el nodo en la posición específicada y retorna el valor almacenado
     * 
     * @param index
     * @return
     */
    public T pop(int index) {
        if (first != null && last != null) {
            int listSize = size();
            T data;
            if (index == listSize - 1) {
                data = last.data;
                removeLast();
            } else if (index == 0) {
                data = first.data;
                removeFirst();
            } else {
                TNode<T> current = first;
                for (int i = 0; i < index; i++) {
                    current = current.next;
                }
                current.prev.next = current.next;
                current.next.prev = current.prev;
                data = current.data;
                size -= 1;
            }
            return data;
        }
        return null;
    }

    public void remove(TNode<T> nodeT) {
        if (nodeT.equals(first)) {
            removeFirst();
        } else if (nodeT.equals(last)) {
            removeLast();
        } else {

            nodeT.prev.next = nodeT.next;
            nodeT.next.prev = nodeT.prev;
            size -= 1;
        }
    }

    public void removeValue(T value) {
        TNode<T> current = first;
        while (!current.data.equals(value) && current.next != null) {
            current = current.next;
        }
        if (current.data.equals(value)) {
            remove(current);
        }
    }

    public void removeFirst() {
        if (first != null) {
            if (first.next != null) {
                first = first.next;
                first.prev = null;
            } else {
                first = last = null;
            }
            size -= 1;
        }
    }

    public void removeLast() {
        if (last != null) {
            if (last.prev != null) {
                last = last.prev;
                last.next = null;
            } else {
                last = first = null;
            }
            size -= 1;
        }
    }

    public void clear() {
        first = last = null;
        size = 0;
    }

    public int size() {
        int cnt = 0;
        TNode<T> current = first;
        while (current != null) {
            cnt++;
            current = current.next;
        }
        return cnt;
    }

    public TNode<T> getFirst() {
        return first;
    }

    public TNode<T> getLast() {
        return last;
    }

    public T getFirstValue() {
        return first.data;
    }

    public T getLastValue() {
        return last.data;
    }

    public boolean isEmpty() {
        return first == null;
    }

    public boolean contains(T value) {
        boolean contains = false;
        for (T nodeData : this) {
            if (value.equals(nodeData)) {
                contains = true;
            }
        }
        return contains;
    }

    public int nodesWithValue(T value) {
        int cnt = 0;
        for (T nodeData : this) {
            if (value.equals(nodeData)) {
                cnt++;
            }
        }
        return cnt;
    }

    @Override
    public String toString() {
        String toString = "[|-|";
        for (T value : this) {
            toString += value.toString() + "|-|";
        }
        toString += "]";
        return toString;
    }

    @Override
    public Iterator<T> iterator() {
        return new CEListIterator<T>(first);
    }

    public JSONArray toJsonArray() {
        JSONArray array = new JSONArray();
        if (!isEmpty()) {
            if (first.data instanceof Integer || first.data instanceof Double) {
                for (T element : this) {
                    array.add(element);
                }
            } else {
                for (T element : this) {
                    array.add(element.toString());
                }
            }
        }
        return array;
    }
}
