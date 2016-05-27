package io.netty.example.securechat;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

public class Main {
    
    //public final static SslProvider PROVIDER = SslProvider.JDK;
    public final static SslProvider PROVIDER = Boolean.getBoolean("memleak.disable_ossl")?SslProvider.JDK:SslProvider.OPENSSL;
    
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
        
        /*new Thread(new Runnable() {

            @Override
            public void run() {
                while(true) {
                    System.out.println("-------- Memory usage "+PROVIDER+" -------------------");
                    System.out.println("Free: "+Runtime.getRuntime().freeMemory()/(1024*1024)+"mb");
                    System.out.println("Total: "+Runtime.getRuntime().totalMemory()/(1024*1024)+"mb");
                    System.out.println(executeCommand(new String []{"/bin/sh", "-c", "ps -ef | grep -i java"}));
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        }).start();*/
        
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if(Boolean.getBoolean("memleak.v4only")) {
                        SecureChatServer.main(null);
                    } else {
                        SecureChatServer3.main(null);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
        
        Thread.sleep(2000);

        for(;;) {

            new Thread(new Runnable() {
    
                @Override
                public void run() {
                    try {
                        SecureChatClient.main(null);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }).start();
            
            Thread.sleep(2000);
        }

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
    
    private static String executeCommand(String[] command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";           
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

}
