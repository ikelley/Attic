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

package org.atticfs.roleservices.protocol;

import org.atticfs.protocol.AtticProtocol;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class Description Here...
 *
 * 
 */

public class StreamTest {


    public static void main(String[] args) {
        AtticProtocol.registerAttic();
        if (args.length < 2) {
            System.out.println("Usage StreamTest <attic-url> <output-file> [<hash>]");
            System.exit(0);
        }

        try {
            URL url = new URL(args[0]);
            File file = new File(args[1]);
            String hash = null;
            if (args.length > 2) {
                hash = args[2];
            }
            InputStream in = url.openStream();
            FileOutputStream fout = new FileOutputStream(file);
            byte[] bytes = new byte[8192];
            int c;
            while ((c = in.read(bytes)) != -1) {
                fout.write(bytes, 0, c);
            }
            fout.flush();
            fout.close();
            in.close();
            if (hash != null) {
                boolean b = verify(new FileInputStream(file), hash);
                System.out.println("StreamTest.main verified file? " + b);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Boolean verify(InputStream in, String hash) {
        if (hash == null) {
            return true;
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        int len = 8192;
        try {
            int c;
            byte[] bytes = new byte[len];
            while ((c = in.read(bytes)) != -1) {
                md.update(bytes, 0, c);
            }
            byte[] digest = md.digest();
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                buf.append(Integer.toHexString((int) digest[i] & 0x00FF));
            }
            boolean b = buf.toString().equals(hash);


            return b;
        } catch (IOException e) {
            return null;
        }

    }
}
