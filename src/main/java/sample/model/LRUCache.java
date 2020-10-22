package sample.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Implements a least recently used cache with a hashmap and queue
 * @param <K> The key of the LRU cache
 * @param <V> The values to be stored in the cache
 */
public class LRUCache<K, V> implements Iterable<K>, Iterator<K> {
    private int capacity;
    private Function<V, Boolean> checkValid;
    private BiConsumer<K, V> deleteFunc;

    private final Queue<K> keyQueue;
    private final HashMap<K, V> hashMap;

    /**
     * Constructs an LRU cache with the specified capacity
     * @param capacity The capacity of the LRU cache
     */
    public LRUCache(int capacity) {
        this.capacity = capacity;

        keyQueue = new LinkedList<>();
        hashMap = new HashMap<>();
    }

    /**
     * Sets the a value in the LRU cache
     * @param key Key to associate value with
     * @param value Value to be set
     */
    public void put(K key, V value) {
        keyQueue.add(key);
        hashMap.put(key, value);

        if (hashMap.size() > capacity && capacity != -1) {
            K keyToDelete = keyQueue.poll();

            if (checkValid != null) {
                while (hashMap.get(keyToDelete) == null || !checkValid.apply(hashMap.get(keyToDelete))) {
                    hashMap.remove(keyToDelete);
                    keyToDelete = keyQueue.poll();
                }
            }

            if (deleteFunc != null)
                deleteFunc.accept(keyToDelete, hashMap.get(keyToDelete));

            hashMap.remove(keyToDelete);
        }
    }

    /**
     * Removes the a value from the LRU cache
     * O(N) removal by the way
     * @param key Key to associated with the value
     */
    public void remove(K key) {
        //keyQueue.remove(key);
        hashMap.remove(key);
    }

    /**
     * Gets a value associated with a key
     * @param key The key that the value is associated with
     * @return Returns the value
     */
    public V get(K key) {
        return hashMap.get(key);
    }

    /**
     * Size of the LRU cache
     * @return Returns the size of the LRU cache
     */
    public int size() {
        return hashMap.size();
    }

    /**
     * Sets the function that should run when a value is deleted
     * @param deleteFunc The function to run
     */
    public void setDeleteFunc(BiConsumer<K, V> deleteFunc) {
        this.deleteFunc = deleteFunc;
    }

    /**
     * Checks if a given value is valid in the LRU cache
     * @param checkValid The function to run
     */
    public void setCheckValid(Function<V, Boolean> checkValid) {
        this.checkValid = checkValid;
    }

    /**
     * Sets the capacity of the LRU cache
     * @param capacity Capacity of the LRU cache
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public Iterator<K> iterator() {
        return hashMap.keySet().iterator();
    }

    @Override
    public boolean hasNext() {
        return hashMap.keySet().iterator().hasNext();
    }

    @Override
    public K next() {
        return hashMap.keySet().iterator().next();
    }
}
