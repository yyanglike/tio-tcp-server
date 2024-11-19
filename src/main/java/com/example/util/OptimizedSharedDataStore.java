package com.example.util;

import net.openhft.chronicle.map.ChronicleMap;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

public class OptimizedSharedDataStore {

    // 单例模式
    private static final OptimizedSharedDataStore INSTANCE = new OptimizedSharedDataStore();

    private final ConcurrentHashMap<String, ChronicleMap<?, ?>> mapCache = new ConcurrentHashMap<>();
    private final StampedLock lock = new StampedLock();

    private OptimizedSharedDataStore() {
    }

    public static OptimizedSharedDataStore getInstance() {
        return INSTANCE;
    }

    /**
     * 配置 ChronicleMap 的参数
     */
    public static class MapConfig {
        private long entries = 1000;
        private int averageValueSize = 100;

        public MapConfig setEntries(long entries) {
            this.entries = entries;
            return this;
        }

        public MapConfig setAverageValueSize(int averageValueSize) {
            this.averageValueSize = averageValueSize;
            return this;
        }

        public long getEntries() {
            return entries;
        }

        public int getAverageValueSize() {
            return averageValueSize;
        }
    }

    /**
     * 获取或创建 ChronicleMap
     */
    private <K, V> ChronicleMap<K, V> createOrRecoverMap(
            String mapName, File file, MapConfig config, Class<K> keyClass, Class<V> valueClass) throws IOException {
        if (file.exists()) {
            // 如果文件已存在，尝试恢复
            return ChronicleMap
                    .of(keyClass, valueClass)
                    .name(mapName)
                    .entries(config.getEntries())
                    .averageKeySize(30)
                    .averageValueSize(config.getAverageValueSize())
                    .recoverPersistedTo(file, false);
        } else {
            // 如果文件不存在，创建新的持久化文件
            return ChronicleMap
                    .of(keyClass, valueClass)
                    .name(mapName)
                    .entries(config.getEntries())
                    .averageKeySize(30)
                    .averageValueSize(config.getAverageValueSize())
                    .createPersistedTo(file);
        }
    }

    /**
     * 获取或创建普通的 Map
     */
    @SuppressWarnings("unchecked")
    public ChronicleMap<String, String> getMap(String mapName, MapConfig config) throws IOException {
        return (ChronicleMap<String, String>) mapCache.computeIfAbsent(mapName, name -> {
            try {
                File mapFile = new File(name + ".dat");
                return createOrRecoverMap(name, mapFile, config, String.class, String.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create ChronicleMap for " + name, e);
            }
        });
    }

    /**
     * 获取或创建 Multimap
     */
    @SuppressWarnings("unchecked")
    public ChronicleMap<String, Set<String>> getMultimap(String mapName, MapConfig config) throws IOException {
        return (ChronicleMap<String, Set<String>>) mapCache.computeIfAbsent(mapName, name -> {
            try {
                File mapFile = new File(name + "_multimap.dat");
                return ChronicleMap
                        .of(String.class, (Class<Set<String>>) (Class<?>) Set.class)
                        .name(name)
                        .entries(config.getEntries())
                        .averageKeySize(30)
                        .averageValueSize(128) // 配置值大小
                        .createPersistedTo(mapFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create ChronicleMap for " + name, e);
            }
        });
    }

    /**
     * 添加数据到普通的 ChronicleMap
     */
    public void putToMap(String mapName, String key, String value, MapConfig config) throws IOException {
        long stamp = lock.tryOptimisticRead();
        ChronicleMap<String, String> map = getMap(mapName, config);

        // 检查乐观锁是否失效，失效则获取写锁
        if (!lock.validate(stamp)) {
            stamp = lock.writeLock();
            try {
                map.put(key, value);
            } finally {
                lock.unlockWrite(stamp);
            }
        } else {
            map.put(key, value);
        }
    }

    /**
     * 添加数据到 Multimap
     */
    public void putToMultimap(String mapName, String key, String value, MapConfig config) throws IOException {
        long stamp = lock.writeLock();
        try {
            ChronicleMap<String, Set<String>> multimap = getMultimap(mapName, config);
            Set<String> set = multimap.get(key);
            if (set == null) {
                set = new HashSet<>();
            }
            set.add(value);
            multimap.put(key, set);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 批量添加数据到普通 Map
     */
    public void putMultipleToMap(String mapName, Map<String, String> entries, MapConfig config) throws IOException {
        long stamp = lock.writeLock();
        try {
            ChronicleMap<String, String> map = getMap(mapName, config);
            map.putAll(entries);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 获取普通 Map 的值
     */
    public String getFromMap(String mapName, String key, MapConfig config) throws IOException {
        long stamp = lock.tryOptimisticRead();
        ChronicleMap<String, String> map = getMap(mapName, config);

        // 检查乐观锁是否失效
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                return map.get(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return map.get(key);
    }

    /**
     * 获取 Multimap 的值集合
     */
    public Set<String> getFromMultimap(String mapName, String key, MapConfig config) throws IOException {
        long stamp = lock.tryOptimisticRead();
        ChronicleMap<String, Set<String>> multimap = getMultimap(mapName, config);

        // 检查乐观锁是否失效
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                return multimap.get(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return multimap.get(key);
    }
}