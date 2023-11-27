package it.unipi.mircv;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {
    private final int capacity;
    private final LinkedHashMap<K, V> cache;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        // Setting access order to true ensures the LinkedHashMap maintains
        // the order based on the access of elements (least recently accessed to most recently accessed)
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                // Controls the removal of the eldest entry when capacity is reached
                return size() > capacity;
            }
        };
    }
    public void put(K key, V value) {
        cache.put(key, value);
    }
    public V get(K key) {
        return cache.getOrDefault(key, null);
    }
    @Override
    public String toString(){
        StringBuilder string = new StringBuilder();
        for (Map.Entry<K, V> entry : cache.entrySet()) {
            string.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return string.toString();
    }
}
