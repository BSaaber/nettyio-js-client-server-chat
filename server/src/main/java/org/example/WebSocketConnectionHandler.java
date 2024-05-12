package org.example;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;

public class WebSocketConnectionHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String websocketPath;

    private static final ChannelGroup channels = new DefaultChannelGroup(null);

    public WebSocketConnectionHandler(String websocketPath) {
        this.websocketPath = websocketPath;
    }
// TODO: rename handler classes
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        System.out.println("[LOG]: -----channelRead0---------");

        // Handle websocket upgrade request.
        if (fullHttpRequest.headers().contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)) {
            channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
            System.out.println("[LOG]: Updated websocket connection | skip");
            WebSocketMessageHandler.SendHistory(channelHandlerContext.channel());
            System.out.println("[LOG]: history sent");
            System.out.println("[LOG]: --------------\n\n");
            return;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("[LOG]: exception caught");
        cause.printStackTrace();
        ctx.close();
    }
}
