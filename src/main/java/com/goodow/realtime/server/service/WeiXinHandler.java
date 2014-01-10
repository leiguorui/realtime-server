package com.goodow.realtime.server.service;

import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.json.JsonObject;
import com.goodow.realtime.json.impl.xml.Xml;

import com.google.inject.Inject;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.Date;
import java.util.logging.Logger;

public class WeiXinHandler implements Handler<HttpServerRequest> {
  private static final Logger log = Logger.getLogger(WeiXinHandler.class.getName());
  @Inject private Vertx vertx;
  @Inject private Bus bus;

  @Override
  public void handle(HttpServerRequest req) {
    switch (req.method()) {
      case "GET":
        req.response().end(req.params().get("echostr"));
        break;
      case "POST":
        post(req);
        break;
    }
  }

  private void post(final HttpServerRequest req) {
    req.bodyHandler(new Handler<Buffer>() {
      @Override
      public void handle(Buffer body) {
        String string = body.toString();
        JsonObject msg = Xml.parse(string);
        bus.publish("weixin", msg);

        String from = msg.getString("FromUserName");
        msg.set("FromUserName", msg.getString("ToUserName")).set("ToUserName", from).set("MsgType",
            "text").set("Content", "hi, " + msg.getString("Content")).remove("MsgId").set(
            "CreateTime", "" + new Date().getTime());

        String xml = Xml.toXml(msg);
        req.response().end(xml);
      }
    });
  }
}