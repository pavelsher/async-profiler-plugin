package org.jetbrains.teamcity.asyncProfiler;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.serverSide.IOGuard;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncProfiler {
  private final String myAsprofPath;
  private final String myArgs;
  private final String myReportPath;
  private final File myPidFile;
  private final ExecutorService myExecutor;
  private final ServerPaths myServerPaths;

  public AsyncProfiler(@NotNull String asprofPath, @NotNull String args, @NotNull String reportPath,
                       @NotNull ServerPaths serverPaths,
                       @NotNull ExecutorService executor) {
    myAsprofPath = asprofPath;
    myArgs = args;
    myReportPath = reportPath;
    myPidFile = new File(serverPaths.getLogsPath(), "teamcity.pid");
    myExecutor = executor;
    myServerPaths = serverPaths;
  }

  @NotNull
  public AsyncProfilerSession start() {
    AsyncProfilerSession session = new AsyncProfilerSession();
    session.setRelativeReportPath(FileUtil.getRelativePath(myServerPaths.getLogsPath(), new File(myReportPath)).replace('\\', '/'));

    if (!myPidFile.isFile()) {
      session.setFuture(CompletableFuture.completedFuture(createFailedResult("TeamCity pid file does not exist: " + myPidFile.getAbsolutePath())));
      return session;
    }

    String pid;
    try {
      pid = FileUtil.readText(myPidFile).trim();
    } catch (IOException e) {
      session.setFuture(CompletableFuture.completedFuture(createFailedResult("Cannot read from the pid file " + myPidFile.getAbsolutePath() + ", error: " + e.toString())));
      return session;
    }

    AtomicInteger maxProfilingDurationSeconds = new AtomicInteger(90);
    List<String> params = StringUtil.split(myArgs, true, ' ');
    for (int i = 0; i < params.size(); i++) {
      if (params.get(i).equals("-d") && i < params.size() - 1) {
        String duration = params.get(i + 1);
        maxProfilingDurationSeconds.set(Math.round(1.5f * Integer.parseInt(duration)));
      }
    }

    GeneralCommandLine cli = new GeneralCommandLine();
    cli.setExePath(myAsprofPath);
    cli.addParameters(StringUtil.split(myArgs, true, ' '));
    cli.addParameter("-f");
    cli.addParameter(myReportPath);
    cli.addParameter(pid);

    session.setCommandLine(cli.getCommandLineString());

    session.setFuture(CompletableFuture.supplyAsync(() -> IOGuard.allowCommandLine(() -> SimpleCommandLineProcessRunner.runCommand(cli, null, new SimpleCommandLineProcessRunner.RunCommandEventsAdapter() {
      @Override
      public Integer getOutputIdleSecondsTimeout() {
        return maxProfilingDurationSeconds.get();
      }
    })), myExecutor));
    return session;
  }

  @NotNull
  private ExecResult createFailedResult(@NotNull String message) {
    ExecResult failedResult = new ExecResult();
    failedResult.setStderr(message);
    failedResult.setExitCode(-100);
    return failedResult;
  }
}
