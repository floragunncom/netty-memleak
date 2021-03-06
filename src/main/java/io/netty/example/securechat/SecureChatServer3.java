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

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * Simple SSL chat server modified from {@link TelnetServer}.
 */
public final class SecureChatServer3 {

    static final int PORT = Integer.parseInt(System.getProperty("port", "8992"));
    
    public static void main(String[] args) throws Exception {
    
        System.out.println("Start netty server v3 on "+PORT+" with "+Main.PROVIDER+" provider");
        org.jboss.netty.bootstrap.ServerBootstrap bootstrap = new org.jboss.netty.bootstrap.ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Configure the pipeline factory.
        bootstrap.setPipelineFactory(new SecureChatServerPipelineFactory3());

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(PORT));
       
    }
}
