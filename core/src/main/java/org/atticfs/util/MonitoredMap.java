/*
 * Copyright 2004 - 2012 Cardiff University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atticfs.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class Description Here...
 *
 * 
 */

public class MonitoredMap<K, V> implements Map<K, V> {

    private int timeout;
    private Map<K, TimedEntry<V>> map = new ConcurrentHashMap<K, TimedEntry<V>>();
    ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

    public MonitoredMap(int timeout) {
        this(timeout, 10 * 1000 * 60);
    }

    /**
     * default timeout of 10 seconds and sweep interval 5 minutes
     */
    public MonitoredMap() {
        this(1000 * 10);
    }

    public MonitoredMap(int timeout, int sweepInterval) {
        this.timeout = timeout;
        ses.scheduleAtFixedRate(new Monitor(), sweepInterval, sweepInterval, TimeUnit.MILLISECONDS);
    }

    public int getTimeout() {
        return timeout;
    }

    public void destroy() {
        ses.shutdownNow();
        clear();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    public V get(Object o) {
        TimedEntry<V> entry = map.get(o);
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }

    public V put(K k, V v) {
        if (k == null || v == null) {
            throw new NullPointerException("null keys or values are not allowed in a MonitoredMap");
        }
        TimedEntry<V> entry = map.put(k, new TimedEntry<V>(v));
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }

    public V remove(Object o) {
        TimedEntry<V> entry = map.remove(o);
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        Set<? extends Entry<? extends K, ? extends V>> set = map.entrySet();
        for (Entry<? extends K, ? extends V> entry : set) {
            this.map.put(entry.getKey(), new TimedEntry<V>(entry.getValue()));
        }
    }

    public void clear() {
        map.clear();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Collection<V> values() {
        Collection<TimedEntry<V>> c = map.values();
        ArrayList<V> l = new ArrayList<V>();
        for (TimedEntry<V> timedEntry : c) {
            l.add(timedEntry.getValue());
        }
        return l;
    }

    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, TimedEntry<V>>> set = map.entrySet();
        Set<Entry<K, V>> treeSet = new TreeSet<Entry<K, V>>();
        for (Entry<K, TimedEntry<V>> entry : set) {
            treeSet.add(new SimpleEntry<K, V>(entry.getKey(), entry.getValue().getValue()));
        }
        return treeSet;
    }

    private class Monitor implements Runnable {

        public void run() {
            Set<Entry<K, TimedEntry<V>>> set = map.entrySet();
            ArrayList<K> list = new ArrayList<K>();
            for (Entry<K, TimedEntry<V>> entry : set) {
                long time = entry.getValue().getCreated() + timeout;
                long now = System.currentTimeMillis();
                if (time < now) {
                    list.add(entry.getKey());
                }
            }
            for (K k : list) {
                map.remove(k);
            }
        }
    }

    private static class TimedEntry<V> {
        private long created;
        private V value;

        public TimedEntry(V value) {
            this.created = System.currentTimeMillis();
            this.value = value;
        }

        public long getCreated() {
            return created;
        }

        public V getValue() {
            return value;
        }
    }

    private static class SimpleEntry<K, V> implements Map.Entry<K, V> {

        private K key;
        private V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V v) {
            V ret = value;
            value = v;
            return ret;
        }
    }
}
