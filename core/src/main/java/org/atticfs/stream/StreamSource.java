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

/**
 * A producer of streams for a StreamSink.
 * Sinks a Sources are tightly bound. Each responds to the other
 * in order to control the flow of data.
 *
 * 
 */
public interface StreamSource {

    /**
     * set the sink for receiving streams generated here.
     * This source will receive callbacks when a stream is exhausted
     * and when the streams have been closed at the sink side.
     *
     * @param sink
     */
    public void setSink(StreamSink sink);

    /**
     * callback when a stream is exhausted
     *
     * @param event
     */
    public void streamExhaused(StreamEvent event);

    /**
     * callback when the streams have been closed, possibly prematurely, by the sink side.
     */
    public void streamsClosed();

}
