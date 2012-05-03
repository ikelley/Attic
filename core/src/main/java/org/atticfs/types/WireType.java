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

package org.atticfs.types;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class WireType implements Serializable {

    static Logger log = Logger.getLogger("org.atticfs.types");


    public static enum Type {
        DataDescription,
        DataPointer,
        FileHash,
        Segment,
        DataCollection,
        PointerCollection,
        Endpoint,
        DataAdvert,
        Constraint,
        Constraints,
        DataQuery

    }

    private Type type;

    public WireType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
