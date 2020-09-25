// BST.java: a simple unbalanced binary search tree.
// For homework, you do not modify this file.

// BST<K> behaves as a sorted set of keys of type K (no duplicates, no values).
// It also has indexed operations, like a sorted array (methods "get" and "rank").
// It has a modification counter (modCount), and a fail-safe iterator.

// As a base case, we say that a Node with no children has height 1.
// This agrees with the Goodrich textbook.

public class BST<K extends Comparable<K>> implements Iterable<K>
{
    // We build a binary search tree of Node objects.
    // We have no parent links, and external nodes are represented by null.
    // Remark: class Node could be static, but that would make declarations harder.
    class Node
    {
	// Data fields:
	K key;                  // the data item, set by constructor
	Node left, right;	// children, null means external
	int size=1, height=1;	// size and height of this subtree

        // Internal node with external children (nulls):
	Node(K k) { key = k; size = 1; height = 1; }

        // Internal node with possibly internal children:
	Node(K k, Node l, Node r) {
	    key = k;
	    left = l;
            right = r;
	    update();
	}

	// update() recomputes size and height fields of this Node, assuming
        // the left and right children have correct fields.
	void update() {
	    size = 1 + size(left) + size(right);
	    height = 1 + Math.max(height(left), height(right));
	}

	// Print Node as "[key, h=height, s=size]"
	public String toString() {
	    return "[" + key + ", h=" + height + ", s=" + size + "]";
	}

	// Check deep equality of subtrees (keys and shape)
	public boolean equals(Object o) {
	    if (!(o instanceof BST.Node)) return false;
	    BST.Node n = (BST.Node) o;
	    if (!key.equals(n.key)) return false;
	    if (left==null) { if (n.left!=null) return false; }
	    else { if (!left.equals(n.left)) return false; }
	    if (right==null) { if (n.right!=null) return false; }
	    else { if (!right.equals(n.right)) return false; }
	    return true;
	}
    }

    // BST data fields:
    Node root = null; // root of tree, null when empty
    int modCount = 0; // incremented by each modification

    // Helper methods (allowing n==null for external, used by Node.update):
    int height(Node n) { return (n==null ? 0 : n.height); }
    int size(Node n) { return (n==null ? 0 : n.size); }

    // Most of the following public methods call an internal variant.
    // The internal variants have these differences in behavior:
    //  * recursive, operates on a subtree t, returns root of revised subtree.
    //  * the find* methods return a Node containing the sought item, or null
    //  * min/max methods assume the given subtree t is non-empty

    // Return value indicates whether x was actually inserted.
    // Possibly false, since we do not allow duplicates.
    public boolean insert(K x) {
        int pre = modCount;
        root = insert(x, root);
        return pre < modCount;
    }

    // Return value indicates whether x was actually removed.
    // Possibly false, since maybe x was not found.
    public boolean remove(K x) {
        int pre = modCount;
        root = remove(x, root);
        return pre < modCount;
    }

    // Return the key which equals x, or null if none was found.
    public K find(K x) {
        Node at = find(x, root);
        return (at==null) ? null : at.key;
    }

    public int size() { return size(root); }

    public boolean isEmpty() { return root==null; }

    // For debugging: print tree with indentation that shows depth.
    // We don't print modCount here.
    public void printTree() {
        System.out.printf("Tree has %d nodes:%n", size());
        printTree(root, 0, 0);
        System.out.println("End.");
    }
    int printTree(Node t, int count, int depth) {
        if (t==null) return count;
        count = printTree(t.left, count, depth+1);
        System.out.printf("%2d:%"+(2*depth+1)+"s%s%n", count++, "", t.key);
        count = printTree(t.right, count, depth+1);
        return count;
    }

    // Now the internal methods.  Thee are "protected" so they are still
    // usable by a subclass.

    // Try to insert x into subtree t, return root of revised subtree.
    Node insert(K x, Node t) {
        if(t == null) {
            modCount++;
            return new Node(x);
        }
        int compare = x.compareTo(t.key);
        if (compare < 0) { t.left = insert(x, t.left); return fixup(t); }
        if (compare > 0) { t.right = insert(x, t.right); return fixup(t); }
        // Found a duplicate: ignore x and do not change t.
        return t;
    }

    // Try to remove x from subtree t, return root of revised subtree.
    Node remove(K x, Node t) {
        if (t == null) return null;
        int compare = x.compareTo(t.key);
        if (compare < 0) { t.left = remove(x, t.left); return fixup(t); }
        if (compare > 0) { t.right = remove(x, t.right); return fixup(t); }

        // Found it!  We will somehow remove t.key from the tree.
        // Easy case: if there is a null (external) child, then return the other
        // child as the new root of this subtree.  No need to fixup() subtree.
        if (t.left==null) { modCount++; return t.right; }
        if (t.right==null) { modCount++; return t.left; }
        // Hard case: two non-null children.
        // Replace t.key by a copy of its successor (min value in right subtree),
        t.key = findMin(t.right).key;
        // Now remove that min key from the right subtree.
        t.right = remove(t.key, t.right);
        return fixup(t);
    }

    // This method assumes t is not null, returns the found node.
    Node findMin(Node t) {
        while(t.left != null) t = t.left;
        return t;
    }

    // This method returns the found node, or null if not found.
    Node find(K x, Node t)
    {
        while(t != null) {
            int compare = x.compareTo(t.key);
            if (compare==0) return t;
            t = (compare < 0) ? t.left : t.right;
        }
        return null;
    }

    // fixup(t) is called on each t whose subtree has been modified,
    // and returns the new root of that subtree.  These fixup(t) calls
    // are done in a bottom-up order, after a tree modification.  So,
    // fixup(t) assumes that the t.left and t.right subtrees have
    // already been "fixed up".
    //
    // For BST, fixup(t) updates t.height and t.size, and returns t.
    // In AVLTree, fixup(t) may do some rotations to restore the
    // balance condition, and it should return the new subtree root.

    Node fixup(Node t) {
        t.update();               // fix t.height and t.size
        return t;                 // subtree root unchanged
    }

    // Test BST equality (same keys and shape), ignoring modCount.
    // We use this for debugging.
    public boolean equals(Object o) {
        if (!(o instanceof BST)) return false;
        BST b = (BST) o;
        if (root==null) return b.root==null;
        return root.equals(b.root);
    }

    // Get the key at the given rank (index) in sorted order.
    // Use rank==0 for the min value, up to rank==size()-1 for max value.
    public K get(int rank) {
        Node t = root;
        while (t != null) {
            int s = size(t.left);
            if (s==rank) return t.key;
            if (s> rank) {
                t = t.left;
            } else {
                t = t.right;
                rank -= s+1;
            }
        }
        throw new IndexOutOfBoundsException(); // too big or too small
    }

    // Get the rank of k.  That is, the number of keys in the tree
    // which are strictly smaller than k.
    public int rank(K k)
    {
        int ret = 0;
        Node t = root;
        while (t != null) {
            int cmp = k.compareTo(t.key);
            if (cmp==0)
                return ret + size(t.left);
            if (cmp<0) {
                t = t.left;
            } else {
                ret += 1+size(t.left);
                t = t.right;
            }
        }
        return ret;
    }

    // Here we implement Iterable, using a simple rank-based iterator.
    public java.util.Iterator<K> iterator() {
        return new RankIterator();
    }
    class RankIterator implements java.util.Iterator<K> {
        int nextRank = 0;
        // This is a "fail-fast" iterator, like those in java.util.
        int expectedModCount = modCount;
        private void check() {
            if (expectedModCount != modCount)
                throw new java.util.ConcurrentModificationException();
        }
	public boolean hasNext() { check(); return nextRank < size(); }
	public K next() { check(); return get(nextRank++); }
        // We do not implement remove()
	public void remove() { throw new UnsupportedOperationException(); }
    }

    // testAVL() helps with grading.  It checks whether all height and
    // size fields are consistent, and whether all nodes have the balance
    // condition.  If any problem is found it returns an error String.
    // If no problem is found, it returns null.
    public final String testAVL() { return testAVL(root, "root"); }
    final String testAVL(Node t, String path)
    {
        if (t == null) return null;
        String bug = null;
        if (t.height != 1 + Math.max(height(t.left), height(t.right)))
            bug = "bad height";
        else if (t.size != 1 + size(t.left) + size(t.right))
            bug = "bad size";
        else {
            int bal = height(t.left) - height(t.right);
            if (bal >= 2 || bal <= -2)
                bug = "bad balance (" + bal + ")";
        }
        if (bug != null)
            return "bug at " + path + ": " + bug;
        String m = testAVL(t.left, path + ".left");
        if (m != null) return m;
        return testAVL(t.right, path + ".right");
    }
}
