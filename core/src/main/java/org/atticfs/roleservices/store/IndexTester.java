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

package org.atticfs.roleservices.store;

import org.atticfs.Attic;
import org.atticfs.store.index.FileMapping;
import org.atticfs.store.index.IndexListener;
import org.atticfs.store.index.IndexReceiver;
import org.atticfs.store.index.Indexer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class Description Here...
 *
 * 
 */

public class IndexTester {

    public static void main(String[] args) {
        final Attic c = new Attic();
        c.init();
        BlockingQueue<FileMapping> queue = new LinkedBlockingQueue<FileMapping>();
        Indexer i = new Indexer(c.getDPDataHome(), c.getDataConfig().getFileSegmentHashSize(), queue);
        long now = System.currentTimeMillis();

        IndexReceiver ir = new IndexReceiver(queue, new IndexListener() {
            public void index(FileMapping mapping) {
                long now = System.currentTimeMillis();
                System.out.println("\n================IndexTester.index==================");
                try {
                    c.getSerializer().toStream(mapping.getFileHash(), System.out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long nownow = System.currentTimeMillis();

                System.out.println("IndexTester.indexComplete time:" + (nownow - now));
            }

            public void indexComplete() {

            }
        });
        new Thread(i).start();
        new Thread(ir).start();
    }
}
