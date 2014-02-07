package com.goodow.realtime.vertx;

import com.goodow.realtime.channel.BusHook;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.core.Handler;

class VertxMessage<T> implements Message<T> {
  private final VertxBusServer bus;
  private final org.vertx.java.core.eventbus.Message<T> delegate;

  public VertxMessage(VertxBusServer bus, org.vertx.java.core.eventbus.Message<T> delegate) {
    this.bus = bus;
    this.delegate = delegate;
  }

  @Override
  public String address() {
    return delegate.address();
  }

  @Override
  public T body() {
    return delegate.body();
  }

  @Override
  public void fail(int failureCode, String msg) {
    delegate.fail(failureCode, msg);
  }

  @Override
  public void reply(Object msg) {
    reply(msg, null);
  }

  @SuppressWarnings("hiding")
  @Override
  public <T> void reply(Object msg, final Handler<Message<T>> replyHandler) {
    BusHook hook = bus.getHook();
    if (hook == null || hook.handleSendOrPub(true, replyAddress(), msg, replyHandler)) {
      org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message<T>> handler =
          replyHandler == null ? null
              : new org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message<T>>() {
                @Override
                public void handle(org.vertx.java.core.eventbus.Message<T> message) {
                  VertxMessage<T> event = new VertxMessage<T>(bus, message);
                  BusHook hook = bus.getHook();
                  if (hook == null || hook.handleReceiveMessage(event)) {
                    replyHandler.handle(event);
                  }
                }
              };
      delegate.reply(VertxBusServer.unwrap(msg), handler);
    }
  }

  @Override
  public String replyAddress() {
    return delegate.replyAddress();
  }
}