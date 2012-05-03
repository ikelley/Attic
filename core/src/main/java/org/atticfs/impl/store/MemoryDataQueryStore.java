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

import org.atticfs.store.DataQueryStore;
import org.atticfs.types.Constraint;
import org.atticfs.types.Constraints;
import org.atticfs.types.DataQuery;
import org.atticfs.util.ConstraintMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * A query store. This implementation does nothing really, bar store them.
 * It never even clears the cache.
 * <p/>
 * More intelligent stores may look at the Constraints and base
 * caching on this, for example it a query had a ttl constraint
 * or a retry constraint.
 *
 * 
 */

public class MemoryDataQueryStore implements DataQueryStore {

    static Logger log = Logger.getLogger("org.atticfs.impl.store.MemoryDataQueryStore");

    private Semaphore lock = new Semaphore(1);
    private Set<DataQuery> queries = new HashSet<DataQuery>();


    public DataQuery addDataQuery(DataQuery query) {

        try {
            lock.acquire();
            queries.add(query);
            return query;
        } catch (InterruptedException ie) {

        } finally {
            lock.release();
        }
        return null;
    }

    public List<DataQuery> getDataQueries() {
        List<DataQuery> ret = new ArrayList<DataQuery>();
        try {
            lock.acquire();
            return new ArrayList<DataQuery>(queries);
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return ret;
    }

    public List<DataQuery> getDataQueries(Constraints constraints) {
        List<DataQuery> cached = getDataQueries();
        List<DataQuery> ret = new ArrayList<DataQuery>();
        for (DataQuery advert : cached) {
            boolean match = true;
            Constraints cs = advert.getConstraints();
            List<Constraint> c = constraints.getConstraints();
            for (Constraint constraint : c) {
                if (!ConstraintMatcher.matches(constraint, cs.getConstraint(constraint.getKey()))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                ret.add(advert);
            }
        }
        return ret;
    }

    public void init() {
    }

    public void shutdown() {
    }


}