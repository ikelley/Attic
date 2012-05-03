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

package org.atticfs.key;

import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class KeyUtils {

    static Logger log = Logger.getLogger("org.atticfs.key.KeyUtils");


    private static Random r = new Random();

    private static String[] HEX = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static final String providerName = "BC";

    static {
        Security.addProvider(new BouncyCastleProvider());
        Provider[] prs = Security.getProviders();
        for (Provider pr : prs) {
            log.fine("Available security provider name:" + pr.getName());
        }
    }

    public static BigInteger randomHexInteger(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(HEX[r.nextInt(16)]);
        }
        return new BigInteger(sb.toString(), 16);
    }


    public static X509Certificate createRootCertificate(KeyPair pair, String dn, int days) throws Exception {
        Date startDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        Date expiryDate = cal.getTime();
        BigInteger serialNumber = randomHexInteger(64);

        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
        X500Principal dnName = new X500Principal(dn);

        certGen.setSerialNumber(serialNumber);
        certGen.setIssuerDN(dnName);
        certGen.setNotBefore(startDate);
        certGen.setNotAfter(expiryDate);
        certGen.setSubjectDN(dnName); // note: same as issuer
        certGen.setPublicKey(pair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        X509Certificate cert = certGen.generate(pair.getPrivate(), providerName);
        return cert;
    }

    public static X509Certificate createSignedCertificate(KeyPair keyPair, PrivateKey caKey, X509Certificate caCert, String dn, int days) throws Exception {
        Date startDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);

        Date expiryDate = cal.getTime();
        BigInteger serialNumber = randomHexInteger(64);

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        X500Principal subjectName = new X500Principal(dn);

        certGen.setSerialNumber(serialNumber);
        certGen.setIssuerDN(caCert.getSubjectX500Principal());
        certGen.setNotBefore(startDate);
        certGen.setNotAfter(expiryDate);

        certGen.setSubjectDN(subjectName);
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
                new AuthorityKeyIdentifierStructure(caCert));
        certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
                new SubjectKeyIdentifierStructure(keyPair.getPublic()));

        X509Certificate cert = certGen.generate(caKey, providerName);   // note: private key of CA
        return cert;
    }


    public static KeyPair generateKeyPair(int keySize) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", providerName);
        generator.initialize(keySize);
        return generator.generateKeyPair();
    }

    public static KeyPair generateKeyPair() throws Exception {
        return generateKeyPair(1024);
    }

    public static byte[] getPrivateKeyAsDER(File pemFile) throws Exception {
        PrivateKey pk = readPrivateKeyFromPEM(pemFile);
        return pk.getEncoded();
    }

    public static byte[] getPublicKeyAsDER(File pemFile) throws Exception {
        PublicKey pk = readPublicKeyFromPEM(pemFile);
        return pk.getEncoded();
    }

    public static byte[] getCertificateAsDER(File pemFile) throws Exception {
        X509Certificate pk = readCertificateFromPEM(pemFile);
        return pk.getEncoded();
    }

    public static Object readFromPEM(File file) throws Exception {
        FileReader fr = new FileReader(file);
        PEMReader reader = new PEMReader(fr);
        Object ret = reader.readObject();
        reader.close();
        fr.close();
        return ret;
    }

    public static KeyPair readKeyPairFromPEM(File file) throws Exception {
        return (KeyPair) readFromPEM(file);
    }

    public static PrivateKey readPrivateKeyFromPEM(File file) throws Exception {
        return readKeyPairFromPEM(file).getPrivate();
    }

    public static PublicKey readPublicKeyFromPEM(File file) throws Exception {
        return readKeyPairFromPEM(file).getPublic();
    }

    public static X509Certificate readCertificateFromPEM(File file) throws Exception {
        return (X509Certificate) readFromPEM(file);
    }

    /**
     * creates or adds to a keystore
     *
     * @param cert
     * @param key
     * @param keystore
     * @return
     */
    public static File importToKeystore(File cert, File key, File keystore, String alias, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        if (keystore == null || !keystore.exists() || keystore.length() == 0) {
            keyStore.load(null, null);
        } else {
            FileInputStream fin = new FileInputStream(keystore);
            keyStore.load(fin, null);
        }
        byte[] certDer = getCertificateAsDER(cert);
        ByteArrayInputStream bin = new ByteArrayInputStream(certDer);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        Collection<? extends Certificate> cs = certificateFactory.generateCertificates(bin);
        java.security.cert.Certificate[] certs = cs.toArray(new java.security.cert.Certificate[cs.size()]);
        byte[] keyDer = getPrivateKeyAsDER(key);
        KeyFactory rSAKeyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = rSAKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyDer));
        keyStore.setKeyEntry(alias, privateKey, password.toCharArray(), certs);
        FileOutputStream keyStoreOutputStream = new FileOutputStream(keystore);
        keyStore.store(keyStoreOutputStream, password.toCharArray());
        keyStoreOutputStream.close();
        return keystore;
    }

    public static File importToKeystore(File cert, File keystore, String alias, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        if (keystore == null || !keystore.exists() || keystore.length() == 0) {
            keyStore.load(null, null);
        } else {
            FileInputStream fin = new FileInputStream(keystore);
            keyStore.load(fin, null);
        }
        byte[] certDer = getCertificateAsDER(cert);
        ByteArrayInputStream bin = new ByteArrayInputStream(certDer);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        java.security.cert.Certificate c = certificateFactory.generateCertificate(bin);

        keyStore.setCertificateEntry(alias, c);
        FileOutputStream keyStoreOutputStream = new FileOutputStream(keystore);
        keyStore.store(keyStoreOutputStream, password.toCharArray());
        keyStoreOutputStream.close();
        return keystore;
    }

    public static void writeAsPEM(X509Certificate cert, Writer out) throws IOException {
        PEMWriter pemWriter = new PEMWriter(out, providerName);
        pemWriter.writeObject(cert);
        pemWriter.close();
    }

    public static void writeAsPEM(PrivateKey key, Writer out) throws IOException {
        PEMWriter pemWriter = new PEMWriter(out, providerName);
        pemWriter.writeObject(key);
        pemWriter.close();
    }

    public static void writeAsPEM(PublicKey key, Writer out) throws IOException {
        PEMWriter pemWriter = new PEMWriter(out, providerName);
        pemWriter.writeObject(key);
        pemWriter.close();
    }

    public static void writeAsPEM(KeyPair pair, String name, File outDir) throws IOException {
        outDir.mkdirs();
        FileWriter writer = new FileWriter(new File(outDir, name));
        writeAsPEM(pair.getPrivate(), writer);
        writer.close();
        writer = new FileWriter(new File(outDir, name + ".pub"));
        writeAsPEM(pair.getPublic(), writer);
        writer.close();
    }

    public static X509Certificate createRoot(String dn, String name, File outDir) throws Exception {
        outDir.mkdirs();
        KeyPair pair = KeyUtils.generateKeyPair();
        writeAsPEM(pair, name, outDir);
        X509Certificate cert = KeyUtils.createRootCertificate(pair, dn, 365);
        FileWriter fout = new FileWriter(new File(outDir, name + ".cert"));
        writeAsPEM(cert, fout);
        fout.close();
        return cert;
    }

    public static X509Certificate createSigned(String dn, String name, File outDir, X509Certificate cacert, PrivateKey caPriv) throws Exception {
        outDir.mkdirs();
        KeyPair pair = KeyUtils.generateKeyPair();
        writeAsPEM(pair, name, outDir);
        X509Certificate cert = KeyUtils.createSignedCertificate(pair, caPriv, cacert, dn, 365);
        FileWriter fout = new FileWriter(new File(outDir, name + ".cert"));
        writeAsPEM(cert, fout);
        fout.close();
        return cert;
    }

    public static X509Certificate createSigned(String dn, String name, File outDir, File cacert, File cakey) throws Exception {
        outDir.mkdirs();
        KeyPair pair = KeyUtils.generateKeyPair();
        writeAsPEM(pair, name, outDir);
        X509Certificate cs = readCertificateFromPEM(cacert);
        PrivateKey key = readPrivateKeyFromPEM(cakey);
        X509Certificate cert = KeyUtils.createSignedCertificate(pair, key, cs, dn, 365);
        FileWriter fout = new FileWriter(new File(outDir, name + ".cert"));
        writeAsPEM(cert, fout);
        fout.close();
        return cert;
    }


    public static void main(String[] args) throws Exception {
        Dn dn = new Dn();
        dn.setCommonName("Attic CA")
                .setOrganization("Cardiff University")
                .setOrganizationalUnit("COMSC")
                .setLocality("Cardiff")
                .setProvince("Wales")
                .setCountry("UK");
        createRoot(dn.toString(), "attic-ca", new File("attic-keys/attic-ca"));

        X509Certificate cacert = readCertificateFromPEM(new File("attic-keys/attic-ca/attic-ca.cert"));
        PrivateKey cakey = readPrivateKeyFromPEM(new File("attic-keys/attic-ca/attic-ca"));

        dn.setCommonName("mail.kingsgatedental.com");
        createSigned(dn.toString(), "attic-mail", new File("attic-keys/attic-mail"), cacert, cakey);

        dn.setCommonName("d220.cs.cf.ac.uk");
        createSigned(dn.toString(), "attic-seed", new File("attic-keys/attic-seed"), cacert, cakey);

        dn.setCommonName("voldemort.cs.cf.ac.uk");
        createSigned(dn.toString(), "attic-dls", new File("attic-keys/attic-dls"), cacert, cakey);

        dn.setCommonName("electricline.cs.cf.ac.uk");
        createSigned(dn.toString(), "attic-dc1", new File("attic-keys/attic-dc1"), cacert, cakey);

        dn.setCommonName("kitt.cs.cf.ac.uk");
        createSigned(dn.toString(), "attic-dc2", new File("attic-keys/attic-dc2"), cacert, cakey);

        dn.setCommonName("attic Worker");
        createSigned(dn.toString(), "attic-worker", new File("attic-keys/attic-worker"), cacert, cakey);

        dn.setCommonName("attic Admin");
        createSigned(dn.toString(), "attic-admin", new File("attic-keys/attic-admin"), cacert, cakey);

        File keystores = new File("attic-keys/keystores");
        keystores.mkdirs();

        File trust = importToKeystore(new File("attic-keys/attic-ca/attic-ca.cert"),
                new File(keystores, "trust.keystore"),
                "ca", "ca");

        File seedKey = importToKeystore(new File("attic-keys/attic-seed/attic-seed.cert"),
                new File("attic-keys/attic-seed/attic-seed"),
                new File(keystores, "seed-key.keystore"),
                "seed", "seed");

        File dlsKey = importToKeystore(new File("attic-keys/attic-dls/attic-dls.cert"),
                new File("attic-keys/attic-dls/attic-dls"),
                new File(keystores, "dls-key.keystore"),
                "dls", "dls");

        File dc1Key = importToKeystore(new File("attic-keys/attic-dc1/attic-dc1.cert"),
                new File("attic-keys/attic-dc1/attic-dc1"),
                new File(keystores, "dc1-key.keystore"),
                "dc1", "dc1");

        File dc2Key = importToKeystore(new File("attic-keys/attic-dc2/attic-dc2.cert"),
                new File("attic-keys/attic-dc2/attic-dc2"),
                new File(keystores, "dc2-key.keystore"),
                "dc2", "dc2");

        File workerKey = importToKeystore(new File("attic-keys/attic-worker/attic-worker.cert"),
                new File("attic-keys/attic-worker/attic-worker"),
                new File(keystores, "worker-key.keystore"),
                "worker", "worker");

        File adminKey = importToKeystore(new File("attic-keys/attic-admin/attic-admin.cert"),
                new File("attic-keys/attic-admin/attic-admin"),
                new File(keystores, "admin-key.keystore"),
                "admin", "admin");

        File mailKey = importToKeystore(new File("attic-keys/attic-mail/attic-mail.cert"),
                new File("attic-keys/attic-mail/attic-mail"),
                new File(keystores, "mail-key.keystore"),
                "mail", "mail");

    }

}
