/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jsonKVStore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jsonKVStore.json.Json;

public class BlobStore {

    private final HashMap<String, Json> store = new HashMap<>();
    private long nextFileName;
    
    public Json get(String key) {
        if (!store.containsKey(key)) {
            throw new IllegalStateException("Not found: " + key);
        }
        return store.get(key);
    }
    
    public void put(String key, Json value) {
        store.put(key, value);
    }
    
    public String toString() {
        return "files: " + store.size();
//        TreeMap<String, Json> map = new TreeMap<>(store);
//        return map.toString();
    }
    
    public String newFileName() {
        return "f" + nextFileName++;
    }
    
    public GarbageCollectionResult garbageCollection(List<String> rootKeys) {
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
        Json value = get(root);
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
