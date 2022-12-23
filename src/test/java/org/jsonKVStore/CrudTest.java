package org.jsonKVStore;

import static org.junit.Assert.assertEquals;

import org.jsonKVStore.json.Json;
import org.jsonKVStore.store.MemoryStore;
import org.junit.Test;

public class CrudTest {

    @Test
    public void test() {
        MemoryStore store = new MemoryStore();
        Session session = new Session(store);
        session.init();
        Json value = new Json();
        value.setPropertyString("data", "world");
        String key = "hello";
        session.put(key, value);
        session.flush();
        
        session = new Session(store);
        assertEquals(value, session.get(key));
        value.setPropertyString("data", "world!");        
        session.put(key, value);
        session.flush();
        
        session = new Session(store);
        assertEquals(value, session.get(key));
        session.put(key, new Json());
        session.flush();        
        
        session = new Session(store);
        assertEquals(null, session.get(key));
    }
}
