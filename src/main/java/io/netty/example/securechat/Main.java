package io.netty.example.securechat;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.security.cert.CertificateException;
import java.util.Date;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

public class Main {

    public static SslProvider PROVIDER;

    public static SelfSignedCertificate sc;
    public static SslContext SERVER_SSL_CTX = null;

    static {
        try {
            sc = new SelfSignedCertificate();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
    }

    public static SSLEngine createServerEngine() {
        System.out.println("create new " + PROVIDER + " server engine");
        return SERVER_SSL_CTX.newEngine(PooledByteBufAllocator.DEFAULT);
    }

    public static void main(String[] args) throws Exception {

        PROVIDER = System.getProperty("openssl") != null ? SslProvider.OPENSSL : SslProvider.JDK;

        try {
            SERVER_SSL_CTX = SslContextBuilder.forServer(sc.key(), sc.cert()).applicationProtocolConfig(ApplicationProtocolConfig.DISABLED)
                    .clientAuth(ClientAuth.REQUIRE).sslProvider(Main.PROVIDER).trustManager(sc.cert()).build();
        } catch (SSLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " "
                + System.getProperty("os.version"));
        System.out.println("Java Version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor"));
        System.out.println("JVM Impl.: " + System.getProperty("java.vm.version") + " " + System.getProperty("java.vm.vendor") + " "
                + System.getProperty("java.vm.name"));
        System.out.println("Open SSL available: " + OpenSsl.isAvailable());
        System.out.println("Open SSL version: " + OpenSsl.versionString());
        System.out.println("Max memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "mb");
        System.out.println("Starttime: " + new Date());
        long start = System.currentTimeMillis();

        Thread s = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    SecureChatServer.main(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        s.start();

        // Thread.sleep(2000);

        Thread c = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    SecureChatClient.main(null);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        c.start();
        c.join();
        long end = System.currentTimeMillis();
        System.out.println("Endtime: " + new Date());
        System.out.println("Took " + (end - start) + " ms");
        SecureChatServer.kill();
        s.interrupt();
    }
}
