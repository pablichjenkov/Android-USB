package com.soft305.mdb.input;

import com.soft305.mdb.Input;
import com.soft305.mdb.parser.ChunkQueue;


public class VmcInput implements Input {

    public final ChunkQueue chunkQueue;

    public VmcInput(ChunkQueue chunkQueue) {
        this.chunkQueue = chunkQueue;
    }

}
