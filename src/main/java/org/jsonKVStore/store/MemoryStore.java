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
package org.jsonKVStore.store;

import java.util.HashMap;
import java.util.Set;

import org.jsonKVStore.json.Json;

public class MemoryStore {

    private final HashMap<String, Json> map = new HashMap<>();
    private long nextFileName;
    
    public Json get(String key) {
        if (!map.containsKey(key)) {
            throw new IllegalStateException("Not found: " + key);
        }
        return map.get(key);
    }
    
    public void put(String key, Json value) {
        map.put(key, value);
    }
    
    public String toString() {
        return "files: " + map.size();
//        TreeMap<String, Json> map = new TreeMap<>(store);
//        return map.toString();
    }
    
    public String newFileName() {
        return "f" + nextFileName++;
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public void remove(String key) {
        map.remove(key);
    }
    
}
