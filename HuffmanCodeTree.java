import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HuffmanCodeTree {
    private TreeNode root;
    private int size;

    // Takes an array of integers as parameter.
    // Each index of the array represents
    // a character, and the value stored at
    // each index is the frequency of the
    // character. This method constructs
    // the initial HuffmanTree.
    public HuffmanCodeTree(int[] frequencies) {
        root = null;
        HuffmanPriorityQueue<TreeNode> queue = new HuffmanPriorityQueue<>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                TreeNode node = new TreeNode(i, frequencies[i]);
                queue.insert(node);
            }
        }
        TreeNode eofNode = new TreeNode(frequencies.length, 1);
        queue.insert(eofNode);

        // build the tree using frequencies
        while (queue.size() > 1) {
            TreeNode node1 = queue.extractMin();
            TreeNode node2 = queue.extractMin();
            int combineFreq = node1.getFrequency() + node2.getFrequency();
            TreeNode combineNode = new TreeNode(node1,combineFreq, node2);
            queue.insert(combineNode);
        }
        root = queue.extractMin();
    }

    // build tree by PriorityQueue
    public HuffmanCodeTree(HuffmanPriorityQueue<TreeNode> queue) {
        // build the tree using frequencies
        while (queue.size() > 1) {
            TreeNode n1 = queue.extractMin();
            TreeNode n2 = queue.extractMin();
            int combineFreq = n1.getFrequency() + n2.getFrequency();
            TreeNode combineNode = new TreeNode(n1,combineFreq, n2);
            queue.insert(combineNode);
        }
        root = queue.extractMin();
    }

    public HuffmanCodeTree(BitInputStream is) throws IOException {
        BitInputStream in = new BitInputStream(is);
        int i = in.readBits(IHuffConstants.BITS_PER_INT);
        root = rebuildTreeHelper(is);
    }

    private TreeNode rebuildTreeHelper(BitInputStream is) throws IOException {
        int nextBit = is.readBits(1);
        if (nextBit == 0) {
            TreeNode newNode = new TreeNode(-1, -1);
            newNode.setLeft(rebuildTreeHelper(is).getLeft());
            newNode.setRight(rebuildTreeHelper(is).getRight());
            return newNode;
        } else if (nextBit == 1) {
            int value = is.readBits(IHuffConstants.BITS_PER_WORD + 1);
            TreeNode newNode2 = new TreeNode(value, 1);
            return newNode2;
        } else {
            throw new IOException("");
        }
    }

    // Get the total number of nodes
    public int getSize() {
        return getSizeHelper(root);
    }
    private int getSizeHelper(TreeNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + getSizeHelper(node.getLeft()) + getSizeHelper(node.getRight());
    }

    // Use preorder to traverse the tree
    // get the path
    public void preOrderTraverse(BitOutputStream os) {
        preOrderTraverseHelper(root,os);
    }
    private void preOrderTraverseHelper(TreeNode node, BitOutputStream os) {
        if (node.isLeaf()) {
            os.writeBits(1,1);
            os.writeBits(IHuffConstants.BITS_PER_WORD + 1, node.getValue());
        } else {
            os.writeBits(1,0);
            preOrderTraverseHelper(node.getLeft(), os);
            preOrderTraverseHelper(node.getRight(), os);
        }
    }

    public Map<Integer, String> createBitCodeMap() {
        Map<Integer, String> bitCodeMap = new HashMap<>();
        getMap(root, "", bitCodeMap);
        return bitCodeMap;
    }

    private void getMap(TreeNode n, String path, Map<Integer, String> bitCodeMap) {
        if (n.isLeaf()) {
            bitCodeMap.put(n.getValue(), path);
        } else {
            getMap(n.getLeft(), path + "0", bitCodeMap);
            getMap(n.getRight(), path + "1", bitCodeMap);
        }
    }

    // calculate the number of Tree's bits
    public int getTreeBits() {
        return getTreeBitsHelper(root);
    }
    private int getTreeBitsHelper(TreeNode node) {
        if (node.isLeaf()) {
            return 1 + (1 + IHuffConstants.BITS_PER_WORD);
        } else {
            return 1 + getTreeBitsHelper(node.getLeft()) + getTreeBitsHelper(node.getRight());
        }
    }

    public void printTree() {
        printTree(root, " ");
    }
    private void printTree(TreeNode n, String spaces) {
        if (n != null) {
            printTree(n.getLeft(), spaces + " ");
            System.out.println(spaces + (n.isLeaf() ? n.getFrequency() + "; " +
                    n.getValue() : n.getValue()));

            printTree(n.getRight(), spaces + " ");
        }
    }
}
