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
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public final class SecureChatClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8992"));
    public static final String LARGE_DATA;

    private static SslContext sslCtx = null;
    static {

        int data = Integer.parseInt(System.getProperty("data", "20"));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (4100 * data); i++) { // 16000 = 3mb, 32000 = 7mb
            sb.append("AAAAAABBBBBBBBBCCCCCCCCCCCCCCDDDDDDDDDDDDDDEEEEEEEEEEEEEEEEEEEEEFFFFFFFFFFFFFFFFFFFF");
            sb.append("AAAAAABBBBBBBBBCCCCCCCCCCCCCCDDDDDDDDDDDDDDEEEEEEEEEEEEEEEEEEEEEFFFFFFFFFFFFFFFFFFFF");
            sb.append("AAAAAABBBBBBBBBCCCCCCCCCCCCCCDDDDDDDDDDDDDDEEEEEEEEEEEEEEEEEEEEEFFFFFFFFFFFFFFFFFFFF");
        }
        LARGE_DATA = sb.toString();
        System.out.println("Data size " + (LARGE_DATA.getBytes().length / (1024 * 1024)) + " mb");

        try {
            sslCtx = SslContextBuilder.forClient().applicationProtocolConfig(ApplicationProtocolConfig.DISABLED).sessionCacheSize(0)
                    .sessionTimeout(0).sslProvider(Main.PROVIDER).trustManager(Main.sc.cert()).keyManager(Main.sc.key(), Main.sc.cert())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Start netty v4 client with " + Main.PROVIDER + " provider");

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new SecureChatClientInitializer(sslCtx));

            // Start the connection attempt.
            Channel ch = b.connect(HOST, PORT).sync().channel();

            // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            int rounds = 1;
            System.out.println("rounds " + rounds);
            for (int i = 0; i < rounds; i++) {
                String line = LARGE_DATA;

                lastWriteFuture = ch.writeAndFlush(line);

                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                if ("bye".equals(line.toLowerCase())) {
                    ch.closeFuture().sync();
                    break;
                }
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
