package com.soft305.mdb.parse;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by pablo on 5/20/17.
 */
public class ChunkQueue {

    private LinkedBlockingQueue<byte[]> chunkQueue;
    private byte[] leftOverChunk;
    private int leftOverChunkOffset = 0;

    public ChunkQueue() {
        chunkQueue = new LinkedBlockingQueue<>();
    }

    public boolean offer(byte[] chunk) {
        if (chunk.length > 0) {
            return chunkQueue.offer(chunk);
        }
        return false;
    }

    /**
     *  If zero is returned, means the leftover chunk has enough bytes to provide the consumer.
     * */
    private int canProvide(int count) {
        int len = 0;
        int chunksNeeded = 0;

        if (leftOverChunk != null) {
            len += leftOverChunk.length - leftOverChunkOffset;
            if (len >= count) {
                return chunksNeeded;
            }
        }

        for (byte[] chunk : chunkQueue) {
            len += chunk.length;
            chunksNeeded ++;
            if (len >= count) {
                return chunksNeeded;
            }
        }

        return -1;
    }

    // Just for Testing
    public int getTotalLength() {
        int len = 0;
        if (leftOverChunk != null) {
            len += leftOverChunk.length;
        }

        for (byte[] chunk : chunkQueue) {
            len += chunk.length;
        }

        return len;
    }

    // Just for Testing
    public int getAvailableLength() {
        int len = 0;
        if (leftOverChunk != null) {
            len += leftOverChunk.length - leftOverChunkOffset;
        }

        for (byte[] chunk : chunkQueue) {
            len += chunk.length;
        }

        return len;
    }

    /**
     * Retrieves and remove the specified amount of bytes from the chunks in the Queue.
     * Return a byte array with the specified amount of bytes in the order they were inserted.
     * Return null if the count is greater than the current amount of bytes or if counter is zero.
     * */
    public byte[] consume(int count) {

        int chunksNeeded = canProvide(count);

        if (chunksNeeded < 0 || count == 0) {
            return null;
        }

        byte[] bytePack = new byte[count];
        int bytePackOffset = 0;

        if (chunksNeeded == 0) { // Means the leftOverChunk is enough

            while (bytePackOffset < count) {
                bytePack[bytePackOffset] = leftOverChunk[leftOverChunkOffset + bytePackOffset];
                bytePackOffset ++;
            }

            leftOverChunkOffset += count;

            int leftOverChunkBytes = leftOverChunk.length - leftOverChunkOffset;
            if (leftOverChunkBytes <= 0) {
                leftOverChunk = null;
                leftOverChunkOffset = 0;
            }

            return bytePack;
        }

        // leftOverChunk was not enough or did not existed.

        if (leftOverChunk != null) { // If there is leftOverChunk copy it completely
            int leftOverChunkBytes = leftOverChunk.length - leftOverChunkOffset;

            System.arraycopy(leftOverChunk
                    , leftOverChunkOffset
                    , bytePack
                    , bytePackOffset
                    , leftOverChunkBytes);

            bytePackOffset += leftOverChunkBytes;
            leftOverChunk = null;
            leftOverChunkOffset = 0;
        }

        // It will copy all the chunks until the last needed to complete the count.
        for (int i=0; i<(chunksNeeded-1); i++) {
            byte[] chunk = chunkQueue.poll();

            System.arraycopy(chunk
                    , 0
                    , bytePack
                    , bytePackOffset
                    , chunk.length);

            bytePackOffset += chunk.length;
        }

        byte[] lastNeededChunk = chunkQueue.poll();
        int lastChunkOffset = 0;

        while ((bytePackOffset + lastChunkOffset) < count) {
            bytePack[bytePackOffset + lastChunkOffset] = lastNeededChunk[lastChunkOffset];
            lastChunkOffset ++;
        }

        if (lastChunkOffset < lastNeededChunk.length) {
            leftOverChunk = lastNeededChunk;
            leftOverChunkOffset = lastChunkOffset;
        }

        return bytePack;
    }

    /**
     * Retrieves but does not remove the specified amount of bytes from the chunks in the Queue.
     * TODO: Add test to check that the returned array is the expected one
     * */
    public byte[] peek(int count) {

        int chunksNeeded = canProvide(count);

        if ( chunksNeeded < 0 || count == 0) {
            return null;
        }

        byte[] bytePack = new byte[count];
        int bytePackOffset = 0;

        if (chunksNeeded == 0) { // Means the leftOverChunk is enough

            while (bytePackOffset < count) {
                bytePack[bytePackOffset] = leftOverChunk[leftOverChunkOffset + bytePackOffset];
                bytePackOffset ++;
            }

            return bytePack;
        }

        // leftOverChunk was not enough or did not existed.

        if (leftOverChunk != null) { // If there is leftOverChunk copy it completely
            int leftOverChunkBytes = leftOverChunk.length - leftOverChunkOffset;

            System.arraycopy(leftOverChunk
                    , leftOverChunkOffset
                    , bytePack
                    , bytePackOffset
                    , leftOverChunkBytes);

            bytePackOffset += leftOverChunkBytes;
        }

        // It will copy all the chunks until the last needed(does not remove them)
        int i=0;
        for (byte[] chunk : chunkQueue) {

            if (i < chunksNeeded - 1) {

                System.arraycopy(chunk
                        , 0
                        , bytePack
                        , bytePackOffset
                        , chunk.length);

                bytePackOffset += chunk.length;

            } else if (i == chunksNeeded - 1) {
                int lastChunkOffset = 0;

                while ((bytePackOffset + lastChunkOffset) < count) {
                    bytePack[bytePackOffset + lastChunkOffset] = chunk[lastChunkOffset];
                    lastChunkOffset ++;
                }

            } else {
                break;
            }

            i++;
        }

        return bytePack;
    }

    public void empty() {
        chunkQueue.clear();
    }

}
