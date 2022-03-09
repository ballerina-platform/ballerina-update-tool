/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import java.io.PrintStream;
import java.util.List;

/**
 * This class represents the "Update" command and it holds arguments and flags specified by the user.
 */
@CommandLine.Command(name = "command", description = "Update Ballerina current distribution")
public class UpdateCommand extends Command implements BCommand {

    @CommandLine.Parameters(description = "Command name")
    private List<String> updateCommands;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--test", "-t"}, hidden = true)
    private static boolean testFlag;

    private CommandLine parentCmdParser;

    public UpdateCommand(PrintStream printStream) {
        super(printStream);
    }

    public void execute() {
        if (helpFlag) {
            printUsageInfo(ToolUtil.CLI_HELP_FILE_PREFIX + getName());
            return;
        }

        if (updateCommands == null) {
            ToolUtil.handleInstallDirPermission();
            update(getPrintStream());
            return;
        }

        if (updateCommands.size() > 0) {
            throw ErrorUtil.createDistSubCommandUsageExceptionWithHelp("too many arguments", getName());
        }
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.UPDATE;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal dist command\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    public static void update(PrintStream printStream) {
        if (!testFlag) {
            // Check and update the tool if any latest version available
            ToolUtil.updateTool(printStream);
        }
        String version = ToolUtil.getCurrentBallerinaVersion();
        String distVersion = ToolUtil.getType(version) + "-" + version;
        printStream.println("Fetching the latest patch distribution for '" + distVersion + "' from " +
                "the remote server...");
        String latestVersion = ToolUtil.getLatest(version, "patch");
        if (latestVersion == null) {
            printStream.println("Failed to find the latest patch distribution for '" + distVersion + "'");
            return;
        }

        if (!latestVersion.equals(version)) {
            ToolUtil.downloadDistribution(printStream, latestVersion, ToolUtil.getType(latestVersion), latestVersion, testFlag);
            ToolUtil.useBallerinaVersion(printStream, latestVersion);
            printStream.println("Successfully set the latest patch distribution '" + latestVersion + "' as the " +
                    "active distribution");
            return;
        }
        printStream.println("The latest patch distribution '" + latestVersion + "' is already the active distribution");
    }
}
