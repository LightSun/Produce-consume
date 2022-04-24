package com.heaven7.java.pc.utils;

/**
 * the pair
 * @param <K> the key type
 * @param <V> the value type
 * @since 1.0.5
 */
public class Pair<K, V> {
    public final K key;
    public final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}