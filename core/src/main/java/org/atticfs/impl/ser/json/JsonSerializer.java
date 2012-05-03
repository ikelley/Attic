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

package org.atticfs.impl.ser.json;

import org.atticfs.ser.Serializer;
import org.atticfs.types.WireType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class JsonSerializer implements Serializer {

    static Logger log = Logger.getLogger("org.atticfs.impl.ser.json.JsonSerializer");

    private boolean prettyPrint = false;


    public void setFeature(String feature, String value) {
        if (feature.equals(PRETTY_PRINT) && value.equalsIgnoreCase("true")) {
            this.prettyPrint = true;
        }
    }

    public void toStream(WireType desc, OutputStream out) throws IOException {
        try {
            JSerializer writer = new JSerializer(prettyPrint);
            String wt = writer.write(desc);
            out.write(wt.getBytes());
            out.flush();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public WireType fromStream(InputStream in) throws Exception {
        try {
            return JDeserializer.deserialize(in);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public String getMimeType() {
        return "application/json";
    }

    public String getFileExtension() {
        return "atticj";
    }


}
