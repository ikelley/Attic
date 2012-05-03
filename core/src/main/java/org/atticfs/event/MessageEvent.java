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

package org.atticfs.event;

import org.atticfs.types.WireType;

import java.util.EventObject;

/**
 * Class Description Here...
 *
 * 
 */

public abstract class MessageEvent extends EventObject {

    private int status = -1;
    private WireType type;
    private String detail;
    private boolean successful = false;
    private Throwable throwable;

    public MessageEvent(Object o, int status, WireType type, String detail, boolean successful) {
        super(o);
        this.status = status;
        this.type = type;
        this.detail = detail;
        this.successful = successful;
    }

    public MessageEvent(Object o, int status, WireType type, Throwable throwable) {
        super(o);
        this.status = status;
        this.type = type;
        this.throwable = throwable;
        this.detail = throwable.getMessage();
        this.successful = false;
    }

    public MessageEvent(Object o, WireType type, String detail, boolean successful) {
        super(o);
        this.type = type;
        this.detail = detail;
        this.successful = successful;
    }

    public MessageEvent(Object o, WireType type, Throwable throwable) {
        super(o);
        this.type = type;
        this.throwable = throwable;
        this.detail = throwable.getMessage();
        this.successful = false;
    }

    public int getStatus() {
        return status;
    }

    public WireType getType() {
        return type;
    }

    public String getDetail() {
        return detail;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
