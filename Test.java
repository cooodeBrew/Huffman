import java.util.Map;

public class Test {
    public static void main(String[] args) {
        test();
    }

    public static void test() {
        HuffmanPriorityQueue<TreeNode> pq = new HuffmanPriorityQueue<>();
        pq.insert(new TreeNode(100, 3));
        pq.insert(new TreeNode(88, 10));
        pq.insert(new TreeNode(200, 5));
        HuffmanCodeTree tree = new HuffmanCodeTree(pq);
        tree.printTree();
        System.out.println("-------");
        Map<Integer, String> map = tree.createBitCodeMap();
        System.out.println(map.keySet());
        for (int key : map.keySet()) {
            System.out.println("get key of " + key + ": "+ map.get(key));
        }
    }
}
