/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jsonKVStore.json;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * Simple JSON Object representation.
 *
 * It alphabetically sorts children and properties.
 */
public class Json {

    private TreeMap<String, String> props = new TreeMap<>();
    private TreeMap<String, Json> children = new TreeMap<>();
    private int sizeInBytes;

    @Override
    public int hashCode() {
        return props.hashCode() ^ children.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (!(other instanceof Json)) {
            return false;
        }
        Json o = (Json) other;
        return props.equals(o.props) && children.equals(o.children);
    }
    
    public long sizeInBytes() {
        // TODO verify during testing, but disable otherwise
//        return sizeInBytes;
        long result = 0;
        for(String k : props.keySet()) {
            result += k.length();
            result += props.get(k).length();
        }
        for(String k : children.keySet()) {
            result += k.length();
            result += children.get(k).sizeInBytes();
        }
        if (result != sizeInBytes) {
            throw new AssertionError("Expected " + result + " got "+ sizeInBytes);
        }
        return result;
    }

    /**
     * Write the object to a builder.
     *
     * @param buf the target
     */
    public void toJson(JsonBuilder buf) {
        toJson(buf, this);
    }

    
    public Json setPropertyString(String key, String value) {
        removeProperty(key);
        sizeInBytes += key.length();
        value = JsonBuilder.encode(value);
        sizeInBytes += value.length();
        props.put(key, value);
        return this;
    }
    
    public Json setPropertyLong(String key, long value) {
        removeProperty(key);
        sizeInBytes += key.length();
        String v = "" + value;
        sizeInBytes += v.length();
        props.put(key, v);
        return this;
    }

    private Json removeProperty(String key) {
        String old = props.remove(key);
        if (old != null) {
            sizeInBytes -= key.length();
            sizeInBytes -= old.length();
        }
        return this;
    }

    /**
     * Pretty-print the object.
     *
     * @return the pretty-printed string representation
     */
    @Override
    public String toString() {
        JsonBuilder w = new JsonBuilder();
        toJson(w);
        return JsonBuilder.prettyPrint(w.toString());
    }

    private static void toJson(JsonBuilder buf, Json obj) {
        buf.object();
        for (String name : obj.props.keySet()) {
            buf.key(name).encodedValue(obj.props.get(name));
        }
        for (String name : obj.children.keySet()) {
            buf.key(name);
            toJson(buf, obj.children.get(name));
        }
        buf.endObject();
    }

    public boolean containsKey(String key) {
        return children.containsKey(key) | props.containsKey(key);
    }
    
    public boolean containsProperty(String key) {
        return props.containsKey(key);
    }

    public Set<String> getKeys() {
        HashSet<String> result = new HashSet<>();
        result.addAll(props.keySet());
        result.addAll(children.keySet());
        return result;
    }

    public String getStringProperty(String key) {
        return JsonTokenizer.decodeQuoted(props.get(key));
    }

    public Json getChild(String key) {
        return children.get(key);
    }

    public void setChild(String key, Json value) {
        sizeInBytes += key.length();
        sizeInBytes += value.sizeInBytes();
        Json old = children.put(key, value);
        if (old != null) {
            sizeInBytes -= key.length();
            sizeInBytes -= old.sizeInBytes();
        }
    }

    public Set<String> getChildKeys() {
        return children.keySet();
    }

    public Json removeChild(String key) {
        Json result = children.remove(key);
        sizeInBytes -= key.length();
        sizeInBytes -= result.sizeInBytes();
        return result;
    }

    public Set<String> getPropertyKeys() {
        return props.keySet();
    }

    public long getPropertyLong(String key) {
        return Long.parseLong(props.get(key));
    }

    public Json copy() {
        Json result = new Json();
        result.children.putAll(children);
        result.props.putAll(props);
        result.sizeInBytes = sizeInBytes;
        return result;
    }

    public boolean isEmpty() {
        return children.isEmpty() && props.isEmpty();
    }

}