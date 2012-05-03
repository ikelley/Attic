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

/**
 * Class Description Here...
 *
 * 
 */

public class AtticLog extends Log {

    public static enum LogRole {
        DC,
        DW,
        DL,
        DP
    }

    public static enum Action {
        DOWNLOAD,
        DATA_QUERY,
        DATA_PUBLISH,
        DATA_CACHE,
        DATA_REGISTER
    }

    public AtticLog(Type type) {
        super("AtticLog", type);
    }

    public AtticLog(Type type, LogProcessor logProcessor) {
        super("AtticLog", type, logProcessor);
    }


    public void setAction(Action action) {
        super.setAction(action.toString());
    }

    public void setRole(LogRole role) {
        super.setRole(role.toString());
    }

}
