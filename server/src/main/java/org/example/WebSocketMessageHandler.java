package org.example;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.util.LinkedList;
import java.util.List;

public class WebSocketMessageHandler extends SimpleChannelInboundHandler<WebSocketFrame> {


    private static final ChannelGroup channels = new DefaultChannelGroup(null);

    public static List<String> chatHistory = new LinkedList<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        System.out.println("[LOG]: -----channelRead0---------");
        Channel incoming = ctx.channel();

        String message = "[" + incoming.remoteAddress() + "]: " + ((TextWebSocketFrame) frame).text();
        chatHistory.add(message);
        if (chatHistory.size() > 50) {
            chatHistory.removeFirst();
        }

        System.out.println("[LOG]: Start sending messages from " + incoming.remoteAddress());
        System.out.println("[LOG]: Message is: '" + message + "'");

        for (Channel channel: channels) {
            if (channel != incoming) {
                channel.writeAndFlush(new TextWebSocketFrame(message));
                System.out.println("   [LOG]: receiver: " + channel.remoteAddress());
            } else {
                System.out.println("   [LOG]: no message to: " + channel.remoteAddress());
            }
        }

        System.out.println("[LOG]: --------------\n\n");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[LOG]: -----handlerAdded---------");
        super.handlerAdded(ctx);

        Channel incoming = ctx.channel();
        String message = "[SERVER]: " + incoming.remoteAddress() + " has joined chat!";
        for (Channel channel: channels) {
            channel.write(message);
            System.out.println("[LOG]: joining server message has sent. Text: " + message + " | receiver: " + channel.remoteAddress());
        }

        channels.add(incoming);
        System.out.println("[LOG]: chat registered. remoteAddress: " + incoming.remoteAddress());
        System.out.println("[LOG]: channels amount now are: " + channels.size());

        System.out.println("[LOG]: --------------\n\n");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[LOG]: -----handlerRemoved---------");
        super.handlerRemoved(ctx);


        Channel incoming = ctx.channel();
        String message = "[SERVER]: " + incoming.remoteAddress() + " has left chat!";
        for (Channel channel: channels) {
            if (channel != incoming) {
                channel.write(message);
                System.out.println("[LOG]: lefting server message has sent. Text: " + message + " | receiver: " + channel.remoteAddress());
            }

        }

        channels.remove(incoming);
        System.out.println("[LOG]: chat deleted. remoteAddress: " + incoming.remoteAddress());
        System.out.println("[LOG]: channels amount now are: " + channels.size());

        System.out.println("[LOG]: --------------\n\n");
    }

    public static void SendHistory(Channel channel) {
        for (int i = Math.max((chatHistory.size() - 10), 0); i < chatHistory.size(); i++) {
            channel.writeAndFlush(new TextWebSocketFrame(chatHistory.get(i)));
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            //Channel upgrade to websocket, remove WebSocketIndexPageHandler.
            ctx.pipeline().remove(WebSocketConnectionHandler.class);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
