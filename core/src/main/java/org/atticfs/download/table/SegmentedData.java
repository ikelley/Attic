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

package org.atticfs.download.table;

import org.atticfs.download.DownloadException;
import org.atticfs.types.FileSegmentHash;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

/**
 * represents a data unit made up of segments.
 *
 * 
 */

public class SegmentedData {


    private DownloadTable.Status status = DownloadTable.Status.EMPTY;

    private final long length;

    private Semaphore lock = new Semaphore(1);


    private SortedMap<Long, FileSegmentHash> segments = new TreeMap<Long, FileSegmentHash>();
    private SortedMap<Long, FileSegmentHash> gaps = new TreeMap<Long, FileSegmentHash>();

    private static FileSegmentHash TOKEN = new FileSegmentHash("token", 0, 0);

    public SegmentedData(long length) {
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public boolean containsChunk(FileSegmentHash hash) {
        try {
            lock.acquire();
            return segments.containsKey(hash.getHash());
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return false;
    }

    public int getBlockNumber() {
        return segments.size() / 2;
    }


    public List<FileSegmentHash> getChunks() {
        try {
            lock.acquire();
            return new ArrayList<FileSegmentHash>(segments.values());
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return new ArrayList<FileSegmentHash>();
    }

    public boolean addChunk(FileSegmentHash segment) throws DownloadException {
        try {
            lock.acquire();
            FileSegmentHash block = segments.get(segment.getStartOffset());
            if (block != null && block.getEndOffset() <= segment.getEndOffset()) {
                return false;
            }
            segments.put(segment.getStartOffset(), segment);
            segments.put(segment.getEndOffset(), TOKEN);
            setStatus(segment, true);
            return true;
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return false;

    }

    private void setStatus(FileSegmentHash block, boolean added) {
        DownloadTable.Status status;
        boolean atStart = false;
        boolean atEnd = false;
        if (added) {
            gaps.remove(block.getStartOffset());
            gaps.remove(block.getEndOffset());
        } else {
            gaps.put(block.getStartOffset(), TOKEN);
            gaps.put(block.getEndOffset(), TOKEN);
        }

        if (block.getStartOffset() > 0) {
            FileSegmentHash before = segments.get(block.getStartOffset() - 1);
            if (before == null) {
                gaps.put(block.getStartOffset() - 1, TOKEN);
            } else {
                gaps.remove(block.getStartOffset() - 1);
            }
        } else {
            atStart = true;
        }
        if (block.getEndOffset() < getLength() - 1) {
            FileSegmentHash after = segments.get(block.getEndOffset() + 1);
            if (after == null) {
                gaps.put(block.getEndOffset() + 1, TOKEN);
            } else {
                gaps.remove(block.getEndOffset() + 1);
            }
        } else {
            atEnd = true;
        }
        if (gaps.size() == 0) {
            if (atStart && atEnd) {
                status = DownloadTable.Status.COMPLETE;
            } else {
                if (segments.get(0L) != null && segments.get(getLength() - 1) != null) {
                    status = DownloadTable.Status.COMPLETE;
                } else {
                    status = DownloadTable.Status.CONTINUOUS;
                }
            }
        } else {
            if (gaps.size() == 1) {
                status = DownloadTable.Status.CONTINUOUS;

            } else if (gaps.size() == 2) {
                if (gaps.get(0L) != null || gaps.get(getLength() - 1) != null) {
                    status = DownloadTable.Status.CONTINUOUS;
                } else {
                    status = DownloadTable.Status.DISCONTINUOUS;
                }
            } else {
                status = DownloadTable.Status.DISCONTINUOUS;
            }
        }
        this.status = status;

    }


    public void addChunks(List<FileSegmentHash> segments) throws DownloadException {
        for (FileSegmentHash segment : segments) {
            addChunk(segment);
        }
    }


    /**
     * returns a list of Blocks representing the gaps in the data.
     *
     * @return
     */
    /*public List<FileSegmentHash> getGaps() {
        try {
            lock.acquire();
            List<FileSegmentHash> segs = new ArrayList<FileSegmentHash>();
            if (segments.size() == 0) {
                segs.add(new FileSegmentHash("none", 0, getLength() - 1));
            }
            for (int i = 0; i < segments.size(); i++) {
                FileSegmentHash curr = segments.get(i);
                if (i == 0 && curr.getStartOffset() > 0) {
                    segs.add(new FileSegmentHash("none", 0, curr.getStartOffset() - 1));
                }
                if (i < segments.size() - 1) {
                    FileSegmentHash next = segments.get(i + 1);
                    if (next.getStartOffset() > curr.getEndOffset() + 1) {
                        segs.add(new FileSegmentHash("none", curr.getEndOffset() + 1, next.getStartOffset() - 1));
                    }
                } else {
                    if (curr.getEndOffset() < getLength() - 1) {
                        segs.add(new FileSegmentHash("none", curr.getEndOffset() + 1, getLength() - 1));
                    }
                }
            }
            return segs;
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return new ArrayList<FileSegmentHash>();

    }*/
    public DownloadTable.Status getStatus() {
        return status;
    }

    public String toString() {
        try {
            lock.acquire();
            StringBuilder sb = new StringBuilder();
            for (FileSegmentHash segment : segments.values()) {
                sb.append(segment.getHash() + " " + segment.getStartOffset() + "-" + segment.getEndOffset() + ",");
            }
            return sb.toString();
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return "not known";
    }

    @Override
    public boolean equals(Object o) {
        try {
            lock.acquire();

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SegmentedData that = (SegmentedData) o;

            if (length != that.length) return false;
            if (segments != null ? !segments.equals(that.segments) : that.segments != null) return false;
            if (status != that.status) return false;

            return true;
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return false;
    }

    @Override
    public int hashCode() {
        try {
            lock.acquire();

            int result = status != null ? status.hashCode() : 0;
            result = 31 * result + (int) (length ^ (length >>> 32));
            result = 31 * result + (segments != null ? segments.hashCode() : 0);
            return result;
        } catch (InterruptedException e) {

        } finally {
            lock.release();
        }
        return -1;
    }
}
