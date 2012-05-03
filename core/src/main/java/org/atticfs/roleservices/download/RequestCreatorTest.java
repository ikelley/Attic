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

package org.atticfs.roleservices.download;

import org.atticfs.download.request.EndpointRequest;
import org.atticfs.download.request.RequestCollection;
import org.atticfs.types.DataDescription;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileHash;
import org.atticfs.types.FileSegmentHash;

/**
 * Class Description Here...
 *
 * 
 */

public class RequestCreatorTest {

    public void testCreator1() {
        System.out.println("====================================testCreator1====================================");
        DataDescription dd = createDescription1();

        RequestCollection coll = new RequestCollection(dd);

        EndpointRequest p = new EndpointRequest(new Endpoint("http://foo.com"));
        p.addChunk(new FileSegmentHash("q1", 0L, 9L));
        p.addChunk(new FileSegmentHash("q2", 10L, 19L));
        p.addChunk(new FileSegmentHash("q3", 20L, 29L));
        p.setReceiveTime(3);
        coll.addMapping(p);

        EndpointRequest p1 = new EndpointRequest(new Endpoint("http://bar.com"));
        p1.addChunk(new FileSegmentHash("q4", 30L, 39L));
        p1.addChunk(new FileSegmentHash("q6", 50L, 59L));
        p1.addChunk(new FileSegmentHash("q5", 40L, 49L));
        p1.setReceiveTime(4);
        coll.addMapping(p1);
        System.out.println("RequestCreatorTest.testCreator collection:\n" + coll);

    }

    public void testCreator2() {
        System.out.println("====================================testCreator2====================================");

        DataDescription dd = createDescription1();

        RequestCollection coll = new RequestCollection(dd);

        EndpointRequest p = new EndpointRequest(new Endpoint("http://foo.com"));
        p.addChunk(new FileSegmentHash("q1", 0L, 9L));
        //p.addChunk(new FileSegmentHash("q2", 10L, 19L));
        p.addChunk(new FileSegmentHash("q3", 20L, 29L));
        p.setReceiveTime(3);
        coll.addMapping(p);

        EndpointRequest p1 = new EndpointRequest(new Endpoint("http://bar.com"));
        p1.addChunk(new FileSegmentHash("q4", 30L, 39L));
        p1.addChunk(new FileSegmentHash("q6", 50L, 59L));
        p1.addChunk(new FileSegmentHash("q5", 40L, 49L));
        p1.setReceiveTime(4);
        coll.addMapping(p1);
        System.out.println("RequestCreatorTest.testCreator collection:\n" + coll);

    }

    public void testCreator3() {
        System.out.println("====================================testCreator3====================================");

        DataDescription dd = createDescription1();

        RequestCollection coll = new RequestCollection(dd);

        EndpointRequest p = new EndpointRequest(new Endpoint("http://foo.com"));
        p.addChunk(new FileSegmentHash("q1", 0L, 9L));
        p.addChunk(new FileSegmentHash("q2", 10L, 19L));
        p.addChunk(new FileSegmentHash("q3", 20L, 29L));
        p.setReceiveTime(3);
        coll.addMapping(p);

        EndpointRequest p1 = new EndpointRequest(new Endpoint("http://bar.com"));
        p1.addChunk(new FileSegmentHash("q4", 30L, 39L));
        //p1.addChunk(new FileSegmentHash("q6", 50L, 59L));
        p1.addChunk(new FileSegmentHash("q5", 40L, 49L));
        p1.setReceiveTime(4);
        coll.addMapping(p1);
        System.out.println("RequestCreatorTest.testCreator collection:\n" + coll);

    }

    public void testCreator4() {
        System.out.println("====================================testCreator4====================================");

        DataDescription dd = createDescription1();

        RequestCollection coll = new RequestCollection(dd);

        EndpointRequest p = new EndpointRequest(new Endpoint("http://foo.com"));
        p.addChunk(new FileSegmentHash("q1", 0L, 9L));
        p.addChunk(new FileSegmentHash("q2", 10L, 19L));
        p.addChunk(new FileSegmentHash("q3", 20L, 29L));
        p.setReceiveTime(3);
        coll.addMapping(p);

        EndpointRequest p1 = new EndpointRequest(new Endpoint("http://bar.com"));
        p1.addChunk(new FileSegmentHash("q4", 30L, 39L));
        p1.addChunk(new FileSegmentHash("q6", 50L, 59L));
        p1.addChunk(new FileSegmentHash("q5", 40L, 49L));
        p1.addChunk(new FileSegmentHash("q2", 10L, 19L));
        p1.addChunk(new FileSegmentHash("q3", 20L, 29L));
        p1.setReceiveTime(4);
        coll.addMapping(p1);
        System.out.println("RequestCreatorTest.testCreator collection:\n" + coll);

    }

    public void testCreator5() {
        System.out.println("====================================testCreator5====================================");

        DataDescription dd = createDescription2();

        RequestCollection coll = new RequestCollection(dd);

        EndpointRequest h1 = createMapping("foo", 0, 10, 3);
        coll.addMapping(h1);

        EndpointRequest h2 = createMapping("bar", 10, 20, 4);
        coll.addMapping(h2);

        EndpointRequest h3 = createMapping("foo1", 20, 30, 5);
        coll.addMapping(h3);

        EndpointRequest h4 = createMapping("bar1", 30, 40, 6);
        coll.addMapping(h4);

        EndpointRequest h5 = createMapping("foo2", 40, 50, 1);
        coll.addMapping(h5);

        EndpointRequest h6 = createMapping("bar2", 50, 60, 2);
        coll.addMapping(h6);

        EndpointRequest h7 = createMapping("foo3", 60, 70, 7);
        coll.addMapping(h7);

        EndpointRequest h8 = createMapping("bar3", 70, 90, 8);
        coll.addMapping(h8);

        EndpointRequest h9 = createMapping("foo4", 60, 90, 9);
        coll.addMapping(h9);

        EndpointRequest h10 = createMapping("bar4", 90, 100, 0);
        coll.addMapping(h10);


        System.out.println("RequestCreatorTest.testCreator collection:\n" + coll);

    }

    private EndpointRequest createMapping(String domain, int start, int end, long receive) {
        EndpointRequest mapping = new EndpointRequest(new Endpoint("http://" + domain + ".com"));
        for (int i = start; i < end; i++) {
            int offset = i * 10;
            mapping.addChunk(new FileSegmentHash("hash" + i, offset, offset + 9));
        }
        mapping.setReceiveTime(receive);
        return mapping;
    }

    private DataDescription createDescription1() {
        DataDescription d = new DataDescription("1", "my data");
        FileHash fh = new FileHash();
        fh.setSize(60);
        fh.addSegment(new FileSegmentHash("q1", 0L, 9L));
        fh.addSegment(new FileSegmentHash("q2", 10L, 19L));
        fh.addSegment(new FileSegmentHash("q3", 20L, 29L));
        fh.addSegment(new FileSegmentHash("q4", 30L, 39L));
        fh.addSegment(new FileSegmentHash("q5", 40L, 49L));
        fh.addSegment(new FileSegmentHash("q6", 50L, 59L));

        d.setHash(fh);
        return d;
    }

    private DataDescription createDescription2() {
        DataDescription d = new DataDescription("2", "my data");
        FileHash fh = new FileHash();
        fh.setSize(1000);
        for (int i = 0; i < 100; i++) {
            int offset = i * 10;
            fh.addSegment(new FileSegmentHash("hash" + i, offset, offset + 9));
        }
        d.setHash(fh);
        return d;
    }
}
