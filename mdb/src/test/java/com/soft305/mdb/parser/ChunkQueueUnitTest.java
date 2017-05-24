package com.soft305.mdb.parser;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ChunkQueueUnitTest {

    @Test
    public void chunkQueueTest0() throws Exception {

        byte[] chunk1 = new byte[]{1}; //1
        byte[] chunk2 = new byte[]{2,3}; //2
        byte[] chunk3 = new byte[]{4,5,6}; //3
        byte[] chunk4 = new byte[]{7,8,9,10}; //4
        byte[] chunk5 = new byte[]{11,12,13,14,15}; //5

        ChunkQueue chunkQueue = new ChunkQueue();
        chunkQueue.offer(chunk1);
        chunkQueue.offer(chunk2);
        chunkQueue.offer(chunk3);
        chunkQueue.offer(chunk4);
        chunkQueue.offer(chunk5);

        // step 1
        byte[] data = chunkQueue.consume(2);
        chunkQueue.peek(2);

        assertArrayEquals(data, new byte[]{1,2});
        assertEquals(14, chunkQueue.getTotalLength());
        assertEquals(13, chunkQueue.getAvailableLength());

        // step 2
        chunkQueue.peek(5);
        data = chunkQueue.consume(3);

        assertArrayEquals(data, new byte[]{3,4,5});
        assertEquals(12, chunkQueue.getTotalLength());
        assertEquals(10, chunkQueue.getAvailableLength());

        // step 3
        chunkQueue.peek(9);
        data = chunkQueue.consume(10);

        assertArrayEquals(data, new byte[]{6,7,8,9,10,11,12,13,14,15});
        assertEquals(0, chunkQueue.getTotalLength());
        assertEquals(0, chunkQueue.getAvailableLength());

    }

    @Test
    public void chunkQueueTest1() throws Exception {


        byte[] chunk1 = new byte[]{1}; //1
        byte[] chunk2 = new byte[]{2,3}; //2
        byte[] chunk3 = new byte[]{4,5,6}; //3
        byte[] chunk4 = new byte[]{7,8,9,10}; //4
        byte[] chunk5 = new byte[]{11,12,13,14,15}; //5


        // block 1
        ChunkQueue chunkQueue = new ChunkQueue();
        chunkQueue.offer(chunk1);
        chunkQueue.offer(chunk2);
        chunkQueue.offer(chunk3);
        chunkQueue.offer(chunk4);
        chunkQueue.offer(chunk5);

        assertEquals(15, chunkQueue.getTotalLength());


        // block 2
        chunkQueue.peek(3);
        byte[] data1 = chunkQueue.consume(1);
        chunkQueue.peek(1);

        assertArrayEquals(data1, chunk1);
        assertEquals(14, chunkQueue.getTotalLength());
        assertEquals(14, chunkQueue.getAvailableLength());


        // block 3
        chunkQueue.peek(4);
        byte[] data2 = chunkQueue.consume(1);
        chunkQueue.peek(0);

        assertArrayEquals(data2, new byte[]{2});
        assertEquals(14, chunkQueue.getTotalLength());
        assertEquals(13, chunkQueue.getAvailableLength());


        // block 4
        chunkQueue.peek(1);
        chunkQueue.peek(2);
        byte[] data3 = chunkQueue.consume(2);
        chunkQueue.peek(3);
        chunkQueue.peek(4);

        assertArrayEquals(data3, new byte[]{3,4});
        assertEquals(12, chunkQueue.getTotalLength());
        assertEquals(11, chunkQueue.getAvailableLength());


        // block 5
        byte[] data4 = chunkQueue.consume(2);

        assertArrayEquals(data4, new byte[]{5,6});
        assertEquals(9, chunkQueue.getTotalLength());
        assertEquals(9, chunkQueue.getAvailableLength());


        // block 6
        byte[] data5 = chunkQueue.consume(1);
        assertArrayEquals(data5, new byte[]{7});

        data5 = chunkQueue.consume(2);
        assertArrayEquals(data5, new byte[]{8,9});

        data5 = chunkQueue.consume(1);
        assertArrayEquals(data5, new byte[]{10});

        assertEquals(5, chunkQueue.getTotalLength());
        assertEquals(5, chunkQueue.getAvailableLength());


        // block 7

        chunkQueue.offer(chunk4);
        assertEquals(9, chunkQueue.getTotalLength());
        assertEquals(9, chunkQueue.getAvailableLength());

        byte[] data6 = chunkQueue.consume(9);
        assertArrayEquals(data6, new byte[]{11,12,13,14,15,7,8,9,10});
        assertEquals(0, chunkQueue.getTotalLength());
        assertEquals(0, chunkQueue.getAvailableLength());


        // block 8
        chunkQueue.empty();
        assertEquals(0, chunkQueue.getTotalLength());
        assertEquals(0, chunkQueue.getAvailableLength());

    }

    @Test
    public void chunkQueueTest2() throws Exception {

        byte[] chunk1 = new byte[]{1}; //1
        byte[] chunk2 = new byte[]{2,3}; //2
        byte[] chunk3 = new byte[]{4,5,6}; //3
        byte[] chunk4 = new byte[]{7,8,9,10}; //4
        byte[] chunk5 = new byte[]{11,12,13,14,15}; //5

        ChunkQueue chunkQueue = new ChunkQueue();
        chunkQueue.offer(chunk1);
        chunkQueue.offer(chunk2);
        chunkQueue.offer(chunk3);
        chunkQueue.offer(chunk4);
        chunkQueue.offer(chunk5);

        // step 1
        byte[] data = chunkQueue.consume(10);

        assertArrayEquals(data, new byte[]{1,2,3,4,5,6,7,8,9,10});
        assertEquals(5, chunkQueue.getTotalLength());
        assertEquals(5, chunkQueue.getAvailableLength());

    }

    @Test
    public void chunkQueueTest4() throws Exception {

        byte[] chunk1 = new byte[]{1}; //1
        byte[] chunk2 = new byte[]{2,3}; //2
        byte[] chunk3 = new byte[]{4,5,6}; //3
        byte[] chunk4 = new byte[]{7,8,9,10}; //4
        byte[] chunk5 = new byte[]{11,12,13,14,15}; //5

        ChunkQueue chunkQueue = new ChunkQueue();
        chunkQueue.offer(chunk1);
        chunkQueue.offer(chunk2);
        chunkQueue.offer(chunk3);
        chunkQueue.offer(chunk4);
        chunkQueue.offer(chunk5);

        // step 1
        byte[] data = chunkQueue.consume(12);

        assertArrayEquals(data, new byte[]{1,2,3,4,5,6,7,8,9,10,11,12});
        assertEquals(5, chunkQueue.getTotalLength());
        assertEquals(3, chunkQueue.getAvailableLength());


        // step 2
        data = chunkQueue.consume(2);

        assertArrayEquals(data, new byte[]{13,14});
        assertEquals(5, chunkQueue.getTotalLength());
        assertEquals(1, chunkQueue.getAvailableLength());

    }

    @Test
    public void chunkQueueTest5() throws Exception {

        byte[] chunk1 = new byte[]{1}; //1
        byte[] chunk2 = new byte[]{2,3}; //2
        byte[] chunk3 = new byte[]{4,5,6}; //3
        byte[] chunk4 = new byte[]{7,8,9,10}; //4
        byte[] chunk5 = new byte[]{11,12,13,14,15}; //5

        ChunkQueue chunkQueue = new ChunkQueue();
        chunkQueue.offer(chunk1);
        chunkQueue.offer(chunk2);
        chunkQueue.offer(chunk3);
        chunkQueue.offer(chunk4);
        chunkQueue.offer(chunk5);


        // step 1
        byte[] data = chunkQueue.consume(15);

        assertArrayEquals(data, new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
        assertEquals(0, chunkQueue.getTotalLength());
        assertEquals(0, chunkQueue.getAvailableLength());


        // step 2
        data = chunkQueue.consume(2);

        assertArrayEquals(data, null);
        assertEquals(0, chunkQueue.getTotalLength());
        assertEquals(0, chunkQueue.getAvailableLength());


        // step 3
        data = chunkQueue.consume(1);

        assertArrayEquals(data, null);
        assertEquals(0, chunkQueue.getTotalLength());
        assertEquals(0, chunkQueue.getAvailableLength());


        // step 4
        chunkQueue.offer(chunk1);
        data = chunkQueue.consume(2);

        assertArrayEquals(data, null);
        assertEquals(1, chunkQueue.getTotalLength());
        assertEquals(1, chunkQueue.getAvailableLength());

        chunkQueue.offer(chunk5);
        data = chunkQueue.consume(3);

        assertArrayEquals(data, new byte[]{1,11,12});
        assertEquals(5, chunkQueue.getTotalLength());
        assertEquals(3, chunkQueue.getAvailableLength());

        // step 5
        chunkQueue.offer(chunk3);
        data = chunkQueue.consume(4);

        assertArrayEquals(data, new byte[]{13,14,15,4});
        assertEquals(3, chunkQueue.getTotalLength());
        assertEquals(2, chunkQueue.getAvailableLength());


        // step 6
        chunkQueue.offer(chunk2);
        data = chunkQueue.consume(3);

        assertArrayEquals(data, new byte[]{5,6,2});
        assertEquals(2, chunkQueue.getTotalLength());
        assertEquals(1, chunkQueue.getAvailableLength());

    }

}