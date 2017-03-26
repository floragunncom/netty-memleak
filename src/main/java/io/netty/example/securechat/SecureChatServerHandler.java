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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;

/**
 * Handles a server-side channel.
 */
public class SecureChatServerHandler extends SimpleChannelInboundHandler<String> {

    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Once session is secured, send a greeting and register the channel to the global channel
        // list so the channel received the messages from others.
        
        if(ctx.pipeline().get(SslHandler.class) == null) {
            System.out.println("*** NO SSL ***");
            return;
        }
        
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                new GenericFutureListener<Future<Channel>>() {
                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        System.out.println(
                                "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure chat service!");
                        System.out.println(
                                "Your session is protected by " +
                                        ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                        " cipher suite.");

                        System.out.println("Peer certificates length: "+ctx.pipeline().get(SslHandler.class).engine().getSession().getPeerCertificates().length);
                        channels.add(ctx.channel());
                    }
        });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // Send the received message to all channels but the current one.
        
        for (Channel c: channels) {
            if (c != ctx.channel()) {
                c.writeAndFlush("ack\r\n");
            } else {
                c.writeAndFlush("ack\r\n");
            }
        }
        
        //System.out.println("received: "+msg.getBytes().length);

        // Close the connection if the client has sent 'bye'.
       // if ("bye".equals(msg.toLowerCase())) {
        //   ctx.close();
        //}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if(!cause.getMessage().contains("Connection reset by peer")) {
            cause.printStackTrace();
        }
        ctx.close();
        System.out.println("Serverhandler context closed");
    }
}
