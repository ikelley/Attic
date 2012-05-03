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

import java.util.ArrayList;
import java.util.List;

/**
 * Class Description Here...
 *
 * 
 */

public class PointerCollection extends WireType {

    private List<DataPointer> dataPointers = new ArrayList<DataPointer>();

    public PointerCollection() {
        super(WireType.Type.PointerCollection);
    }

    public List<DataPointer> getDataPointers() {
        return dataPointers;
    }

    public void setDataPointers(List<DataPointer> dataPointers) {
        this.dataPointers = dataPointers;
    }

    public void addDataPointer(DataPointer pointer) {
        dataPointers.add(pointer);
    }
}
