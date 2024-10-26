package org.jetbrains.teamcity.asyncProfiler;

import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.*;
import jetbrains.buildServer.web.util.SessionUser;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class AsyncProfilerTab extends SimpleCustomTab {
  public AsyncProfilerTab(@NotNull final PagePlaces places,
                     @NotNull final PluginDescriptor descriptor) {
    super(places);
    setPluginName("asyncProfiler");
    setTabTitle("Async Profiler");
    setPlaceId(PlaceId.ADMIN_SERVER_DIAGNOSTIC_TAB);
    setIncludeUrl("/admin/diagnostics/asyncProfiler.html");
    register();
  }

  public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
    super.fillModel(model, request);
  }

  public boolean isAvailable(@NotNull HttpServletRequest request) {
    return SessionUser.getUser(request).isPermissionGrantedGlobally(Permission.MANAGE_SERVER_INSTALLATION);
  }
}