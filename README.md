https://github.com/netty/netty/issues/5235

Run it with -Xmx4g

## If no additional properties are set
* you should see a memleak

## -Dmemleak.disable_ossl=true
* disable openssl, use the jdk provider
* no memleak

## -Dmemleak.v4only=true
* do not use netty3, only netty4
* when openssl is enabled there is also a memleak but it grows very slow

###Tested on

	OS: Mac OS X x86_64 10.10.5
	Java Version: 1.7.0_79 Oracle Corporation
	JVM Impl.: 24.79-b02 Oracle Corporation Java HotSpot(TM) 64-Bit Server VM
	Open SSL available: true
	Open SSL version: OpenSSL 1.0.2e 3 Dec 2015

	OS: Mac OS X x86_64 10.10.5
	Java Version: 1.8.0_45 Oracle Corporation
	JVM Impl.: 25.45-b02 Oracle Corporation Java HotSpot(TM) 64-Bit Server VM
	Open SSL available: true
	Open SSL version: OpenSSL 1.0.2e 3 Dec 2015
