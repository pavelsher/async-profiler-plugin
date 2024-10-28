package org.jetbrains.teamcity.asyncProfiler;

import org.jetbrains.annotations.NotNull;

public class ProfilerSettingsBean {
  private final String myProfilerPath;
  private String myReportsPath;
  private String mySessionName = "profile";
  private String myArgs = "-d 60";

  private AsyncProfilerSession myProfilerSession;

  public ProfilerSettingsBean(@NotNull String profilerPath) {
    myProfilerPath = profilerPath;
  }

  @NotNull
  public String getProfilerPath() {
    return myProfilerPath;
  }

  @NotNull
  public String getReportsPath() {
    return myReportsPath;
  }

  public void setReportsPath(@NotNull String reportsPath) {
    myReportsPath = reportsPath;
  }

  @NotNull
  public String getArgs() {
    return myArgs;
  }

  public void setArgs(@NotNull String args) {
    myArgs = args;
  }

  @NotNull
  public String getSessionName() {
    return mySessionName;
  }

  public void setSessionName(@NotNull String sessionName) {
    mySessionName = sessionName;
  }

  public AsyncProfilerSession getProfilerSession() {
    return myProfilerSession;
  }

  public void setProfilerSession(AsyncProfilerSession profilerSession) {
    myProfilerSession = profilerSession;
  }
}
