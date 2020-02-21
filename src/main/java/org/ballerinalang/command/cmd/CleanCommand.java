/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.command.cmd;

import org.ballerinalang.command.BallerinaCliCommands;
import org.ballerinalang.command.util.ErrorUtil;
import org.ballerinalang.command.util.ToolUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import static org.ballerinalang.command.util.OSUtils.deleteFiles;

/**
 * This class represents the "Clean" command and it holds arguments and flags specified by the user.
 */
@CommandLine.Command(name = "clean", description = "Clean Ballerina distributions")
public class CleanCommand extends Command implements BCommand {

    @CommandLine.Parameters(description = "Command name")
    private List<String> cleanCommands;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--force", "-f"}, hidden = true)
    private boolean forceFlag;

    private CommandLine parentCmdParser;

    public CleanCommand(PrintStream printStream) {
        super(printStream);
    }

    public void execute() {
        Scanner in = new Scanner(System.in);
        if (helpFlag) {
            printUsageInfo(ToolUtil.CLI_HELP_FILE_PREFIX + BallerinaCliCommands.CLEAN);
            return;
        }

        if (forceFlag) {
            ToolUtil.handleInstallDirPermission();
            clean();
            return;
        }

        if (cleanCommands == null) {
            ToolUtil.handleInstallDirPermission();
            getPrintStream().println("This will delete all distributions except the active distribution." +
                    " Still you want to continue? (y/n)");
            String res = in.next();
            if (res.equalsIgnoreCase("y") || res.equalsIgnoreCase("yes")) {
                clean();
            } else {
                getPrintStream().println("If you want to remove a specified version, use 'ballerina remove [dist-id]'");
            }
            return;
        }

        if (cleanCommands.size() > 0) {
            throw ErrorUtil.createDistSubCommandUsageExceptionWithHelp("too many arguments",
                    BallerinaCliCommands.CLEAN);
        }
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.CLEAN;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina dist clean\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    private void clean() {
        String currentVersion = ToolUtil.getCurrentBallerinaVersion();
        try {
            File folder = new File(ToolUtil.getDistributionsPath());
            File[] listOfFiles;
            listOfFiles = folder.listFiles();
            if (listOfFiles.length == 2) {
                getPrintStream().println("Already cleaned");
                return;
            }
            for (File file: listOfFiles) {
                if (file.isDirectory()) {
                    String version = file.getName();
                    File directory = new File(ToolUtil.getDistributionsPath() + File.separator + version);
                    if (!version.equals(ToolUtil.BALLERINA_TYPE + "-" + currentVersion) && directory.exists()) {
                        deleteFiles(directory.toPath(), getPrintStream(), version);
                    }
                }
            }
            getPrintStream().println("Cleaned Successfully");
        } catch (IOException | NullPointerException e) {
            throw ErrorUtil.createCommandException("error occurred while cleaning distributions" + e);
        }
    }
}
