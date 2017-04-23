Simple SSL PerfTest

## Run with SSL
* java -Xmx4g -jar target/simpleperf-1.0-jar-with-dependencies.jar

## Run without SSL
* java -Xmx4g -Dnossl=true -jar target/simpleperf-1.0-jar-with-dependencies.jar



### Tested on
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