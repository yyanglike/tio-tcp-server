package com.example.util;

import java.io.IOException;

public class TestSharedMenory {
    public static void main(String[] args) throws IOException {
        OptimizedSharedDataStore store = OptimizedSharedDataStore.getInstance();

        // 定义配置
        OptimizedSharedDataStore.MapConfig mapConfig = new OptimizedSharedDataStore.MapConfig()
                .setEntries(5000)
                .setAverageValueSize(200);

        // 操作普通 Map
        String mapName = "optimizedSharedMap";
        store.putToMap(mapName, "key1", "value1", mapConfig);
        System.out.println("普通 Map - key1 -> " + store.getFromMap(mapName, "key1", mapConfig));

        // 操作 Multimap
        String multimapName = "optimizedSharedMultimap";
        store.putToMultimap(multimapName, "key1", "value1", mapConfig);
        store.putToMultimap(multimapName, "key1", "value2", mapConfig);
        System.out.println("Multimap - key1 -> " + store.getFromMultimap(multimapName, "key1", mapConfig));


    }    
}
