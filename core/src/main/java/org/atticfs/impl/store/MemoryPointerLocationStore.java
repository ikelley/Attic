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

package org.atticfs.impl.store;

import org.atticfs.store.DataPointerLocationStore;
import org.atticfs.types.DataPointer;
import org.atticfs.types.Endpoint;

import java.util.*;

/**
 * Class Description Here...
 *
 * 
 */

public class MemoryPointerLocationStore implements DataPointerLocationStore {

    private Map<String, PointerEndpoints> pointerEndpoints = new HashMap<String, PointerEndpoints>();

    public void add(Endpoint endpoint, DataPointer pointer) {
        if (pointer.getDataDescription() == null || pointer.getDataDescription().getId() == null) {
            return;
        }
        PointerEndpoints pe = pointerEndpoints.get(pointer.getDataDescription().getId());
        if (pe == null) {
            pe = new PointerEndpoints();
        }
        pe.addPointer(pointer, endpoint);
        pointerEndpoints.put(pointer.getDataDescription().getId(), pe);
    }

    public Endpoint remove(DataPointer pointer) {
        if (pointer.getDataDescription() == null || pointer.getDataDescription().getId() == null) {
            return null;
        }
        PointerEndpoints pe = pointerEndpoints.get(pointer.getDataDescription().getId());
        List<Pair> pes = pe.getPairs();
        Endpoint ret = null;
        for (Pair pair : pes) {
            if (pair.getPointer().equals(pointer)) {
                ret = pair.getEndpoint();
                pe.removePair(pair);
            }
        }
        return ret;
    }

    public Endpoint get(DataPointer pointer) {
        if (pointer.getDataDescription() == null || pointer.getDataDescription().getId() == null) {
            return null;
        }
        PointerEndpoints pe = pointerEndpoints.get(pointer.getDataDescription().getId());
        List<Pair> pes = pe.getPairs();
        for (Pair pair : pes) {
            if (pair.getPointer().equals(pointer)) {
                return pair.getEndpoint();
            }
        }
        return null;
    }

    public List<DataPointer> get(String descriptionId) {
        PointerEndpoints pe = pointerEndpoints.get(descriptionId);
        if (pe != null) {
            return pe.getPointers();
        }
        return new ArrayList<DataPointer>();
    }

    public List<DataPointer> remove(String descriptionId) {
        PointerEndpoints pe = pointerEndpoints.remove(descriptionId);
        if (pe != null) {
            return pe.getPointers();
        }
        return new ArrayList<DataPointer>();
    }

    public void init() {
    }

    public void shutdown() {
    }


    private static class PointerEndpoints {
        private Set<Pair> pointers = new HashSet<Pair>();

        public void addPointer(DataPointer pointer, Endpoint endpoint) {
            pointers.add(new Pair(pointer, endpoint));
        }

        public List<DataPointer> getPointers() {
            ArrayList<DataPointer> ret = new ArrayList<DataPointer>();
            for (Pair pair : pointers) {
                ret.add(pair.getPointer());
            }
            return ret;
        }

        public void removePair(Pair pair) {
            pointers.remove(pair);

        }


        public List<Pair> getPairs() {
            return new ArrayList<Pair>(pointers);
        }
    }

    private static class Pair {
        private DataPointer pointer;
        private Endpoint endpoint;

        private Pair(DataPointer pointer, Endpoint endpoint) {
            this.pointer = pointer;
            this.endpoint = endpoint;
        }

        public DataPointer getPointer() {
            return pointer;
        }

        public Endpoint getEndpoint() {
            return endpoint;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair pair = (Pair) o;

            if (endpoint != null ? !endpoint.equals(pair.endpoint) : pair.endpoint != null) return false;
            if (pointer != null ? !pointer.equals(pair.pointer) : pair.pointer != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = pointer != null ? pointer.hashCode() : 0;
            result = 31 * result + (endpoint != null ? endpoint.hashCode() : 0);
            return result;
        }
    }
}
