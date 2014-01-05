package com.goodow.realtime.vertx;

import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.State;
import com.goodow.realtime.core.Handler;
import com.goodow.realtime.json.impl.JreJsonArray;
import com.goodow.realtime.json.impl.JreJsonObject;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

  @SuppressWarnings("rawtypes") private final ConcurrentMap<Handler, org.vertx.java.core.Handler> handlerMap =
      new ConcurrentHashMap<Handler, org.vertx.java.core.Handler>();
  private final EventBus eb;
  private State state;

  public VertxBusServer(EventBus eventBus) {
    this.eb = eventBus;
    state = State.OPEN;
  }

  @Override
  public void close() {
    state = State.CLOSING;
    eb.close(new org.vertx.java.core.Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> ar) {
        if (ar.succeeded()) {
          state = State.CLOSED;
        } else {
          log.log(Level.SEVERE, "Failed to close EventBus", ar.cause());
        }
      }
    });
  }

  @Override
  public State getReadyState() {
    return state;
  }

  @Override
  public Bus publish(String address, Object msg) {
    eb.publish(address, unwrap(msg));
    return this;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Bus registerHandler(String address, final Handler<? extends Message> handler) {
    final org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message> vertxHandler =
        new org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message>() {
          @SuppressWarnings("unchecked")
          @Override
          public void handle(org.vertx.java.core.eventbus.Message message) {
            ((Handler<Message>) handler).handle(new VertxMessage(message));
          }
        };
    eb.registerHandler(address, vertxHandler, new org.vertx.java.core.Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> ar) {
        if (ar.succeeded()) {
          handlerMap.put(handler, vertxHandler);
        } else {
          log.log(Level.SEVERE, "Failed to register handler on event bus", ar.cause());
        }
      }
    });
    return this;
  }

  @Override
  public <T> Bus send(String address, Object msg, final Handler<Message<T>> replyHandler) {
    @SuppressWarnings("rawtypes")
    org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message> handler =
        new org.vertx.java.core.Handler<org.vertx.java.core.eventbus.Message>() {
          @SuppressWarnings("unchecked")
          @Override
          public void handle(org.vertx.java.core.eventbus.Message message) {
            replyHandler.handle(new VertxMessage<T>(message));
          }
        };
    eb.send(address, unwrap(msg), handler);
    return this;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Bus unregisterHandler(String address, final Handler<? extends Message> handler) {
    org.vertx.java.core.Handler vertxHandler = handlerMap.get(handler);
    handlerMap.remove(handler);
    if (vertxHandler != null) {
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
    return this;
  }
}