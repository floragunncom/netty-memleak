package io.netty.example.securechat;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

public class Main {
    
   //public final static SslProvider PROVIDER = SslProvider.OPENSSL;
    public final static SslProvider PROVIDER = SslProvider.JDK;
    
    public static SslContext SERVER_SSL_CTX = null;
    
    public static SSLEngine createServerEngine() {
       System.out.println("create new "+PROVIDER+" server engine");
       return SERVER_SSL_CTX.newEngine(PooledByteBufAllocator.DEFAULT);
    }

    public static void main(String[] args) throws Exception {
        
        try {
            File[] pemks = SSLCertificateHelper.jksKsToPem("node-0-keystore.jks");
            File pemts = SSLCertificateHelper.jksTsToPem("truststore.jks");
         
            SERVER_SSL_CTX = SslContextBuilder
                    .forServer(pemks[0], pemks[1])
                    //.ciphers(getEnabledSSLCiphers())
                     .applicationProtocolConfig(ApplicationProtocolConfig.DISABLED)
                    .clientAuth(ClientAuth.REQUIRE)
                    .sessionCacheSize(0)
                    .sessionTimeout(0)
                    .sslProvider(Main.PROVIDER)
                    .trustManager(pemts)
                    .build();
        } catch (SSLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " "
                + System.getProperty("os.version"));
        System.out.println("Java Version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor"));
        System.out.println("JVM Impl.: " + System.getProperty("java.vm.version") + " " + System.getProperty("java.vm.vendor") + " "
                + System.getProperty("java.vm.name"));
        System.out.println("Open SSL available: "+OpenSsl.isAvailable());
        System.out.println("Open SSL version: "+OpenSsl.versionString());
        System.out.println("Max memory: "+Runtime.getRuntime().maxMemory()/(1024*1024)+"mb");
        System.out.println("Starttime: "+new Date());
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
        
        Thread.sleep(2000);

       // for(;;) {

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
            System.out.println("ce");
            SecureChatServer.kill();
            s.interrupt();
            System.out.println("Endtime: "+new Date());
            long end = System.currentTimeMillis();
            System.out.println("Took "+(end-start)+" ms");
        //    Thread.sleep(2000);
      //  }

    }

    static File getAbsoluteFilePathFromClassPath(final String fileNameFromClasspath) {
        File file = null;
        final URL fileUrl = Main.class.getClassLoader().getResource(fileNameFromClasspath);
        if (fileUrl != null) {
            try {
                file = new File(URLDecoder.decode(fileUrl.getFile(), "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
                return null;
            }

            if (file.exists() && file.canRead()) {
                return file;
            } else {
                System.out.println("Cannot read from {}, maybe the file does not exists? " + file.getAbsolutePath());
            }

        } else {
            System.out.println("Failed to load " + fileNameFromClasspath);
        }
        return null;
    }

}
