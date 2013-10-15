package com.goodow.realtime.server.bootstrap;

import com.goodow.realtime.channel.constant.Constants.Services;
import com.goodow.realtime.server.service.SaveHandler;
import com.goodow.realtime.server.service.SnapshotHandler;

import org.vertx.java.core.http.RouteMatcher;
import org.vertx.mods.web.WebServer;

public class RealtimeWebServer extends WebServer {
  @Override
  protected RouteMatcher routeMatcher() {
    RouteMatcher routeMatcher = super.routeMatcher();

    routeMatcher.get(Services.SAVE, new SaveHandler());
    routeMatcher.get(Services.SNAPSHOT, new SnapshotHandler());

    return routeMatcher;
  }
}
