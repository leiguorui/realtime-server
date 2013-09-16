/*
 * Copyright 2013 Goodow.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.goodow.realtime.server.bootstrap;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

public class WebFE extends BusModBase implements Handler<HttpServerRequest> {
  private static final String BUNDLED_STATIC_FILES_PATH = "/static/";

  /**
   * Routes HTTP requests to the Realtime web server.
   */
  @Override
  public void handle(HttpServerRequest req) {
    String path = req.path();
    if (path.contains("..")) {
      // This is an attempt to escape the directory jail. Deny it.
      sendStatusCode(req, 404);
    } else if (path.startsWith(BUNDLED_STATIC_FILES_PATH)) {
      // This is a request for static content bundled with the client.
      req.response().sendFile("." + path);
    } else {
      // Otherwise, we don't know what you are looking for.
      sendStatusCode(req, 404);
    }
  }

  @Override
  public void start() {
    super.start();

    HttpServer server = vertx.createHttpServer();
    server.requestHandler(this);

    // Configure the SockJS event bus bridge.
    SockJSServer sjsServer = vertx.createSockJSServer(server);
    JsonArray permitted = new JsonArray().addObject(new JsonObject());
    JsonArray inboundPermitted = getOptionalArrayConfig("in_permitted", permitted);
    JsonArray outboundPermitted = getOptionalArrayConfig("out_permitted", permitted);

    sjsServer.bridge(getOptionalObjectConfig("sjs_config", new JsonObject().putString("prefix",
        "/eventbus")), inboundPermitted, outboundPermitted);

    server.listen(getOptionalIntConfig("port", 8080), getOptionalStringConfig("host", "0.0.0.0"));
  }

  private void sendStatusCode(HttpServerRequest req, int statusCode) {
    req.response().setStatusCode(statusCode);
    req.response().end();
  }

}