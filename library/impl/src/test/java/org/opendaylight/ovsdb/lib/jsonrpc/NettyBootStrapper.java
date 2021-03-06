/*
 * Copyright (C) 2013 EBay Software Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.ovsdb.lib.jsonrpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.NotImplementedException;

public class NettyBootStrapper {

    EventLoopGroup bossGroup = null;
    EventLoopGroup workerGroup = null;
    ChannelFuture channelFuture = null;

    public ChannelFuture startServer(int localPort, final ChannelHandler... handlers) throws Exception {
        // Configure the server.
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .localAddress(localPort)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        for (ChannelHandler handler : handlers) {
                            ch.pipeline().addLast(handler);
                        }
                    }
                });

        // Start the server.
        channelFuture = serverBootstrap.bind().sync();
        return channelFuture;
    }

    public void stopServer() throws InterruptedException {
        try {
            ChannelFuture channelCloseFuture = channelFuture.channel().closeFuture();
            channelCloseFuture.get(1000, TimeUnit.MILLISECONDS);
            if (!channelCloseFuture.isDone()) {
                channelCloseFuture.channel().unsafe().closeForcibly();
            }

            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            // Wait until all threads are terminated.
            bossGroup.terminationFuture().sync();
            workerGroup.terminationFuture().sync();
        } catch (ExecutionException | TimeoutException e) {
            //ignore
        }
    }

    public int getServerPort() {
        SocketAddress socketAddress = channelFuture.channel().localAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            return inetSocketAddress.getPort();
        } else {
            throw new NotImplementedException("Please implement how to obtain port from a " + socketAddress.toString());
        }
    }

}
