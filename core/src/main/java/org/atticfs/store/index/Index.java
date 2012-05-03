/*
 * Copyright 2004 - 2012 Cardiff University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atticfs.store.index;

import org.atticfs.Attic;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class Index {

    static Logger log = Logger.getLogger("org.atticfs.store.index.Index");

    public static final String INDEX = "org.atticfs.store.index";

    private Attic attic;
    private IndexListener listener;

    public Index(Attic attic, IndexListener listener) {
        this.attic = attic;
        this.listener = listener;
    }

    public void index(File root) {
        BlockingQueue<FileMapping> queue = new LinkedBlockingQueue<FileMapping>();
        Indexer i = new Indexer(root, attic.getDataConfig().getFileSegmentHashSize(), queue);
        IndexReceiver ir = new IndexReceiver(queue, listener);
        attic.execute(i);
        attic.execute(ir);
    }
}
