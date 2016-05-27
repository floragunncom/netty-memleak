package io.netty.example.securechat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

public class SSLCertificateHelper {

    public static int exportCertificateChain(final KeyStore ks, final String alias, final File saveTo) throws KeyStoreException,
            IOException, CertificateEncodingException {
        final Enumeration<String> e = ks.aliases();
        final List<String> aliases = new ArrayList<>();

        while (e.hasMoreElements()) {
            aliases.add(e.nextElement());
        }

        String evaluatedAlias = alias;

        if (alias == null && aliases.size() == 1) {
            evaluatedAlias = aliases.get(0);
        }

        if (evaluatedAlias == null) {
            throw new KeyStoreException("null alias, current aliases: " + aliases);
        }

        try (FileWriter fw = new FileWriter(saveTo)) {
            Certificate[] certs = ks.getCertificateChain(evaluatedAlias);

            if (certs == null) {

                certs = new Certificate[] { ks.getCertificate(evaluatedAlias) };
                if (certs == null) {

                    throw new KeyStoreException("no certificate chain or certificate with alias named " + evaluatedAlias);
                }
            }

            for (int i = 0; i < certs.length; i++) {
                final Certificate certificate = certs[i];
                if (certificate == null) {
                    continue;
                }
                fw.write("-----BEGIN CERTIFICATE-----" + System.lineSeparator());
                fw.write(DatatypeConverter.printBase64Binary(certificate.getEncoded()) + System.lineSeparator());
                fw.write("-----END CERTIFICATE-----" + System.lineSeparator());
            }

            return certs.length;
        }
    }

    public static void exportDecryptedKey(final KeyStore ks, final String alias, final char[] password, final File saveTo)
            throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException {
        final Enumeration<String> e = ks.aliases();
        final List<String> aliases = new ArrayList<>();

        while (e.hasMoreElements()) {
            aliases.add(e.nextElement());
        }

        String evaluatedAlias = alias;

        if (alias == null && aliases.size() == 1) {
            evaluatedAlias = aliases.get(0);
        }

        if (evaluatedAlias == null) {
            throw new KeyStoreException("null alias");
        }

        try (FileWriter fw = new FileWriter(saveTo)) {
            final Key key = ks.getKey(evaluatedAlias, password);

            if (key == null) {
                throw new KeyStoreException("no key alias named " + evaluatedAlias);
            }

            fw.write("-----BEGIN PRIVATE KEY-----" + System.lineSeparator());
            fw.write(DatatypeConverter.printBase64Binary(key.getEncoded()) + System.lineSeparator());
            fw.write("-----END PRIVATE KEY-----");
        }
    }

    public static File[] jksKsToPem(String res) throws Exception{
        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(Main.getAbsoluteFilePathFromClassPath(res)), "changeit".toCharArray());

        File transportKeystoreCert = File.createTempFile("sg_", ".pem");
        File transportKeystoreKey = File.createTempFile("sg_", ".pem");
        SSLCertificateHelper.exportCertificateChain(ks, null, transportKeystoreCert);
        SSLCertificateHelper.exportDecryptedKey(ks, null, "changeit".toCharArray(), transportKeystoreKey);
        transportKeystoreCert.deleteOnExit();
        transportKeystoreKey.deleteOnExit();
        return new File[]{transportKeystoreCert, transportKeystoreKey};
    }
    
    public static File jksTsToPem(String res) throws Exception{
        final KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream(Main.getAbsoluteFilePathFromClassPath(res)), "changeit".toCharArray());

        File trustedTransportCertificates = File.createTempFile("sg_", ".pem");
        trustedTransportCertificates.deleteOnExit();

        SSLCertificateHelper.exportCertificateChain(ts, null, trustedTransportCertificates);
        return trustedTransportCertificates;

    }
}
