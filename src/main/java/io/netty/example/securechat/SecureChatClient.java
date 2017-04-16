/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.securechat;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SecureChatClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8992"));
    public static final int rounds = Integer.parseInt(System.getProperty("rounds", "3"));
    private final static ByteBufAllocator ba = new UnpooledByteBufAllocator(true);
    private final static ByteBuf LARGE_DATA = ba.buffer();
    public final static int datasize;
    public final static List<String> ciphers = null;//new ArrayList<>();

    private static SslContext sslCtx = null;
    static {
        //ciphers.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        double data = Double.parseDouble(System.getProperty("data", "12"));
        datasize = (int) (data*1024*1024);
        byte[] b = new byte[datasize];
        new Random().nextBytes(b);
        
        LARGE_DATA.writeBytes(b);
        b=null;
        System.out.println("Data size " + (datasize / (1024)) + " kb");

        
        
        
        try {
            sslCtx = SslContextBuilder.forClient().applicationProtocolConfig(ApplicationProtocolConfig.DISABLED).sessionCacheSize(0)
                    .sessionTimeout(0).sslProvider(Main.PROVIDER).trustManager(Main.sc.cert()).keyManager(Main.sc.key(), Main.sc.cert())
                    .ciphers(ciphers)
                    .protocols("TLSv1")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new SecureChatClientInitializer(sslCtx))
            .option(ChannelOption.ALLOCATOR, System.getProperty("unpooled") == null ? PooledByteBufAllocator.DEFAULT : UnpooledByteBufAllocator.DEFAULT);

            // Start the connection attempt.
            Channel ch = b.connect(HOST, PORT).sync().channel();

            // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            
            System.out.println("Rounds " + rounds);
            for (int i = 0; i < rounds; i++) {
                lastWriteFuture = ch.writeAndFlush(LARGE_DATA.copy()).sync();
            }

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }

        } finally {
            // The connection is closed automatically on shutdown.
            group.shutdownGracefully();
        }
    }
}
