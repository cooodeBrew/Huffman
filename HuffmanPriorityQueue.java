import java.util.ArrayList;

public class HuffmanPriorityQueue<E extends Comparable<E>> {
    private ArrayList<E> pq;
    private int size;

    public HuffmanPriorityQueue() {
        size = 0;
        pq = new ArrayList<E>();
    }

    public boolean isEmpty() {
        return pq.isEmpty();
    }

    public int size() {
        return pq.size();
    }

    public void insert(E node) {
        if(node == null) {
            throw new IllegalArgumentException("Cannot enqueue a null item");
        }

        //If queue is empty, just add it
        if(pq.size() == 0) {
            pq.add(node);
            size++;
        } else {
            int oldSize = size;
            int index = 0;
            while(oldSize == size) {
                if(node.compareTo(pq.get(index)) >= 0) {
                    index++;
                    //If we've reached the end, just add it
                    if(index == size) {
                        pq.add(node);
                        size++;
                    }
                } else {
                    //If we've reached the right place in the queue, add it at that point
                    pq.add(index, node);
                    size++;
                }
            }
        }
    }

    public E extractMin() {
        if (pq.isEmpty()) {
            return null;
        }
        E min = pq.get(0);
        pq.remove(min);
        size--;
        return min;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (pq.size() > 1) {
            for (int i = 0; i < pq.size() - 1; i++) {
                sb.append(pq.get(i).toString() + ", ");
            }
            sb.append(pq.get(pq.size() - 1).toString());
        } else if (pq.size() == 1) {
            sb.append(pq.get(0).toString());
        }
        sb.append("]");
        return sb.toString();
    }
}
