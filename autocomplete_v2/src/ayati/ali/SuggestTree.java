package ayati.ali;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

public class SuggestTree {
    
    private final Random random = new Random();
    private final int k;
    private Node root;
    private int size;

    public SuggestTree(int k) {
        if(k < 1)
            throw new IllegalArgumentException();
        this.k = k;
        root = null;
        size = 0;
    }

    public Node getAutocompleteSuggestions(String prefix) {
        return search(prefix);
    }

    public Entry getEntry(String term) {
        Node n = search(term);
        if(n == null || n.charEnd > term.length())
            return null;
        else
            return n.entry;
    }

    public Iterator iterator() {
        return new Iterator();
    }

    public int size() {
        return size;
    }

    public void put(String term, int weight, String extra_data) {
        if(term.isEmpty() || term.length() > Short.MAX_VALUE)
            throw new IllegalArgumentException();
        if(root == null) {
            root = new Node(term, weight, 0, null, extra_data);
            finishInsertion(root);
            return;
        }
        int i = 0;
        Node n = root;
        while(true) {
            if(term.charAt(i) < n.firstChar) {
                if(n.left == null) {
                    n.left = new Node(term, weight, i, n, extra_data);
                    finishInsertion(n.left);
                    return;
                }else
                    n = n.left;
            }else if(term.charAt(i) > n.firstChar) {
                if(n.right == null) {
                    n.right = new Node(term, weight, i, n, extra_data);
                    finishInsertion(n.right);
                    return;
                }else
                    n = n.right;
            }else{
                while(++i < n.charEnd) {
                    if(i == term.length() || term.charAt(i) != n.charAt(i)) {
                        n = split(n, i);
                        break;
                    }
                }
                if(i < term.length()) {
                    if(n.mid == null) {
                        n.mid = new Node(term, weight, i, n, extra_data);
                        finishInsertion(n.mid);
                        return;
                    }else
                        n = n.mid;
                }else{
                    if(n.entry == null) {
                        n.entry = new Entry(term, weight, extra_data);
                        finishInsertion(n);
                    }else if(n.entry.weight < weight)
                        increaseWeight(n, weight);
                    else if(n.entry.weight > weight)
                        reduceWeight(n, weight);
                    return;
                }
            }
        }
    }

    public void remove(String term) {
        Node n = search(term);
        if(n == null || n.entry == null || n.charEnd > term.length())
            return;
        randomizeDeletion(n);
        Entry e = n.entry;
        n.entry = null;
        if(n.mid == null) {
            Node p = parent(n);
            delete(n);
            n = p;
        }
        if(n != null && n.entry == null && n.mid.left == null && n.mid.right == null) {
            Node p = parent(n);
            merge(n, n.mid);
            n = p;
        }
        removeFromLists(e, n);
        size--;
    }
    
    private Node search(String s) {
        if(s.isEmpty())
            throw new IllegalArgumentException();
        int i = 0;
        Node n = root;
        while(n != null) {
            if(s.charAt(i) < n.firstChar)
                n = n.left;
            else if(s.charAt(i) > n.firstChar)
                n = n.right;
            else{
                while(++i < n.charEnd) {
                    if(i == s.length())
                        return n;
                    else if(s.charAt(i) != n.charAt(i))
                        return null;
                }
                if(i == s.length())
                    return n;
                else
                    n = n.mid;
            }
        }
        return null;
    }
    
    private Node split(Node n, int position) {
        Node s = new Node(n, position);
        if(n.list.length == k)
            s.list = Arrays.copyOf(n.list, k);
        else // the list is copied in insertIntoLists()
            s.list = n.list;
        if(n.left != null)
            n.left.up = s;
        if(n.right != null)
            n.right.up = s;
        if(n == root)
            root = s;
        else if(n == n.up.left)
            n.up.left = s;
        else if(n == n.up.right)
            n.up.right = s;
        else
            n.up.mid = s;
        n.firstChar = n.charAt(position);
        n.left = n.right = null;
        n.up = s;
        return s;
    }

    private void merge(Node n, Node m) {
        m.firstChar = n.firstChar;
        m.left = n.left;
        m.right = n.right;
        m.up = n.up;
        if(n.left != null)
            n.left.up = m;
        if(n.right != null)
            n.right.up = m;
        if(n == root)
            root = m;
        else if(n == n.up.left)
            n.up.left = m;
        else if(n == n.up.right)
            n.up.right = m;
        else
            n.up.mid = m;
    }

    private void delete(Node n) {
        if(n == root)
            root = null;
        else if(n == n.up.left)
            n.up.left = null;
        else if(n == n.up.right)
            n.up.right = null;
        else
            n.up.mid = null;
    }
    
    private void finishInsertion(Node n) {
        randomizeInsertion(n);
        insertIntoLists(n);
        size++;
    }
    
    private void randomizeInsertion(Node n) {
        n.entry.priority = random.nextInt();
        n.priority = higherPriority(n.entry, n.mid);
        while(n != root && n.up.priority < n.priority) {
            if(n == n.up.left)
                rotateRight(n.up);
            else if(n == n.up.right)
                rotateLeft(n.up);
            else{
                n.up.priority = n.priority;
                n = n.up;
            }
        }
    }
    
    private void randomizeDeletion(Node n) {
        int p = n.entry.priority;
        n.entry.priority = Integer.MIN_VALUE;
        while(n != null && n.priority == p) {
            n.priority = higherPriority(n.entry, n.mid);
            Node h = higherPriorityNode(n.left, n.right);
            while(h != null && h.priority >= n.priority) {
                if(h == n.left)
                    rotateRight(n);
                else
                    rotateLeft(n);
                h = higherPriorityNode(n.left, n.right);
            }
            n = parent(n);
        }
    }
    
    private int higherPriority(Entry e, Node n) {
        if(e == null)
            return n.priority;
        else if(n == null)
            return e.priority;
        else if(e.priority < n.priority)
            return n.priority;
        else
            return e.priority;
    }
    
    private Node higherPriorityNode(Node n, Node m) {
        if(n == null)
            return m;
        else if(m == null)
            return n;
        else if(n.priority < m.priority)
            return m;
        else
            return n;
    }
    
    private void rotateLeft(Node n) {
        Node r = n.right;
        n.right = r.left;
        if(r.left != null)
            r.left.up = n;
        r.up = n.up;
        if(n == root)
            root = r;
        else if(n == n.up.left)
            n.up.left = r;
        else if(n == n.up.right)
            n.up.right = r;
        else
            n.up.mid = r;
        r.left = n;
        n.up = r;
    }
    
    private void rotateRight(Node n) {
        Node l = n.left;
        n.left = l.right;
        if(l.right != null)
            l.right.up = n;
        l.up = n.up;
        if(n == root)
            root = l;
        else if(n == n.up.left)
            n.up.left = l;
        else if(n == n.up.right)
            n.up.right = l;
        else
            n.up.mid = l;
        l.right = n;
        n.up = l;
    }
    
    private void insertIntoLists(Node n) {
        Entry e = n.entry;
        for( ; n != null; n = parent(n)) {
            if(n.mid == null)
                n.list = new Entry[1];
            else if(n.list.length < k)
                n.list = Arrays.copyOf(n.list, n.list.length + 1);
            else if(e.weight <= n.list[k - 1].weight)
                return;
            int i = n.list.length - 1;
            while(i > 0 && e.weight > n.list[i - 1].weight) {
                n.list[i] = n.list[i - 1];
                i--;
            }
            n.list[i] = e;
        }
    }
    
    private void increaseWeight(Node n, int newWeight) {
        Entry e = n.entry;
        e.weight = newWeight;
        for( ; n != null; n = parent(n)) {
            int i = n.listIndexOf(e);
            if(i == -1) {
                if(e.weight <= n.list[k - 1].weight)
                    return;
                else
                    i = k - 1;
            }
            while(i > 0 && e.weight > n.list[i - 1].weight) {
                n.list[i] = n.list[i - 1];
                i--;
            }
            n.list[i] = e;
        }
    }
    
    private void reduceWeight(Node n, int newWeight) {
        Entry e = n.entry;
        e.weight = newWeight;
        for( ; n != null; n = parent(n)) {
            int i = n.listIndexOf(e);
            if(i == -1)
                return;
            while(i < n.list.length - 1 && e.weight < n.list[i + 1].weight) {
                n.list[i] = n.list[i + 1];
                i++;
            }
            n.list[i] = e;
            if(i == k - 1) {
                Entry t = topUnlistedTerm(n);
                if(t != null && t.weight > e.weight)
                    n.list[i] = t;
            }
        }
    }
    
    private void removeFromLists(Entry e, Node n) {
        for( ; n != null; n = parent(n)) {
            int i = n.listIndexOf(e);
            if(i == -1)
                return;
            while(i < n.list.length - 1) {
                n.list[i] = n.list[i + 1];
                i++;
            }
            n.list[i] = e;
            if(n.list.length < k)
                n.list = Arrays.copyOf(n.list, n.list.length - 1);
            else{
                Entry t = topUnlistedTerm(n);
                if(t == null)
                    n.list = Arrays.copyOf(n.list, k - 1);
                else
                    n.list[i] = t;
            }
        }
    }

    private Entry topUnlistedTerm(Node n) {
        Entry t = null;
        if(n.entry != null && n.listIndexOf(n.entry) == -1)
            t = n.entry;
        for(Node c = leftmostChild(n); c != null; c = rightSibling(c)) {
            for(Entry e : c.list) {
                if(n.listIndexOf(e) == -1) {
                    if(t == null || t.weight < e.weight)
                        t = e;
                    break;
                }
            }
        }
        return t;
    }

    private Node leftmostChild(Node n) {
        n = n.mid;
        if(n != null) {
            while(n.left != null)
                n = n.left;
        }
        return n;
    }

    private Node rightSibling(Node n) {
        if(n.right != null) {
            n = n.right;
            while(n.left != null)
                n = n.left;
            return n;
        }else{
            while(n == n.up.right)
                n = n.up;
            if(n == n.up.left)
                return n.up;
            else
                return null;
        }
    }
    
    private Node parent(Node n) {
        while(n != root && n != n.up.mid)
            n = n.up;
        return n.up;
    }

    public class Iterator {
        
        private Node next;
        
        private Iterator() {
            if(root == null)
                next = null;
            else
                next = firstEntry(root);
        }

        public boolean hasNext() {
            return (next != null);
        }

        public Entry next() {
            if(next == null)
                throw new NoSuchElementException();
            Entry e = next.entry;
            next = nextEntry(next);
            return e;
        }
        
        private Node firstEntry(Node n) {
            while(true) {
                while(n.left != null)
                    n = n.left;
                if(n.entry == null)
                    n = n.mid;
                else
                    return n;
            }
        }
        
        private Node nextEntry(Node n) {
            if(n.mid != null)
                return firstEntry(n.mid);
            else if(n.right != null)
                return firstEntry(n.right);
            else{
                while(n.up != null) {
                    if(n == n.up.left) {
                        if(n.up.entry != null)
                            return n.up;
                        else
                            return firstEntry(n.up.mid);
                    }else if(n == n.up.mid && n.up.right != null)
                        return firstEntry(n.up.right);
                    else
                        n = n.up;
                }
                return null;
            }
        }
    }

    public static class Node {
        
        private Entry[] list;
        private Entry entry;
        private char firstChar;
        private short charEnd;
        private int priority;
        private Node left, mid, right;
        private Node up; // parent in the ternary search tree
        
        private Node(String term, int weight, int charStart, Node up, String extra_data) {
            entry = new Entry(term, weight, extra_data);
            firstChar = term.charAt(charStart);
            charEnd = (short) term.length();
            left = mid = right = null;
            this.up = up;
        }
        
        private Node(Node n, int charEnd) {
            entry = null;
            firstChar = n.firstChar;
            this.charEnd = (short) charEnd;
            priority = n.priority;
            left = n.left;
            mid = n;
            right = n.right;
            up = n.up;
        }

        public Entry getSuggestion(int index) {
            return list[index];
        }

        public int listLength() {
            return list.length;
        }
        
        private char charAt(int index) {
            if(entry != null)
                return entry.term.charAt(index);
            else
                return list[0].term.charAt(index);
        }
        
        private int listIndexOf(Entry e) {
            for(int i = 0; i < list.length; i++) {
                if(list[i] == e)
                    return i;
            }
            return -1;
        }
    }

    public static class Entry {
        
        private final String term;
        private int weight;
        private int priority;
        private String extra_data;
        
        private Entry(String term, int weight, String extra_data) {
            this.term = term;
            this.weight = weight;
            this.extra_data = extra_data;
        }

        public String getTerm() {
            return term;
        }

        public int getWeight() {
            return weight;
        }

        public String getExtra_data(){ return extra_data;}
    }
}