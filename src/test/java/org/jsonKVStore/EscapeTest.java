package org.jsonKVStore;

import static org.junit.Assert.assertEquals;

import org.jsonKVStore.json.Json;
import org.junit.Test;

public class EscapeTest {

    @Test
    public void escape() {
        Session session = new Session();
        session.init();
        session.put(" ", new Json().setPropertyString("x", " "));
        session.put("*", new Json().setPropertyString("x", "*"));
        session.put("a", new Json().setPropertyString("x", "*"));
        assertEquals("*", session.get("*").getStringProperty("x"));
        assertEquals(" ", session.getNextKey(""));
        assertEquals("*", session.getNextKey(" "));
        assertEquals("a", session.getNextKey("*"));
    }

}
