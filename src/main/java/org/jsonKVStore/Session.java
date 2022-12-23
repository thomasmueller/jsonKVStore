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
import java.util.Map.Entry;
import java.util.Set;

import org.jsonKVStore.json.Json;
import org.jsonKVStore.store.MemoryStore;

/*

escaping:
if it ends with "*" then append " "
if it ends with " " then append " "

reverse:
if it ends with " " then remove the " "

"*" -> "*"

+ "customer/c1": { "name": "x" }
"customer/c1": { "name": "x" }

+ "customer/c2": { "name": "y" }
"customer/c*": { "1": { "name": "x" }, "2": { "name": "y" } }

+ "customer/d1": { "name": "z" }
"customer/*": { 
  "c*": { "1": { "name": "x" }, "2": { "name": "y" } },
  "d1": { "name": "z" } }

 */
public class Session {

    private static final int MAX_CACHE_SIZE = 1;
    private static final int LARGE_OBJECT = 1 * 1024;
    static final String ROOT_NAME = "root";
    private static final String MULTIPLE = "*";

    final MemoryStore store;
    private final Cache<String, Json> cache = new Cache<>(MAX_CACHE_SIZE);
    private final HashMap<String, Json> changes = new HashMap<>(MAX_CACHE_SIZE);
    private long updateId;

    public Session() {
        this(new MemoryStore());
    }

    public Session(MemoryStore store) {
        this.store = store;
    }

    void checkpoint() {
        Json root = getFile(ROOT_NAME);
        putChange(ROOT_NAME + "_" + updateId, root);
        updateId++;
        flush();
        root = newJson(root);
        putChange(ROOT_NAME, root);
    }
    
    void flush() {
        for(Entry<String, Json> e : changes.entrySet()) {
            cache.put(e.getKey(), e.getValue());
            store.put(e.getKey(), e.getValue());
        }
        changes.clear();
    }

    void init() {
        Json root = newJson();
        putChange(ROOT_NAME, root);
    }

    private Json newJson(Json old) {
        Json result = old.copy();
        result.setPropertyLong("update", updateId);
        return result;
    }
    
    private Json newJson() {
        Json result = new Json();
        result.setPropertyLong("update", updateId);
        return result;
    }

    private static String getPrefixOrNull(String key, Json json) {
        for (int len = 1; len <= key.length(); len++) {
            if (json.containsKey(key.substring(0, len) + MULTIPLE)) {
                return key.substring(0, len) + MULTIPLE;
            }
        }
        return null;
//        for (int len = key.length(); len >= 1; len--) {
//            if (json.containsKey(key.substring(0, len) + MULTIPLE)) {
//                return key.substring(0, len) + MULTIPLE;
//            }
//        }
//        return null;
    }

    private static String escapeKey(String key) {
        if (key.endsWith(" ") || key.endsWith("*")) {
            return key + " ";
        }
        return key;
    }

    private static String unescapeKey(String key) {
        if (key == null) {
            return key;
        }
        if (key.endsWith(" ")) {
            return key.substring(0, key.length() - 1);
        }
        return key;
    }

    static boolean isPrefix(String k) {
        return k.endsWith(MULTIPLE);
    }
    
    private static String removePrefix(String prefix) {
        return prefix.substring(0, prefix.length() - 1);
    }

    private static String removePrefix(String key, String prefix) {
        return key.substring(prefix.length() - 1);
    }

    Json get(String key) {
        if (key == null) {
            throw new NullPointerException();
        }        
        key = escapeKey(key);
        String fileName = ROOT_NAME;
        String k = key;
        Json file = getFile(fileName);
        while (true) {
            Json result = file.getChild(k);
            if (result != null) {
                return result;
            }
//            if (file.getChildren() .containsKey(k)) {
//                if (file.containsProperty(k)) {
//                    // whole file
//                    fileName = file.getStringProperty(k);
//                    return getFile(fileName);
//                }
//                // embedded object
//                return file.getChildren().get(k);
//            }
            String prefix = getPrefixOrNull(k, file);
            if (prefix == null) {
                // not found
                return null;
            }
            if (file.containsProperty(prefix)) {
                fileName = file.getStringProperty(prefix);
                k = removePrefix(k, prefix);
                file = getFile(fileName);
                // continue with the new file
            } else {
                file = file.getChild(k);
                k = removePrefix(k, prefix);
                // continue with the embedded object
            }
        }
    }

    /**
     * Put a value.
     * Null is not supported; to remove an entry, use an empty Json object.
     * 
     * @param key the key
     * @param value the value
     */
    void put(String key, Json value) {
        if (key == null) {
            throw new NullPointerException();
        }
        if (value == null) {
            throw new NullPointerException();
        }
        key = escapeKey(key);
        // ensure the client can't modify the value later
        value = value.copy();
        put(ROOT_NAME, key, value);
    }

    private String put(String fileName, String key, Json value) {
        Json file = getFile(fileName);
        if (file.getPropertyLong("update") < updateId) {
            fileName = store.newFileName();
            file = newJson(file);
            putChange(fileName, file);
        }
        if (value.isEmpty()) {
            String prefix = getPrefixOrNull(key, file);
            if (prefix == null) {
                // not stored in a child: we remove the entry
                file.removeChild(key);
            } else {
                file.setChild(key, value);
            }
        } else {
            file.setChild(key, value);
        }
        putChange(fileName, file);
        splitOrMerge(fileName, file);
        return fileName;
    }

    private static String getLongestPrefix(Set<String> set) {
        String longestPrefix = null;
        int min = Integer.MAX_VALUE;
        for (String k : set) {
            if (k.isEmpty()) {
                // ignore
            } else if (longestPrefix == null) {
                longestPrefix = k;
                min = k.length();
            } else {
                int i = 0;
                for (; i < longestPrefix.length() && i < k.length(); i++) {
                    if (longestPrefix.charAt(i) != k.charAt(i)) {
                        min = Math.min(min, i);
                        break;
                    }
                }
            }
        }
        if (min == 0) {
            min = 1;
        }
        return longestPrefix.substring(0, min) + MULTIPLE;
    }

    private void splitOrMerge(String fileName, Json file) {
        if (file.sizeInBytes() < LARGE_OBJECT) {
            // TODO merge if really small
            return;
        }
        putChange(fileName, file);        
        // move full keys to prefixes, if possible
        Set<String> children = new HashSet<>(file.getChildKeys());
        for (String k : children) {
            // TODO actually children are never prefixes currently
            if (!isPrefix(k)) {
                String prefix = getPrefixOrNull(k, file);
                if (prefix != null) {
                    Json value = file.removeChild(k);
                    if (file.containsProperty(prefix)) {
                        // push to file
                        String childFileName = file.getStringProperty(prefix);
                        k = removePrefix(k, prefix);
                        String newChildFileName = put(childFileName, k, value);
                        if (!childFileName.equals(newChildFileName)) {
                            file.setPropertyString(prefix, newChildFileName);
                        }
                    } else {
                        // TODO we don't currently support prefix objects
                        throw new IllegalStateException();
                    }
                }
            }
        }
        if (file.sizeInBytes() < LARGE_OBJECT) {
            return;
        }
        // try to create prefixes
        String longestPrefix = getLongestPrefix(children);
        String newFileName = store.newFileName();
        file.setPropertyString(longestPrefix, newFileName);
        putChange(newFileName, newJson());
        children = new HashSet<>(file.getChildKeys());
        for (String k : children) {
            // TODO actually children are never prefixes currently
            if (!isPrefix(k)) {
                String prefix = getPrefixOrNull(k, file);
                if (prefix != null) {
                    Json value = file.removeChild(k);
                    if (file.containsProperty(prefix)) {
                        // push to file
                        String childFileName = file.getStringProperty(prefix);
                        k = removePrefix(k, prefix);
                        String newChildFileName = put(childFileName, k, value);
                        if (!childFileName.equals(newChildFileName)) {
                            file.setPropertyString(prefix, newChildFileName);
                        }                        
                    } else {
                        // TODO we don't currently support prefix objects
                        throw new IllegalStateException();
                    }
                }
            }
        }
    }
    
    private void putChange(String key, Json value) {
        changes.put(key, value);
        cache.remove(key);
    }

    private Json getFile(String key) {
        Json result = changes.get(key);
        if (result != null) {
            return result;
        }
        result = cache.get(key);
        if (result == null) {
            result = store.get(key);
            cache.put(key, result.copy());
        }
        return result;
    }
    
    String getNextKey(String largerThan) {
        return unescapeKey(getNextKey(escapeKey(largerThan), ROOT_NAME, ""));
    }
    
    private String getNextKey(String largerThan, String fileName, String prefix) {
        Json file = getFile(fileName);
        String resultFromChildren = null;
        for(String k : file.getChildKeys()) {
            String key = prefix + k;
            if (key.compareTo(largerThan) > 0) {
                resultFromChildren = key;
                break;
            }
        }
        for(String k : file.getPropertyKeys()) {
            if (!isPrefix(k)) {
                continue;
            }
            String newPrefix = prefix + removePrefix(k);
            if (resultFromChildren != null && resultFromChildren.compareTo(newPrefix) < 0) {
                return resultFromChildren;
            }
            String fileName2 = file.getStringProperty(k);
            String resultForFile = getNextKey(largerThan, fileName2, newPrefix);
            if (resultForFile != null) {
                if (resultFromChildren != null && resultFromChildren.compareTo(resultForFile) < 0) {
                    return resultFromChildren;
                } else {
                    return resultForFile;
                }
            }
        }
        return resultFromChildren;
    }
    
}
