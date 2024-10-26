package org.jetbrains.teamcity.asyncProfiler;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.FormUtil;
import jetbrains.buildServer.controllers.XmlResponseUtil;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.ThreadUtil;
import jetbrains.buildServer.util.executors.ExecutorsFactory;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;

public class AsyncProfilerController extends BaseController {
  public static final String URL = "/admin/diagnostics/asyncProfiler.html";
  private final ServerPaths myServerPaths;
  private final PluginDescriptor myPluginDescriptor;
  private final File myReportsPath;
  private final ExecutorService myExecutor;

  public AsyncProfilerController(@NotNull WebControllerManager controllerManager,
                                 @NotNull ServerPaths serverPaths,
                                 @NotNull PluginDescriptor pluginDescriptor,
                                 @NotNull EventDispatcher<BuildServerListener> eventDispatcher) {
    controllerManager.registerController(URL, this);
    myServerPaths = serverPaths;
    myPluginDescriptor = pluginDescriptor;
    myReportsPath = new File(serverPaths.getLogsPath(), "async-profiler");
    myExecutor = ExecutorsFactory.newFixedDaemonExecutor("AsyncProfiler", 1);

    eventDispatcher.addListener(new BuildServerAdapter() {
      @Override
      public void serverShutdown() {
        ThreadUtil.shutdownGracefully(myExecutor, "AsyncProfiler");
      }
    });
  }

  @Nullable
  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse httpServletResponse) throws IOException {
    SUser user = SessionUser.getUser(request);
    if (!user.isPermissionGrantedGlobally(Permission.MANAGE_SERVER_INSTALLATION)) {
      throw new AccessDeniedException(user, "You do not have permissions to access this page");
    }

    ProfilerSettingsBean settingsBean = FormUtil.getOrCreateForm(request, ProfilerSettingsBean.class, r -> {
      ProfilerSettingsBean bean = new ProfilerSettingsBean();
      bean.setReportsPath(myReportsPath.getAbsolutePath());
      return bean;
    });

    assert settingsBean != null;

    if (request.getParameter("startProfiler") != null) {
      FormUtil.bindFromRequest(request, settingsBean);

      myReportsPath.mkdirs();
      if (!myReportsPath.isDirectory()) {
        throw new RuntimeException("Cannot create directory for the profiler reports: " + myReportsPath.getAbsolutePath());
      }

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
      File reportPath = new File(myReportsPath, settingsBean.getSessionName() + "-" + df.format(new Date()) + ".html");
      AsyncProfiler profiler = new AsyncProfiler(settingsBean.getProfilerPath(), settingsBean.getArgs(), reportPath.getAbsolutePath(), myServerPaths, myExecutor);
      settingsBean.setProfilerSession(profiler.start());

      Element element = XmlResponseUtil.newXmlResponse();
      element.setText("started");
      XmlResponseUtil.writeXmlResponse(element, httpServletResponse);
      return null;
    }

    ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("profiler.jsp"));
    mv.getModel().put("settingsBean", settingsBean);
    return mv;
  }
}
