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

import java.util.concurrent.BlockingQueue;

/**
 * Class Description Here...
 *
 * 
 */

public class IndexReceiver implements Runnable {

    private BlockingQueue<FileMapping> queue;
    private IndexListener listener;


    public IndexReceiver(BlockingQueue<FileMapping> queue, IndexListener listener) {
        this.queue = queue;
        this.listener = listener;
    }

    public IndexReceiver(BlockingQueue<FileMapping> queue) {
        this(queue, null);
    }

    public void run() {
        while (true) {
            try {
                FileMapping fileMapping = queue.take();
                if (fileMapping.getFile() == null) { // signal to end
                    if (listener != null) {
                        listener.indexComplete();
                    }
                    break;
                }
                listener.index(fileMapping);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
