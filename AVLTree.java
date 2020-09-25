// THIS CODE IS MY OWN WORK, IT WAS WRITTEN WITHOUT CONSULTING
// A TUTOR OR CODE WRITTEN BY OTHER STUDENTS - Gabriel Granados

// AVLTree.java: this should be an AVL search tree.
// For homework, you only edit this file.
// Remember to add your name to the SPCA statement above.

// An AVLTree is a BST (binary search tree) which also maintains the
// "height balance condition": for every node in the tree, its two
// child subtrees have heights within one of each other.
//
// AVLTree inherits most of its methods from BST.  The only BST method
// you really need to change is fixup().  In BST, you can see fixup(t)
// is called on every node t along the path up from a modification
// point (insertion or removal) back up to the root.  In BST the
// fixup(t) method simply calls t.update(), and returns t.
// Method t.update() recomputes t.size and t.height.
//
// But in AVLTree, fixup(t) should also do one step of the "trinode
// restructuring" (one or two rotations) necessary to restore the
// balance condition.  It returns the root of the restructured
// subtree, which may be different from t.
//
// It is up to the callers of fixup(t) to call it on the way back up
// the tree after a modification, but this is already done in the BST
// insert and remove methods, so you don't need to override those.
//
// In fixup(t), you may use rotateRight, provided below.  You'll
// probably also need rotateLeft, which you can write yourself.

public class AVLTree<K extends Comparable<K>> extends BST<K>
{
    // We inherit "root" field from BST.
    // All the public BST methods should work without modification.
    // We only need to revise this one method, to do the rebalancing.

    protected Node fixup(Node t)
    {
        t.update();             // update t.size and t.height

        int bal = height(t.left) - height(t.right);
        if (-1 <= bal && bal <= +1)
            // got lucky: balanced already, nothing to do!
            return t;

        // TODO: restore balance condition using an appropriate
        // trinode restructuring (either one or two rotations), as
        // illustrated in the slides.

        // In those illustrations, this unhappy node t is "z", t's
        // taller child is "y", and y's taller child is "x".  In case
        // of a tie, where both children of y have the same height,
        // choose x so that you only have to do one rotation.

        // return the new root of this subtree (might not be t!)

        if(height(t.right)>height(t.left)){
            Node a = t.right;
            if(height(a.right)<height(a.left)){
                t.right = rotateRight(a);
                t.update();
                return rotateLeft(t);
            } else {
                return rotateLeft(t);
            }
        } else {
            Node b = t.left;
            if(height(b.right)>height(b.left)){
                t.left = rotateLeft(b);
                t.update();
                return rotateRight(t);
            } else {
                return rotateRight(t);
            }
        }

    }

    // Feel free to ignore, delete, or modify the following.

    // This method does a rotation, lifting the left child to become
    // the new root of this subtree. The old root becomes its right
    // child.  The method also updates the size and height fields, and
    // returns the new root of the rotated subtree.
    Node rotateRight(Node k2) {
        Node k1 = k2.left;
        k2.left = k1.right;
        k2.update();
        k1.right = k2;
        k1.update();
        return k1;
    }


    Node rotateLeft(Node k2){
        Node k1 = k2.right;
        k2.right = k1.left;
        k2.update();
        k1.left = k2;
        k1.update();
        return k1;
    }

}

