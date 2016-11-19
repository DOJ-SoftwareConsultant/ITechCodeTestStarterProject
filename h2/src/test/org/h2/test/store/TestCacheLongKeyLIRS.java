/*
 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import org.h2.mvstore.cache.CacheLongKeyLIRS;
import org.h2.test.TestBase;
import org.h2.util.New;

/**
 * Tests the cache algorithm.
 */
public class TestCacheLongKeyLIRS extends TestBase {

    /**
     * Run just this test.
     *
     * @param a ignored
     */
    public static void main(String... a) throws Exception {
        TestBase.createCaller().init().test();
    }

    @Override
    public void test() throws Exception {
        testCache();
    }

    private void testCache() {
        testRandomSmallCache();
        testEdgeCases();
        testSize();
        testClear();
        testGetPutPeekRemove();
        testPruneStack();
        testLimitHot();
        testLimitNonResident();
        testScanResistance();
        testRandomOperations();
    }

    private static void testRandomSmallCache() {
        Random r = new Random(1);
        for (int i = 0; i < 10000; i++) {
            int j = 0;
            StringBuilder buff = new StringBuilder();
            CacheLongKeyLIRS<Integer> test = createCache(1 + r.nextInt(10));
            for (; j < 30; j++) {
                int key = r.nextInt(5);
                switch (r.nextInt(3)) {
                case 0:
                    int memory = r.nextInt(5) + 1;
                    buff.append("add ").append(key).append(' ').
                            append(memory).append('\n');
                    test.put(key, j, memory);
                    break;
                case 1:
                    buff.append("remove ").append(key).append('\n');
                    test.remove(key);
                    break;
                case 2:
                    buff.append("get ").append(key).append('\n');
                    test.get(key);
                }
            }
        }
    }

    private void testEdgeCases() {
        CacheLongKeyLIRS<Integer> test = createCache(1);
        test.put(1, 10, 100);
        assertEquals(10, test.get(1).intValue());
        try {
            test.put(1,  null, 100);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            test.setMaxMemory(0);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            test.setAverageMemory(0);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private void testSize() {
        verifyMapSize(7, 16);
        verifyMapSize(13, 32);
        verifyMapSize(25, 64);
        verifyMapSize(49, 128);
        verifyMapSize(97, 256);
        verifyMapSize(193, 512);
        verifyMapSize(385, 1024);
        verifyMapSize(769, 2048);

        CacheLongKeyLIRS<Integer> test;
        test = createCache(3, 10);
        test.put(0, 0, 9);
        test.put(1, 10, 9);
        test.put(2, 20, 9);
        test.put(3, 30, 9);
        test.put(4, 40, 9);

        test = createCache(1, 1);
        test.put(1, 10);
        test.put(0, 0);
        test.get(0);

        test = createCache(1000);
        for (int j = 0; j < 2000; j++) {
            test.put(j, j);
        }
        // for a cache of size 1000,
        // there are 62 cold entries (about 6.25%).
//        assertEquals(62, test.size() - test.sizeHot());
        // at most as many non-resident elements
        // as there are entries in the stack
//        assertEquals(968, test.sizeNonResident());
    }

    private void verifyMapSize(int elements, int expectedMapSize) {
        CacheLongKeyLIRS<Integer> test;
        test = createCache(elements - 1);
        assertTrue(test.sizeMapArray() < expectedMapSize);
        test = createCache(elements);
        assertEquals(expectedMapSize, test.sizeMapArray());
        test = createCache(elements * 100, 100);
        assertEquals(expectedMapSize, test.sizeMapArray());
    }

    private void testGetPutPeekRemove() {
        CacheLongKeyLIRS<Integer> test = createCache(4);
        test.put(1,  10);
        test.put(2,  20);
        test.put(3,  30);
        assertNull(test.peek(4));
        assertNull(test.get(4));
        test.put(4,  40);
        verify(test, "mem: 4 stack: 4 3 2 1 cold: non-resident:");
        // move middle to front
        assertEquals(30, test.get(3).intValue());
        assertEquals(20, test.get(2).intValue());
        assertEquals(20, test.peek(2).intValue());
        // already on (an optimization)
        assertEquals(20, test.get(2).intValue());
        assertEquals(10, test.peek(1).intValue());
        assertEquals(10, test.get(1).intValue());
        verify(test, "mem: 4 stack: 1 2 3 4 cold: non-resident:");
        test.put(3,  30);
        verify(test, "mem: 4 stack: 3 1 2 4 cold: non-resident:");
        // 5 is cold; will make 4 non-resident
        test.put(5,  50);
        verify(test, "mem: 4 stack: 5 3 1 2 cold: 5 non-resident: 4");
        assertEquals(1, test.getMemory(1));
        assertEquals(1, test.getMemory(5));
        assertEquals(0, test.getMemory(4));
        assertEquals(0, test.getMemory(100));
        assertNull(test.peek(4));
        assertNull(test.get(4));
        assertEquals(10, test.get(1).intValue());
        assertEquals(20, test.get(2).intValue());
        assertEquals(30, test.get(3).intValue());
        verify(test, "mem: 4 stack: 3 2 1 cold: 5 non-resident: 4");
        assertEquals(50, test.get(5).intValue());
        verify(test, "mem: 4 stack: 5 3 2 1 cold: 5 non-resident: 4");
        assertEquals(50, test.get(5).intValue());
        verify(test, "mem: 4 stack: 5 3 2 cold: 1 non-resident: 4");

        // remove
        assertEquals(50, test.remove(5).intValue());
        assertNull(test.remove(5));
        verify(test, "mem: 3 stack: 3 2 1 cold: non-resident: 4");
        assertNull(test.remove(4));
        verify(test, "mem: 3 stack: 3 2 1 cold: non-resident:");
        assertNull(test.remove(4));
        verify(test, "mem: 3 stack: 3 2 1 cold: non-resident:");
        test.put(4,  40);
        test.put(5,  50);
        verify(test, "mem: 4 stack: 5 4 3 2 cold: 5 non-resident: 1");
        test.get(5);
        test.get(2);
        test.get(3);
        test.get(4);
        verify(test, "mem: 4 stack: 4 3 2 5 cold: 2 non-resident: 1");
        assertEquals(50, test.remove(5).intValue());
        verify(test, "mem: 3 stack: 4 3 2 cold: non-resident: 1");
        assertEquals(20, test.remove(2).intValue());
        assertFalse(test.containsKey(1));
        assertNull(test.remove(1));
        assertFalse(test.containsKey(1));
        verify(test, "mem: 2 stack: 4 3 cold: non-resident:");
        test.put(1,  10);
        test.put(2,  20);
        verify(test, "mem: 4 stack: 2 1 4 3 cold: non-resident:");
        test.get(1);
        test.get(3);
        test.get(4);
        verify(test, "mem: 4 stack: 4 3 1 2 cold: non-resident:");
        assertEquals(10, test.remove(1).intValue());
        verify(test, "mem: 3 stack: 4 3 2 cold: non-resident:");
        test.remove(2);
        test.remove(3);
        test.remove(4);

        // test clear
        test.clear();
        verify(test, "mem: 0 stack: cold: non-resident:");

        // strange situation where there is only a non-resident entry
        test.put(1, 10);
        test.put(2, 20);
        test.put(3, 30);
        test.put(4, 40);
        test.put(5, 50);
        assertTrue(test.containsValue(50));
        verify(test, "mem: 4 stack: 5 4 3 2 cold: 5 non-resident: 1");
        test.put(1, 10);
        verify(test, "mem: 4 stack: 1 5 4 3 2 cold: 1 non-resident: 5");
        assertFalse(test.containsValue(50));
        test.remove(2);
        test.remove(3);
        test.remove(4);
        verify(test, "mem: 1 stack: 1 cold: non-resident: 5");
        assertTrue(test.containsKey(1));
        test.remove(1);
        assertFalse(test.containsKey(1));
        verify(test, "mem: 0 stack: cold: non-resident: 5");
        assertFalse(test.containsKey(5));
        assertTrue(test.isEmpty());

        // verify that converting a hot to cold entry will prune the stack
        test.clear();
        test.put(1, 10);
        test.put(2, 20);
        test.put(3, 30);
        test.put(4, 40);
        test.put(5, 50);
        test.get(4);
        test.get(3);
        verify(test, "mem: 4 stack: 3 4 5 2 cold: 5 non-resident: 1");
        test.put(6, 60);
        verify(test, "mem: 4 stack: 6 3 4 5 2 cold: 6 non-resident: 5 1");
        // this will prune the stack (remove entry 5 as entry 2 becomes cold)
        test.get(6);
        verify(test, "mem: 4 stack: 6 3 4 cold: 2 non-resident: 5 1");
    }

    private void testPruneStack() {
        CacheLongKeyLIRS<Integer> test = createCache(5);
        for (int i = 0; i < 7; i++) {
            test.put(i, i * 10);
        }
        verify(test, "mem: 5 stack: 6 5 4 3 2 1 cold: 6 non-resident: 5 0");
        test.get(4);
        test.get(3);
        test.get(2);
        verify(test, "mem: 5 stack: 2 3 4 6 5 1 cold: 6 non-resident: 5 0");
        // this call needs to prune the stack
        test.remove(1);
        verify(test, "mem: 4 stack: 2 3 4 6 cold: non-resident: 5 0");
        test.put(0,  0);
        test.put(1,  10);
        // the the stack was not pruned, the following will fail
        verify(test, "mem: 5 stack: 1 0 2 3 4 cold: 1 non-resident: 6 5");
    }

    private void testClear() {
        CacheLongKeyLIRS<Integer> test = createCache(40, 10);
        for (int i = 0; i < 5; i++) {
            test.put(i, 10 * i, 9);
        }
        verify(test, "mem: 36 stack: 4 3 2 1 cold: 4 non-resident: 0");
        for (Entry<Long, Integer> e : test.entrySet()) {
            assertTrue(e.getKey() >= 1 && e.getKey() <= 4);
            assertTrue(e.getValue() >= 10 && e.getValue() <= 40);
        }
        for (int x : test.values()) {
            assertTrue(x >= 10 && x <= 40);
        }
        for (long x : test.keySet()) {
            assertTrue(x >= 1 && x <= 4);
        }
        assertEquals(40,  test.getMaxMemory());
        assertEquals(10, test.getAverageMemory());
        assertEquals(36,  test.getUsedMemory());
        assertEquals(4, test.size());
        assertEquals(3,  test.sizeHot());
        assertEquals(1,  test.sizeNonResident());
        assertFalse(test.isEmpty());

        // changing the limit is not supposed to modify the map
        test.setMaxMemory(10);
        assertEquals(10, test.getMaxMemory());
        test.setMaxMemory(40);
        test.setAverageMemory(1);
        assertEquals(1, test.getAverageMemory());
        test.setAverageMemory(10);
        verify(test, "mem: 36 stack: 4 3 2 1 cold: 4 non-resident: 0");

        // putAll uses the average memory
        test.putAll(test.getMap());
        verify(test, "mem: 40 stack: 4 3 2 1 cold: non-resident: 0");

        test.clear();
        verify(test, "mem: 0 stack: cold: non-resident:");

        assertEquals(40,  test.getMaxMemory());
        assertEquals(10, test.getAverageMemory());
        assertEquals(0,  test.getUsedMemory());
        assertEquals(0, test.size());
        assertEquals(0,  test.sizeHot());
        assertEquals(0,  test.sizeNonResident());
        assertTrue(test.isEmpty());
    }

    private void testLimitHot() {
        CacheLongKeyLIRS<Integer> test = createCache(100);
        for (int i = 0; i < 300; i++) {
            test.put(i, 10 * i);
        }
        assertEquals(100, test.size());
        assertEquals(99, test.sizeNonResident());
        assertEquals(93, test.sizeHot());
    }

    private void testLimitNonResident() {
        CacheLongKeyLIRS<Integer> test = createCache(4);
        for (int i = 0; i < 20; i++) {
            test.put(i, 10 * i);
        }
        verify(test, "mem: 4 stack: 19 18 17 16 3 2 1 " +
                "cold: 19 non-resident: 18 17 16");
    }

    private void testScanResistance() {
        boolean log = false;
        int size = 20;
        // cache size 11 (10 hot, 1 cold)
        CacheLongKeyLIRS<Integer> test = createCache(size / 2 + 1);
        // init the cache with some dummy entries
        for (int i = 0; i < size; i++) {
            test.put(-i, -i * 10);
        }
        verify(test, null);
        // init with 0..9, ensure those are hot entries
        for (int i = 0; i < size / 2; i++) {
            test.put(i, i * 10);
            test.get(i);
            if (log) {
                System.out.println("get " + i + " -> " + test);
            }
        }
        verify(test, null);
        // read 0..9, add 10..19 (cold)
        for (int i = 0; i < size; i++) {
            Integer x = test.get(i);
            Integer y = test.peek(i);
            if (i < size / 2) {
                assertTrue("i: " + i, x != null);
                assertTrue("i: " + i, y != null);
                assertEquals(i * 10, x.intValue());
                assertEquals(i * 10, y.intValue());
            } else {
                assertNull(x);
                assertNull(y);
                test.put(i, i * 10);
                // peek should have no effect
                assertEquals(i * 10, test.peek(i).intValue());
            }
            if (log) {
                System.out.println("get " + i + " -> " + test);
            }
            verify(test, null);
        }
        // ensure 0..9 are hot, 10..18 are not resident, 19 is cold
        for (int i = 0; i < size; i++) {
            Integer x = test.get(i);
            if (i < size / 2 || i == size - 1) {
                assertTrue("i: " + i, x != null);
                assertEquals(i * 10, x.intValue());
            } else {
                assertNull(x);
            }
            verify(test, null);
        }
    }

    private void testRandomOperations() {
        boolean log = false;
        int size = 10;
        Random r = new Random(1);
        for (int j = 0; j < 100; j++) {
            CacheLongKeyLIRS<Integer> test = createCache(size / 2);
            HashMap<Integer, Integer> good = New.hashMap();
            for (int i = 0; i < 10000; i++) {
                int key = r.nextInt(size);
                int value = r.nextInt();
                switch (r.nextInt(3)) {
                case 0:
                    if (log) {
                        System.out.println(i + " put " + key + " " + value);
                    }
                    good.put(key, value);
                    test.put(key, value);
                    break;
                case 1:
                    if (log) {
                        System.out.println(i + " get " + key);
                    }
                    Integer a = good.get(key);
                    Integer b = test.get(key);
                    if (a == null) {
                        assertNull(b);
                    } else if (b != null) {
                        assertEquals(a, b);
                    }
                    break;
                case 2:
                    if (log) {
                        System.out.println(i + " remove " + key);
                    }
                    good.remove(key);
                    test.remove(key);
                    break;
                }
                if (log) {
                    System.out.println(" -> " + toString(test));
                }
            }
            verify(test, null);
        }
    }

    private static <K, V> String toString(CacheLongKeyLIRS<V> cache) {
        StringBuilder buff = new StringBuilder();
        buff.append("mem: " + cache.getUsedMemory());
        buff.append(" stack:");
        for (long k : cache.keys(false,  false)) {
            buff.append(' ').append(k);
        }
        buff.append(" cold:");
        for (long k : cache.keys(true,  false)) {
            buff.append(' ').append(k);
        }
        buff.append(" non-resident:");
        for (long k : cache.keys(true,  true)) {
            buff.append(' ').append(k);
        }
        return buff.toString();
    }

    private <K, V> void verify(CacheLongKeyLIRS<V> cache, String expected) {
        if (expected != null) {
            String got = toString(cache);
            assertEquals(expected, got);
        }
        int mem = 0;
        for (long k : cache.keySet()) {
            mem += cache.getMemory(k);
        }
        assertEquals(mem, cache.getUsedMemory());
        List<Long> stack = cache.keys(false, false);
        List<Long> cold = cache.keys(true, false);
        List<Long> nonResident = cache.keys(true, true);
        assertEquals(nonResident.size(), cache.sizeNonResident());
        HashSet<Long> hot = new HashSet<Long>(stack);
        hot.removeAll(cold);
        hot.removeAll(nonResident);
        assertEquals(hot.size(), cache.sizeHot());
        assertEquals(hot.size() + cold.size(), cache.size());
        if (stack.size() > 0) {
            long lastStack = stack.get(stack.size() - 1);
            assertTrue(hot.contains(lastStack));
        }
    }

    private static <V> CacheLongKeyLIRS<V> createCache(int maxElements) {
        return createCache(maxElements, 1);
    }

    private static <V> CacheLongKeyLIRS<V> createCache(int maxSize,
            int averageSize) {
        return new CacheLongKeyLIRS<V>(maxSize, averageSize, 1, 0);
    }

}
