package org.jsonKVStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jsonKVStore.json.Json;

public class Test {

    public static void main(String... args) {
        Test test = new Test();
        test.runTest(1_000);
//        new Session().runTest(10_000);
//        new Session().runTest(100_000);
//        new Session().runTest(1_000_000);
    }

    public void runTest(int count) {
        System.out.println("--- init ");
        Session session = new Session();
        session.init();
        HashMap<String, Json> verify = new HashMap<>();
//        Profiler prof = new Profiler().startCollecting();
        long time = System.nanoTime();
        for (int i = 0; i < count; i++) {
            Json value = new Json();
            value.setPropertyString("name", "n" + i);
            String key = "/customer/c" + i;
            verify.put(key, value);
            session.put(key, value);
            if (i % 10 == 0) {
                session.checkpoint();
            }
        }
        time = System.nanoTime() - time;
        System.out.println((time / count) + " ns/key put");
        System.out.println(session.store);
        System.out.println(session.store.garbageCollection(List.of(Session.ROOT_NAME)));
//        System.out.println(prof.getTop(10));
        System.out.println(session.store);
        time = System.nanoTime();
        for (Entry<String, Json> e : verify.entrySet()) {
            Json v2 = session.get(e.getKey());
            Json v = e.getValue();
            if (v2 != v) {
                throw new AssertionError("" + session.get(e.getKey()));
            }
        }
        time = System.nanoTime() - time;
        System.out.println((time / count) + " ns/key get");
        int resultCount = 0;
        for (String x = "";;) {
            String next = session.getNextKey(x);
            if (next == null) {
                break;
            }
            assertTrue(next.compareTo(x) > 0);
            resultCount++;
            x = next;
        }
        assertEquals(count, resultCount);
    }
    
}
