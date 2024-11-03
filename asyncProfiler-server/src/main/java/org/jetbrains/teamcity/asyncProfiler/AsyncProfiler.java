package org.jetbrains.teamcity.asyncProfiler;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.serverSide.IOGuard;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncProfiler {
  private final File myPidFile;
  private final ExecutorService myExecutor;
  private final ServerPaths myServerPaths;

  private volatile AsyncProfilerSession mySession = null;

  public AsyncProfiler(@NotNull ServerPaths serverPaths, @NotNull ExecutorService executor) {
    myPidFile = new File(serverPaths.getLogsPath(), "teamcity.pid");
    myExecutor = executor;
    myServerPaths = serverPaths;
  }

  @NotNull
  public synchronized AsyncProfilerSession start(@NotNull String args, @NotNull String reportPath) {
    if (mySession != null && !mySession.isFinished()) {
      throw new IllegalStateException("There is an in progress profiling session");
    }

    mySession = new AsyncProfilerSession();
    mySession.setRelativeReportPath(FileUtil.getRelativePath(myServerPaths.getLogsPath(), new File(reportPath)).replace('\\', '/'));

    if (!myPidFile.isFile()) {
      mySession.setFuture(CompletableFuture.completedFuture(createFailedResult("TeamCity pid file does not exist: " + myPidFile.getAbsolutePath())));
      return mySession;
    }

    String pid;
    try {
      pid = FileUtil.readText(myPidFile).trim();
    } catch (IOException e) {
      mySession.setFuture(CompletableFuture.completedFuture(createFailedResult("Cannot read from the pid file " + myPidFile.getAbsolutePath() + ", error: " + e.toString())));
      return mySession;
    }

    AtomicInteger maxProfilingDurationSeconds = new AtomicInteger(90);
    List<String> params = StringUtil.split(args, true, ' ');
    for (int i = 0; i < params.size(); i++) {
      if (params.get(i).equals("-d") && i < params.size() - 1) {
        String duration = params.get(i + 1);
        maxProfilingDurationSeconds.set(Math.round(1.5f * Integer.parseInt(duration)));
      }
    }

    GeneralCommandLine cli = new GeneralCommandLine();
    cli.setExePath(getProfilerPath());
    cli.addParameters(StringUtil.split(args, true, ' '));
    cli.addParameter("-f");
    cli.addParameter(reportPath);
    cli.addParameter(pid);

    mySession.setCommandLine(cli.getCommandLineString());

    mySession.setFuture(CompletableFuture.supplyAsync(() -> IOGuard.allowCommandLine(() -> SimpleCommandLineProcessRunner.runCommand(cli, null, new SimpleCommandLineProcessRunner.RunCommandEventsAdapter() {
      @Override
      public Integer getOutputIdleSecondsTimeout() {
        return maxProfilingDurationSeconds.get();
      }
    })), myExecutor));

    return mySession;
  }

  @NotNull
  public String getProfilerPath() {
    return TeamCityProperties.getProperty("teamcity.asyncProfiler.profilerPath", "asprof");
  }

  @NotNull
  private ExecResult createFailedResult(@NotNull String message) {
    ExecResult failedResult = new ExecResult();
    failedResult.setStderr(message);
    failedResult.setExitCode(-100);
    return failedResult;
  }
}
