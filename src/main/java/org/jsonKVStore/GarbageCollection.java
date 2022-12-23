package org.jsonKVStore;

import java.util.HashSet;
import java.util.List;

import org.jsonKVStore.json.Json;
import org.jsonKVStore.store.MemoryStore;

public class GarbageCollection {

    private final MemoryStore store;
    
    GarbageCollection(MemoryStore store) {
        this.store = store;
    }

    public GarbageCollectionResult run(List<String> rootKeys) {
        HashSet<String> used = mark(rootKeys);
        return sweep(used);
    }

    private HashSet<String> mark(List<String> rootKeys) {
        HashSet<String> used = new HashSet<>();
        for(String root : rootKeys) {
            mark(root, used);
        }
        return used;
    }
    
    private void mark(String root, HashSet<String> used) {
        used.add(root);
        Json value = store.get(root);
        for(String k : value.getPropertyKeys()) {
            if (Session.isPrefix(k)) {
                mark(value.getStringProperty(k), used);
            }
        }
    }

    private GarbageCollectionResult sweep(HashSet<String> used) {
        GarbageCollectionResult result = new GarbageCollectionResult();
        for (String key: new HashSet<>(store.keySet())) {
            if (!used.contains(key)) {
                store.remove(key);
                result.countRemoved++;
            } else {
                result.countKept++;
            }
        }
        return result;
    }
    
    static class GarbageCollectionResult {
        long countRemoved;
        long countKept;
        
        public String toString() {
            return "removed: " + countRemoved + " kept: " + countKept;
        }
    }

}
