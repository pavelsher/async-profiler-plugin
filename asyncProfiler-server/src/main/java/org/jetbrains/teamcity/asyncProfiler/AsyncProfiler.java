package org.jetbrains.teamcity.asyncProfiler;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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
    if (!myPidFile.isFile()) {
      throw new IllegalStateException("TeamCity pid file does not exist: " + myPidFile.getAbsolutePath());
    }

    String pid;
    try {
      pid = FileUtil.readText(myPidFile);
    } catch (IOException e) {
      throw new RuntimeException("Cannot read from the pid file " + myPidFile.getAbsolutePath(), e);
    }

    GeneralCommandLine cli = new GeneralCommandLine();
    cli.setExePath(myAsprofPath);
    cli.addParameters(StringUtil.split(myArgs, true, ' '));
    cli.addParameter("-f");
    cli.addParameter(myReportPath);
    cli.addParameter(pid);

    AsyncProfilerSession session = new AsyncProfilerSession();
    session.setRelativeReportPath(FileUtil.getRelativePath(myServerPaths.getLogsPath(), new File(myReportPath)).replace('\\', '/'));
    session.setFuture(CompletableFuture.supplyAsync(() -> SimpleCommandLineProcessRunner.runCommand(cli, null), myExecutor));
    return session;
  }
}
