package com.congueror.yesbot.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class ListMap<K, V> extends HashMap<K, List<V>> {

    public List<V> addEntry(K key, V value) {
        var list = this.get(key);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(value);
        return put(key, list);
    }

    public boolean removeEntry(K key, V value) {
        var list = this.get(key);
        if (list == null) {
            return false;
        }
        return list.remove(value);
    }

    public Stream<V> stream(K key) {
        var list = this.get(key);
        if (list == null)
            list = new ArrayList<>();
        return list.stream();
    }
}
