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

import org.atticfs.identity.Identity;
import org.atticfs.store.DataAdvertStore;
import org.atticfs.types.Constraint;
import org.atticfs.types.Constraints;
import org.atticfs.types.DataAdvert;
import org.atticfs.util.ConstraintMatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class MemoryDataAdvertStore implements DataAdvertStore {

    static Logger log = Logger.getLogger("org.atticfs.impl.store.MemoryDataAdvertStore");

    private Semaphore lock = new Semaphore(1);
    private Map<String, StoredAdvert> adverts = new ConcurrentHashMap<String, StoredAdvert>();


    public DataAdvert addDataAdvert(Identity identity, DataAdvert advert) {
        Constraints cs = advert.getConstraints();
        Constraint c = cs.getConstraint(DataAdvert.REPLICA);
        int max = Integer.MAX_VALUE;
        if (c != null) {
            max = c.getIntegerValue();
        }
        Constraint e = cs.getConstraint(DataAdvert.EXPIRY);
        long exp = Long.MAX_VALUE;
        if (e != null) {
            exp = e.getLongValue();
        }
        StoredAdvert rc = new StoredAdvert(max, exp, advert, identity);

        log.fine(" adding new advert with id:" + advert.getDataDescription().getId());
        try {
            lock.acquire();
            adverts.put(advert.getDataDescription().getId(), rc);
            return advert;
        } catch (InterruptedException ie) {

        } finally {
            lock.release();
        }
        return null;
    }

    public DataAdvert getDataAdvert(String id) {
        try {
            lock.acquire();
            StoredAdvert rc = adverts.get(id);
            if (rc != null) {
                return rc.getAdvert();
            }
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return null;
    }

    public DataAdvert deleteDataAdvert(Identity identity, String id) {
        try {
            lock.acquire();
            StoredAdvert rc = adverts.get(id);
            if (rc != null) {
                Identity i = rc.getIdentity();
                if (i != null && identity == null) {
                    return null;
                }
                if (i == null && identity == null) {
                    return rc.getAdvert();
                }
                if (i != null && identity != null) {
                    if (i.matches(identity)) {
                        adverts.remove(id);
                        return rc.getAdvert();
                    } else {
                        return null;
                    }
                } else {
                    return rc.getAdvert();
                }
            }
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return null;
    }

    public List<DataAdvert> getDataAdverts() {
        List<DataAdvert> ret = new ArrayList<DataAdvert>();
        try {
            lock.acquire();
            Iterator<StoredAdvert> it = adverts.values().iterator();
            while (it.hasNext()) {
                StoredAdvert count = it.next();
                if (count.isValid()) {
                    ret.add(count.getAdvert());
                } else {
                    if (count.isExpired()) {
                        it.remove();
                    }
                }
            }
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return ret;
    }

    public List<DataAdvert> getDataAdverts(Constraints constraints) {
        List<DataAdvert> cached = getDataAdverts();
        List<DataAdvert> ret = new ArrayList<DataAdvert>();
        for (DataAdvert dataAdvert : cached) {
            boolean match = true;
            Constraints cs = dataAdvert.getConstraints();
            List<Constraint> c = constraints.getConstraints();
            for (Constraint constraint : c) {
                if (!ConstraintMatcher.matches(constraint, cs.getConstraint(constraint.getKey()))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                ret.add(dataAdvert);
            }
        }
        return ret;
    }

    public void dataCached(DataAdvert advert) {
        StoredAdvert rc = adverts.get(advert.getDataDescription().getId());
        if (rc != null) {
            rc.decTtl();
        }
    }

    public void dataUncached(DataAdvert advert) {
        StoredAdvert rc = adverts.get(advert.getDataDescription().getId());
        if (rc != null) {
            rc.incTtl();
        }
    }

    public Identity getIdentity(DataAdvert advert) {
        StoredAdvert rc = adverts.get(advert.getDataDescription().getId());
        if (rc != null) {
            return rc.getIdentity();
        }
        return null;
    }

    public void init() {
    }

    public void shutdown() {
    }


    private class StoredAdvert {
        private int ttl;
        private long lastModified;
        private long expiry;
        private int maxReplica;
        private DataAdvert advert;
        private Identity identity;

        private StoredAdvert(int maxReplica, long expiry, DataAdvert advert, Identity identity) {
            this.maxReplica = maxReplica;
            this.ttl = maxReplica;
            this.expiry = expiry;
            this.advert = advert;
            this.identity = identity;
            this.lastModified = System.currentTimeMillis();
        }

        public int getTtl() {
            return ttl;
        }

        public void decTtl() {
            this.ttl--;
        }

        public void incTtl() {
            this.ttl++;
        }

        public int getMaxReplica() {
            return maxReplica;
        }

        public DataAdvert getAdvert() {
            return advert;
        }

        public long getLastModified() {
            return lastModified;
        }

        public Identity getIdentity() {
            return identity;
        }

        public boolean isValid() {
            return expiry > System.currentTimeMillis() && ttl > 0;
        }

        public boolean isExpired() {
            return expiry < System.currentTimeMillis();
        }

    }
}
