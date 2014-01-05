package com.goodow.realtime.vertx;

import com.goodow.realtime.channel.Message;
import com.goodow.realtime.core.Handler;

public class VertxMessage<T> implements Message<T> {
  private final org.vertx.java.core.eventbus.Message<T> wrapped;

  public VertxMessage(org.vertx.java.core.eventbus.Message<T> message) {
    this.wrapped = message;
  }

  @Override
  public String address() {
    return wrapped.address();
  }

  @Override
  public T body() {
    return wrapped.body();
  }

  @Override
  public void fail(int failureCode, String msg) {
    wrapped.fail(failureCode, msg);
  }

  @Override
  public void reply(Object msg) {
    wrapped.reply(VertxBusServer.unwrap(msg));
  }

  @SuppressWarnings("hiding")
  @Override
  public <T> void reply(Object msg, final Handler<Message<T>> replyHandler) {
    org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message<T>> handler =
        new org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message<T>>() {
          @Override
          public void handle(org.vertx.java.core.eventbus.Message<T> message) {
            replyHandler.handle(new VertxMessage<T>(message));
          }
        };
    wrapped.reply(VertxBusServer.unwrap(msg), handler);
  }

  @Override
  public String replyAddress() {
    return wrapped.replyAddress();
  }
}