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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * implementation of the LogProcessor that spits out the Log objects to the underlying logging system
 * at an info level.
 *
 * @author Andrew Harrison
 * @version $Revision: 1.8 $
 * @created Jun 18, 2008: 4:15:09 PM
 * @date $Date: 2009-02-19 16:02:56 $ modified by $Author: harrison $
 * @todo Put your notes here...
 */

public class LoggerProcessor extends LogProcessor {

    private PrintStream stream = System.out;

    public LoggerProcessor() {
        super();
    }

    public LoggerProcessor(String... scopes) {
        super(scopes);
    }

    public LoggerProcessor(File log, String... scopes) {
        super(scopes);
        try {
            if (log.exists() && log.length() > 0) {
                log.delete();
            }
            this.stream = new PrintStream(log);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public LoggerProcessor(File log, boolean singleLine, String... scopes) {
        super(scopes);
        try {
            if (log.exists() && log.length() > 0) {
                log.delete();
            }
            this.stream = new PrintStream(log);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        LogProperties.setSingleLine(singleLine);
    }

    /**
     * process a Log starting
     *
     * @param log
     */
    public void start(Log log) {
        stream.println(log.toString());
    }

    /**
     * process a Log ending
     *
     * @param log
     */
    public void end(Log log) {
        //stream.println(log.toString());

    }

    /**
     * process a Log error
     *
     * @param log
     */
    public void error(Log log) {
        stream.println(log.toString());


    }

    /**
     * close and clean up. No more logging will happen
     */
    public void close() {
        if (stream != System.out && stream != System.err) {
            stream.flush();
            stream.close();
        }
    }
}
