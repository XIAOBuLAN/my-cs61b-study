package bstmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private static class BSTNode<K, V>{
        K key;
        V value;
        BSTNode<K, V> left;
        BSTNode<K, V> right;

        private BSTNode(K key, V value){
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    private BSTNode<K, V> root;
    private int size;

    public BSTMap() {
        root = null;
        size = 0;
    }

    public BSTMap(K key, V value) {
        root = new BSTNode<K, V>(key, value);
        size++;
    }

    private void printInOrder(BSTNode<K,V> node) {
        if (node == null){
            return;
        }
        printInOrder(node.left);
        System.out.print(node.key + " ");
        printInOrder(node.right);
    }

    public void printInOrder(){
        printInOrder(root);
    }


    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    private boolean containsKey(K key, BSTNode<K, V> node){
        if (node == null) {
            return false;
        }

        int compare = key.compareTo(node.key);
        if (compare == 0) {
            return true;
        }
        if (compare < 0) {
            return containsKey(key, node.left);
        }
        if (compare > 0) {
            return containsKey(key, node.right);
        }
        return false;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(key, root);
    }

    private V get(K key, BSTNode<K, V> node){
        if (node == null) {
            return null;
        }

        int compare = key.compareTo(node.key);
        if (compare < 0) {
            return get(key, node.left);
        }
        if (compare > 0) {
            return get(key, node.right);
        }
        return node.value;
    }

    @Override
    public V get(K key) {
        return get(key, root);
    }

    @Override
    public int size() {
        return size;
    }

    private BSTNode<K, V> put(K key, V value, BSTNode<K,V> node){
        if (node == null) {
            size++;
            return new BSTNode<>(key, value);
        }
        int compare = key.compareTo(node.key);
        if (compare < 0) {
            node.left = put(key, value, node.left);
        } else if (compare > 0) {
            node.right = put(key, value, node.right);
        } else {
            node.value = value;
        }
        return node;
    }

    @Override
    public void put(K key, V value) {
        root = put(key, value, root);
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        collectKeys(root, keys);
        return keys;
    }

    private void collectKeys(BSTNode<K, V> node, Set<K> keys) {
        if (node == null) {
            return;
        }
        collectKeys(node.left, keys);
        keys.add(node.key);
        collectKeys(node.right, keys);
    }

    private BSTNode<K, V> remove(K key, BSTNode<K, V> node) {
        if (node == null) {
            return null;
        }
        int compare = key.compareTo(node.key);
        if (compare < 0) {
            node.left = remove(key, node.left);
        } else if (compare > 0) {
            node.right = remove(key, node.right);
        } else {
            // 找到目标节点，处理三种情况
            if (node.left == null) {
                size--;
                return node.right;
            }
            if (node.right == null) {
                size--;
                return node.left;
            }
            // 双子节点：用右子树最小节点替换，再删除该最小节点
            BSTNode<K, V> minNode = node.right;
            while (minNode.left != null) {
                minNode = minNode.left;
            }
            node.key = minNode.key;
            node.value = minNode.value;
            node.right = remove(minNode.key, node.right);
        }
        return node;
    }

    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        V value = get(key);
        root = remove(key, root);
        return value;
    }

    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        }
        V current = get(key);
        if (current == null ? value != null : !current.equals(value)) {
            return null;
        }
        root = remove(key, root);
        return current;
    }

    @Override
    public Iterator<K> iterator() {
        List<K> keys = new ArrayList<>();
        collectToList(root, keys);
        return keys.iterator();
    }

    private void collectToList(BSTNode<K, V> node, List<K> keys) {
        if (node == null) {
            return;
        }
        collectToList(node.left, keys);
        keys.add(node.key);
        collectToList(node.right, keys);
    }
}
