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

package org.atticfs.ser;

import org.atticfs.types.WireType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class Description Here...
 *
 * 
 */
public interface Serializer {

    public static final String PRETTY_PRINT = "org.atticfs.ser.Serializer.pretty.print";

    public void setFeature(String feature, String value);

    public void toStream(WireType desc, OutputStream out) throws Exception;

    public WireType fromStream(InputStream in) throws Exception;

    public String getMimeType();

    /**
     * the file extension for this serializer, if it writes content to file.
     * This allows wire types written to file be a serializer can be mapped back the the serializer
     * that was used to write the file.
     * <p/>
     * The convention is to append a letter signifying the serializer to the string 'attic';
     *
     * @return
     */
    public String getFileExtension();


}
