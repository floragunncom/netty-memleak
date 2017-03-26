Slow netty SSL

## If no additional properties are set
* java -Xmx4g -jar target/netty-sslslow-1.0-jar-with-dependencies.jar
* runs with ssl and approx 20 mb data (on my machine tooks around 1200 ms, with java 1.8.0_45 its around 10000 ms !)

## -Dnossl
* java -Xmx4g -Dnossl -jar target/netty-sslslow-1.0-jar-with-dependencies.jar
* runs without ssl and approx 20 mb data (on my machine tooks around 300-400 ms)

## with OpenSSL
* java -Xmx4g -Dopenssl -jar target/netty-sslslow-1.0-jar-with-dependencies.jar
* fails with io.netty.util.internal.OutOfDirectMemoryError: failed to allocate ...

With -Ddata=3 you can set the amount of data to 3 megabyte for the test


### Tested on
	OS: Mac OS X x86_64 10.10.5
    Java Version: 1.8.0_102 Oracle Corporation
    JVM Impl.: 25.102-b14 Oracle Corporation Java HotSpot(TM) 64-Bit Server VM
    OpenSSL 1.0.2h
    Netty 4.1.9
    tcnative: 2.0.0.Final
