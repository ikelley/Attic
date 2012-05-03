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

package org.atticfs.stream;

import org.atticfs.Attic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Input Stream for reading from a Data Pointer endpoint with an attic:// scheme
 * this is designed to work in conjunciton with a StreamSource which provides the streams.
 * But this is not manditory.
 * <p/>
 * As long as something calls the streamArrived() method it will work.
 * <p/>
 * If an Attic is passed into the constructor, then this wil shut down the attic
 * when close() is called.
 *
 * 
 */

public class AtticInputStream extends InputStream implements StreamSink {

    static Logger log = Logger.getLogger("org.atticfs.stream.AtticInputStream");

    private Attic attic;

    public AtticInputStream(Attic attic) {
        this.attic = attic;
    }

    public AtticInputStream() {
    }

    private Map<Long, StreamEvent> queued = new ConcurrentHashMap<Long, StreamEvent>();
    private LinkedBlockingQueue<StreamEvent> streams = new LinkedBlockingQueue<StreamEvent>();
    private StreamSource source;

    private static StreamEvent EOF = new StreamEvent(new Object(), null, "eof", true, new EofStream(), -1, -1, null);

    private InputStream currStream = null;
    private StreamEvent currEvent = null;

    private AtomicLong nextOffset = new AtomicLong(0);
    private AtomicLong currLength = new AtomicLong(0);

    public void setSource(StreamSource source) {
        this.source = source;
    }

    /**
     * notification that a stream has arrived.
     * If the event offset of the bytes is the next expected
     * offset, then the stream is added to the pending list.
     * Otherwise it is stored for later.
     *
     * @param event
     */
    public void streamArrived(StreamEvent event) {
        if (event.isSuccessful()) {
            long start = event.getStartOffset();
            long end = event.getEndOffset();
            if (start == nextOffset.get()) {
                nextOffset.set(end + 1);
                streams.add(event);
            } else {
                queued.put(start, event);
            }
            while (true) {
                long currOff = nextOffset.get();
                StreamEvent evt = queued.remove(currOff);
                if (evt != null) {
                    streams.add(evt);
                    nextOffset.set(evt.getEndOffset() + 1);
                } else {
                    break;
                }
            }
        } else {
            Throwable t = event.getThrowable();
            if (t == null) {
                t = new Exception("Error reading from Streams.");
            }
            StreamEvent error = new StreamEvent(t, event.getDataDescription(), t.getMessage(), false, null, -1, -1, event.getStats());
            streams.add(error);
        }
    }

    public synchronized void streamsFinished(StreamEvent event) {
        log.info(event.getStats().display());
        streams.add(EOF);
    }


    private void notifyStreamExhausted(StreamEvent evt) {
        if (source != null) {
            source.streamExhaused(evt);
        }
    }

    private void notifyStreamsClosed() {
        if (source != null) {
            source.streamsClosed();
        }
    }

    public synchronized int read() throws IOException {
        getCurrStream();
        int ret = currStream.read();
        if (ret == -1 || ret == currLength.get() - 1) {
            getNextStream();
            return read();
        } else {
            return ret;
        }

    }

    public synchronized int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public synchronized int read(final byte[] b, final int off, final int len) throws IOException {
        getCurrStream();
        if (currStream instanceof EofStream) {
            return -1;
        }


        int ret = currStream.read(b, off, len);
        if (ret == -1) {
            getNextStream();
            ret = read(b, off, len);
        } else if ((off + ret) == currLength.get() - 1) {
            getNextStream();
            int r = read(b, ret, len - ret);
            if (r >= 0) {
                ret += r;
            }
        }
        return ret;
    }

    private void getCurrStream() throws IOException {
        try {
            while (currStream == null) {
                currEvent = streams.take();
                if (currEvent.getSource() instanceof Throwable) {
                    throw new IOException(((Throwable) currEvent.getSource()).getMessage());
                }
                currStream = currEvent.getStream();
                currLength.set(currEvent.getEndOffset() - currEvent.getStartOffset());
            }
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }
    }

    private void getNextStream() throws IOException {
        if (currStream != null) {
            currStream.close();
            currStream = null;
            notifyStreamExhausted(currEvent);
        }
        getCurrStream();
    }


    /**
     * this returns what is currently available in the current stream.
     * There is no way of knowing (without reading in all streams), whether
     * this constitutes the whole stream, and hence, whether the available
     * total should include the available from the following stream.
     *
     * @return
     * @throws IOException
     */
    public int available() throws IOException {
        getNextStream();
        return (currStream.available());
    }

    /**
     * closes all streams
     *
     * @throws IOException
     */
    public void close() throws IOException {

        try {
            while (streams.size() > 0) {
                StreamEvent evt = streams.take();
                evt.getStream().close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        notifyStreamsClosed();
        if (attic != null) {
            attic.shutdown();
        }
    }

    /**
     * returns false
     *
     * @return
     */
    public boolean markSupported() {
        return false;
    }


    private static class EofStream extends InputStream {

        public int read() throws IOException {
            return -1;
        }

        public int read(final byte[] b, final int off, final int len) throws IOException {
            return -1;
        }

        public int read(final byte[] b) throws IOException {
            return -1;
        }

        public void close() {

        }
    }

}
