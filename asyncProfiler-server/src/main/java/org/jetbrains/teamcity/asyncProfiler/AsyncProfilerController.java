package org.jetbrains.teamcity.asyncProfiler;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.FormUtil;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.impl.auth.ServerAuthUtil;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
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
  protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse httpServletResponse) {
    SUser user = SessionUser.getUser(request);
    if (!user.isPermissionGrantedGlobally(Permission.MANAGE_SERVER_INSTALLATION)) {
      throw new AccessDeniedException(user, "You do not have permissions to access this page");
    }

    ProfilerSettingsBean settingsBean = FormUtil.getOrCreateForm(request, ProfilerSettingsBean.class, r -> new ProfilerSettingsBean(myServerPaths));
    ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("profiler.jsp"));
    mv.getModel().put("settingsBean", settingsBean);
    return mv;
  }
}
