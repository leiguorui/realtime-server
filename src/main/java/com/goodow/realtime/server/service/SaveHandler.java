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
package com.goodow.realtime.server.service;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.WebSocket;

import java.io.IOException;

public class SaveHandler implements Handler<HttpServerRequest> {

  public static void main(String[] args) throws IOException {
    VertxFactory.newVertx().createHttpClient().setPort(8080).connectWebsocket(
        "/eventbus/websocket", new Handler<WebSocket>() {

          @Override
          public void handle(WebSocket ws) {
            ws.dataHandler(new Handler<Buffer>() {

              @Override
              public void handle(Buffer data) {
                System.out.println("Received " + data);
              }
            });
            // Send some data
            ws.writeTextFrame("hello world");
          }
        });
    // Prevent the JVM from exiting
    System.in.read();
  }

  @Override
  public void handle(HttpServerRequest req) {
    // String id = req.params().get(Params.ID);
  }
}
