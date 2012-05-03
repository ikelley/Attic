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

package org.atticfs.roles;

import org.atticfs.Attic;
import org.atticfs.config.html.Configurable;

import java.io.IOException;
import java.util.List;

/**
 * Class Description Here...
 *
 * 
 */

public abstract class AbstractRole implements Role {

    private Attic attic;

    public void init(Attic attic) throws IOException {
        this.attic = attic;
        List<Configurable> cs = this.attic.getConfigurables();
        for (Configurable c : cs) {
            c.configure(this);
        }
    }

    public abstract void shutdown() throws IOException;

    public Attic getAttic() {
        return attic;
    }
}
