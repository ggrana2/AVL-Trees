// Run some small AVLTree tests, followed by a timing test.  Usage:
//
//    java Tests
//
// You do not need to edit this file, or even read it.
// Just use it to test your code.  If you fail a test,
// you might want to search for the message below.

import java.util.Random;
import java.util.TreeSet;
import java.util.Iterator;

public class Tests
{
    static BST<Integer> makeBST(int[] keys) {
	BST<Integer> ret = new BST<Integer>();
	for (int key: keys) ret.insert(key);
	return ret;
    }
    static AVLTree<Integer> makeAVL(int[] keys) {
	AVLTree<Integer> ret = new AVLTree<Integer>();
	for (int key: keys)
	    if (key > 0)
		ret.insert(key);
	    else
		ret.remove(-key);
	return ret;
    }

    static boolean treeCompare(String test, BST<Integer> exp, BST<Integer> got)
    {
        // first check got.size(), since equals() does not test that
	if (exp.size() != got.size()) {
            System.out.printf("%s failed, size() is %d but should be %d%n",
                              test, got.size(), exp.size());
            return false;
        }
        // check for deep equality, same keys and tree shape
        if (exp.equals(got)) return true;
        // something does not match, show both trees
	System.out.println(test + " failed, we expected this tree:");
	exp.printTree();
	System.out.println("but instead we got this tree:");
	got.printTree();
	return false;
    }

    // The four basic restructurings (at root, on an insert)
    static boolean test1() {
	return treeCompare("test1",
			   makeBST(new int[] {2, 1, 3}),
			   makeAVL(new int[] {1, 2, 3}));
    }

    static boolean test2() {
	return treeCompare("test2",
			   makeBST(new int[] {2, 1, 3}),
			   makeAVL(new int[] {3, 2, 1}));
    }

    static boolean test3() {
	return treeCompare("test3",
			   makeBST(new int[] {2, 1, 3}),
			   makeAVL(new int[] {1, 3, 2}));
    }

    static boolean test4() {
	return treeCompare("test4",
			   makeBST(new int[] {2, 1, 3}),
			   makeAVL(new int[] {3, 1, 2}));
    }

    // break tie correctly? (remove, left side)
    static boolean test5() {
	return treeCompare("test5",
			   makeBST(new int[] {2, 1, 4, 3}),
			   makeAVL(new int[] {4, 2, 5, 1, 3, -5}));
    }

    // break tie correctly? (remove, right side)
    static boolean test6() {
	return treeCompare("test6",
			   makeBST(new int[] {4, 2, 5, 3}),
			   makeAVL(new int[] {2, 1, 4, 3, 5, -1}));
    }

    // Test AVLTree iterator(), return an error message or null
    static String testIter() {
        BST<Integer> T = makeAVL(new int[] {1, 2, 3});
        Iterator<Integer> it = T.iterator();
        if (it.next()!=1) return "first it.next()!=1";
        if (it.next()!=2) return "second it.next()!=2";
        if (it.next()!=3) return "third it.next()!=3";
        if (it.hasNext()) return "it.hasNext() should be false";
        Iterator<Integer> it2 = T.iterator();
        if (it2.next()!=1) return "first it2.next()!=1";
        if (!T.remove(1)) return "T.remove(1) should be true";
        try {
            it2.next();
        } catch (Exception e) {
            return null;
        }
        return "it2.next() did not throw expected exception";
    }

    static boolean test7() {
        String err = testIter();
        if (err==null) return true;
        System.out.println("test7 failed: " + err);
        return false;
    }

    static int treeSetSize, treeSetHash;

    // Compute a hash of the int sequence returned by an Iterator
    static int hash(java.util.Iterator<Integer> it) {
        int h = 0;
        while (it.hasNext())
            h = 31*h + it.next();
        return h;
    }

    static double timeTreeSet(int M, int N, int S)
    {
	System.out.print("Timing TreeSet ... ");
	Random gen = new Random(S);
	long beg = System.currentTimeMillis();
	TreeSet<Integer> T = new TreeSet<Integer>();
	for (int v=0; v<N; ++v)
	    T.add(v); // insert all keys in order!
        for (int v=N-1; v>=0; --v)
            T.remove(v); // remove all keys in reverse order
	for (int i=2*N; i<M; ++i) {
	    int v = gen.nextInt(N);
	    if (!T.contains(v))
		T.add(v);
	    else
		T.remove(v);
	}
	double secs = (System.currentTimeMillis()-beg)/1000.0;
	System.out.printf("done in %.3f seconds, final size %d%n",
			  secs, T.size());
        // save these for later comparison:
	treeSetSize = T.size();
        treeSetHash = hash(T.iterator());
	return secs;
    }

    static double timeAVLTree(int M, int N, int S)
    {
	System.out.print("Timing AVLTree ... ");
	Random gen = new Random(S);
	long beg = System.currentTimeMillis();
	AVLTree<Integer> T = new AVLTree<Integer>();
	for (int v=0; v<N; ++v)
	    T.insert(v); // insert all keys in order!
        for (int v=N-1; v>=0; --v)
            T.remove(v); // remove all keys in reverse order
	for (int i=2*N; i<M; ++i) {
	    int v = gen.nextInt(N);
	    if (T.find(v)==null)
		T.insert(v);
	    else
		T.remove(v);
	}
	double secs = (System.currentTimeMillis()-beg)/1000.0;
	System.out.printf("done in %.3f seconds, final size %d%n",
			  secs, T.size());
	// check that size and hash agree with the previous TreeSet
	if (T.size() != treeSetSize)
	    throw new RuntimeException("bug: trees should have same size!");
        if (hash(T.iterator()) != treeSetHash)
            throw new RuntimeException("bug: trees should have same Iterator hash!");
	// check the AVL tree is well formed
	String msg = T.testAVL();
	if (msg!=null)
	    throw new RuntimeException("bug: " + msg);
	return secs;
    }

    public static void main(String[] args)
    {
	System.out.println("Doing small tests ...");
	int bugs = 0;
	if (!test1()) ++bugs;
	if (!test2()) ++bugs;
	if (!test3()) ++bugs;
	if (!test4()) ++bugs;
	if (!test5()) ++bugs;
	if (!test6()) ++bugs;
        if (!test7()) ++bugs;
	if (bugs > 0) {
	    System.out.println("Failed "+bugs+" of the small tests,");
	    System.out.println("skipping the timing test.");
	    return;
	}
	System.out.println("Passed the small tests");
	System.out.println();
	int N = 40000, M=N*6, S=17;
	System.out.printf("Doing time comparison of TreeSet and AVLTree "
			  + "(%d ops on %d keys):%n", M, N);
	double secs1 = timeTreeSet(M, N, S);
	double secs2 = timeAVLTree(M, N, S);
	double ratio = secs2/secs1;
	System.out.printf("Time ratio %.2f, ", ratio);
	System.out.println(ratio <= 2.0 ? "good enough" : "slow! (over 2.0)");
    }
}
