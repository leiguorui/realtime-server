package com.goodow.realtime.vertx;

import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.BusHook;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.State;
import com.goodow.realtime.core.Handler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.impl.JreJsonArray;
import com.goodow.realtime.json.impl.JreJsonObject;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VertxBusServer implements Bus {
  private static final Logger log = Logger.getLogger(VertxBusServer.class.getName());

  static Object unwrap(Object msg) {
    if (msg instanceof JreJsonObject) {
      return new JsonObject(((JreJsonObject) msg).toNative());
    } else if (msg instanceof JreJsonArray) {
      return new JsonArray(((JreJsonArray) msg).toNative());
    } else {
      return msg;
    }
  }

  private final EventBus eb;
  private State state;
  private BusHook hook;

  public VertxBusServer(EventBus eb) {
    this.eb = eb;
    state = State.OPEN;
  }

  @Override
  public void close() {
    if (hook == null || hook.handlePreClose()) {
      state = State.CLOSING;
      eb.close(new org.vertx.java.core.Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> ar) {
          if (ar.succeeded()) {
            state = State.CLOSED;
            if (hook != null) {
              hook.handlePostClose();
            }
          } else {
            log.log(Level.SEVERE, "Failed to close EventBus", ar.cause());
          }
        }
      });
    }
  }

  @Override
  public State getReadyState() {
    return state;
  }

  @Override
  public VertxBusServer publish(String address, Object msg) {
    if (hook == null || hook.handleSendOrPub(false, address, msg, null)) {
      eb.publish(address, unwrap(msg));
    }
    return this;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public HandlerRegistration registerHandler(final String address,
      final Handler<? extends Message> handler) {
    if (hook != null && !hook.handlePreRegister(address, handler)) {
      return HandlerRegistration.EMPTY;
    }
    final org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message> vertxHandler =
        new org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message>() {
          @SuppressWarnings("unchecked")
          @Override
          public void handle(org.vertx.java.core.eventbus.Message message) {
            VertxMessage event = new VertxMessage(VertxBusServer.this, message);
            if (hook == null || hook.handleReceiveMessage(event)) {
              ((Handler<Message>) handler).handle(event);
            }
          }
        };
    eb.registerHandler(address, vertxHandler, new org.vertx.java.core.Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> ar) {
        if (ar.failed()) {
          log.log(Level.SEVERE, "Failed to register handler on event bus", ar.cause());
        }
      }
    });
    return new HandlerRegistration() {
      @Override
      public void unregisterHandler() {
        if (hook == null || hook.handleUnregister(address)) {
          eb.unregisterHandler(address, vertxHandler,
              new org.vertx.java.core.Handler<AsyncResult<Void>>() {
                @Override
                public void handle(AsyncResult<Void> ar) {
                  if (ar.failed()) {
                    log.log(Level.SEVERE, "Failed to unregister handler on event bus", ar.cause());
                  }
                }
              });
        }
      }
    };
  }

  @Override
  public <T> VertxBusServer send(String address, Object msg, final Handler<Message<T>> replyHandler) {
    if (hook == null || hook.handleSendOrPub(true, address, msg, replyHandler)) {
      @SuppressWarnings("rawtypes")
      org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message> handler =
          replyHandler == null ? null
              : new org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message>() {
                @SuppressWarnings("unchecked")
                @Override
                public void handle(org.vertx.java.core.eventbus.Message message) {
                  VertxMessage<T> event = new VertxMessage<T>(VertxBusServer.this, message);
                  if (hook == null || hook.handleReceiveMessage(event)) {
                    replyHandler.handle(event);
                  }
                }
              };
      eb.send(address, unwrap(msg), handler);
    }
    return this;
  }

  @Override
  public VertxBusServer setHook(BusHook hook) {
    this.hook = hook;
    if (hook != null && state == State.OPEN) {
      hook.handleOpened();
    }
    return this;
  }

  BusHook getHook() {
    return hook;
  }
}