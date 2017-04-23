package io.netty.example.securechat;

import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.cert.Certificate;
import java.util.Random;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/*
 * 
 * 
Mac14,1
4 x 2,7 GHz Intel Core i5

OS: Mac OS X x86_64 10.10.5
Java Version: 1.8.0_131 Oracle Corporation
JVM Impl.: 25.131-b11 Oracle Corporation Java HotSpot(TM) 64-Bit Server VM
TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256
TLSv1.2
200 mbyte in 7433 ms
28 mbyte/sec
224 mbit/sec


OS: Mac OS X x86_64 10.10.5
Java Version: 1.8.0_131 Oracle Corporation
JVM Impl.: 25.131-b11 Oracle Corporation Java HotSpot(TM) 64-Bit Server VM
no ssl mode
200 mbyte in 3518 ms
66 mbyte/sec
528 mbit/sec

 * 
 * 
 * 
 */

public class PlainSSLTestMain {
    
    private static final int port = 9543;
    private static final boolean ssl = !Boolean.getBoolean("nossl");
    private static SSLContext sc;
    private static final char[] PASSWD = "123".toCharArray();

    static {
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " "
                + System.getProperty("os.version"));
        System.out.println("Java Version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor"));
        System.out.println("JVM Impl.: " + System.getProperty("java.vm.version") + " " + System.getProperty("java.vm.vendor") + " "
                + System.getProperty("java.vm.name"));
        
        if(ssl) {
        
            try {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sc = SSLContext.getInstance("TLS");
                final KeyStore ks = KeyStore.Builder.newInstance("JKS", null, new PasswordProtection(PASSWD)).getKeyStore();
                ks.setKeyEntry("1", ssc.key(),PASSWD, new Certificate[]{ssc.cert()});
                ks.setCertificateEntry("2", ssc.cert());
                final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, PASSWD);
    
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);
                sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }  
    
    
  public static void main(String[] argv) throws Exception {

    final ServerSocket ssocket = ssl?sc.getServerSocketFactory().createServerSocket(port): new ServerSocket(port);
    new CopyThread(ssocket).start();
   
    final Socket client = ssl?sc.getSocketFactory().createSocket("localhost", port):new Socket("localhost", port);
    
    if(client instanceof SSLSocket) {
        SSLSocket sslSocket = (SSLSocket) client;
        System.out.println(sslSocket.getSession().getCipherSuite());
        System.out.println(sslSocket.getSession().getProtocol());
    } else {
        System.out.println("no ssl mode");
    }

    new WriteThread(client).start();
    new ReadThread(client).start();
    
  }
  
  public static class CopyThread extends Thread {
    final ServerSocket ssocket;
    final byte[] buf = new byte[1024*4];

    public CopyThread(ServerSocket ssocket) {
        super();
        this.ssocket = ssocket;
    }

    @Override
    public void run() {
        
        try {
            final Socket socket = ssocket.accept();
            final InputStream in = socket.getInputStream();
            final OutputStream out = socket.getOutputStream();
            int read;
            while((read=in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  }
  
  public static class WriteThread extends Thread {
      final Socket socket;
      final byte[] randomData = new byte[1024*1024*1024];
     
              
    public WriteThread(Socket socket) {
        super();
        this.socket = socket;
        
    }

    @Override
    public void run() {
        try {
            final OutputStream out = socket.getOutputStream();
            long cum=0;
            while(cum < (1024*1024*200)) {
                new Random().nextBytes(randomData);
                out.write(randomData);
                cum+=randomData.length;
                
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  }
  
  public static class ReadThread extends Thread {
      final Socket socket;
      final byte[] buf = new byte[1024*4];

    public ReadThread(Socket socket) {
        super();
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            final InputStream in = socket.getInputStream();
            int read;
            long cum=0;
            final long start = System.currentTimeMillis();
            while((read=in.read(buf)) != -1 && cum < (1024*1024*200)) {
                cum+=read;
            }
            final long end = System.currentTimeMillis();
            final long dur = end-start;
            System.out.println( (cum/(1024*1024))+" mbyte in "+dur+" ms");
            final long mbsec  = ((cum/(1024*1024))/(dur/1000));
            System.out.println(mbsec+" mbyte/sec");
            System.out.println((mbsec*8)+" mbit/sec");
            Thread.sleep(100);
            System.exit(0);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  }
}