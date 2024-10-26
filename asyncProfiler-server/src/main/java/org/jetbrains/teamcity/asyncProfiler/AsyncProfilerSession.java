package org.jetbrains.teamcity.asyncProfiler;

import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.TimePrinter;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AsyncProfilerSession {
  private CompletableFuture<ExecResult> myFuture;
  private Date myStartTime;
  private String myReportPath;
  private String myCommandLine;

  @NotNull
  public ExecResult getResult() {
    ExecResult inProgress = new ExecResult();
    long timeSpent = Dates.now().getTime() - myStartTime.getTime();
    inProgress.setStdout("Running, time spent: " + TimePrinter.createSecondsFormatter().formatTime(TimeUnit.MILLISECONDS.toSeconds(timeSpent)));
    return myFuture.getNow(inProgress);
  }

  public boolean isFinished() {
    return myFuture.isDone();
  }

  public boolean isFailed() {
    return isFinished() && getResult().getExitCode() != 0;
  }

  public void setFuture(@NotNull CompletableFuture<ExecResult> future) {
    myStartTime = new Date();
    myFuture = future;
  }

  public void setRelativeReportPath(String reportPath) {
    myReportPath = reportPath;
  }

  public String getReportPath() {
    return myReportPath;
  }

  public void setCommandLine(String commandLine) {
    myCommandLine = commandLine;
  }

  public String getCommandLine() {
    return myCommandLine;
  }
}
