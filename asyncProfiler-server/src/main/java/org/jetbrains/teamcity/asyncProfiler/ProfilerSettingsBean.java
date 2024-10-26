package org.jetbrains.teamcity.asyncProfiler;

import jetbrains.buildServer.serverSide.ServerPaths;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ProfilerSettingsBean {
  private String myProfilerPath = "asprof";
  private String myReportsPath;
  private String myArgs = "-d60";

  ProfilerSettingsBean(@NotNull ServerPaths serverPaths) {
    myReportsPath = new File(serverPaths.getLogsPath(), "async-profiler").getAbsolutePath();
  }

  public String getProfilerPath() {
    return myProfilerPath;
  }

  public void setProfilerPath(String profilerPath) {
    myProfilerPath = profilerPath;
  }

  public String getReportsPath() {
    return myReportsPath;
  }

  public void setReportsPath(String reportsPath) {
    myReportsPath = reportsPath;
  }

  public String getArgs() {
    return myArgs;
  }

  public void setArgs(String args) {
    myArgs = args;
  }
}
