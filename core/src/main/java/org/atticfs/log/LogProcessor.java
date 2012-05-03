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

package org.atticfs.log;

import java.util.ArrayList;
import java.util.List;

/**
 * An interface to something that can process a Log object.
 *
 * @author Andrew Harrison
 * @version $Revision: 1.5 $
 * @created Jun 18, 2008: 4:03:13 PM
 * @date $Date: 2008-11-07 11:37:22 $ modified by $Author: harrison $
 * @todo Put your notes here...
 */
public abstract class LogProcessor {

    private List<String> scopes = new ArrayList<String>();
    private boolean disabled;

    public LogProcessor() {
        this("Log");
    }

    public LogProcessor(String... scopes) {
        for (String scope : scopes) {
            this.scopes.add(scope);
        }
    }

    public void addScopes(String... scopes) {
        for (String scope : scopes) {
            this.scopes.add(scope);
        }
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    /**
     * scope the processor to various types of logs.
     * These will be matched against the Log's name.
     */
    public List<String> getScopes() {
        return scopes;
    }

    /**
     * process a Log starting
     *
     * @param log
     */
    public abstract void start(Log log);

    /**
     * process a Log ending
     *
     * @param log
     */
    public abstract void end(Log log);

    /**
     * process a Log error
     *
     * @param log
     */
    public abstract void error(Log log);

    /**
     * close and clean up. No more logging will happen
     */
    public abstract void close();


}
