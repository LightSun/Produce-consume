package com.heaven7.java.pc.utils;

import java.util.Map;

/**
 * the pair
 * @param <K> the key type
 * @param <V> the value type
 * @since 1.0.5
 */
public class Pair<K, V> implements Map.Entry<K, V>{

    public K key;
    public V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V val) {
        V old = value;
        this.value = val;
        return old;
    }
}