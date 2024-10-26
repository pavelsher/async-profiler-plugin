package org.jetbrains.teamcity.asyncProfiler;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.FormUtil;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AsyncProfilerController extends BaseController {
  private final ServerPaths myServerPaths;
  private final PluginDescriptor myPluginDescriptor;

  public AsyncProfilerController(@NotNull WebControllerManager controllerManager,
                                 @NotNull ServerPaths serverPaths,
                                 @NotNull PluginDescriptor pluginDescriptor) {
    controllerManager.registerController("/admin/diagnostics/asyncProfiler.html", this);
    myServerPaths = serverPaths;
    myPluginDescriptor = pluginDescriptor;
  }

  @Nullable
  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
    ProfilerSettingsBean settingsBean = FormUtil.getOrCreateForm(httpServletRequest, ProfilerSettingsBean.class, r -> new ProfilerSettingsBean(myServerPaths));
    ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("profiler.jsp"));
    mv.getModel().put("settingsBean", settingsBean);
    return mv;
  }
}
