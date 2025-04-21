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
import org.ballerinalang.command.util.Tool;
import org.ballerinalang.command.util.ToolUtil;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * This class represents the "Update" command and it holds arguments and flags specified by the user.
 *
 * @since 0.8.0
 */
@CommandLine.Command(name = "command", description = "Update Ballerina current cli tool commands")
public class UpdateToolCommand extends Command implements BCommand {

    @CommandLine.Parameters(description = "args")
    private List<String> updateCommands;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    private CommandLine parentCmdParser;

    public UpdateToolCommand(PrintStream printStream) {
        super(printStream);
    }

    public void execute() {
        if (helpFlag) {
            printUsageInfo(BallerinaCliCommands.UPDATE);
            return;
        }

        if (updateCommands == null) {
            ToolUtil.handleInstallDirPermission();
            updateCommands(getPrintStream());
            return;
        }

        if (updateCommands.size() > 0) {
            throw ErrorUtil.createUsageExceptionWithHelp("too many arguments", getName());
        }
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.UPDATE;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Updates the Ballerina tool to the latest version.\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina tool\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    private static void updateCommands(PrintStream printStream) {
        String version = ToolUtil.getCurrentToolsVersion();
        printStream.println("Fetching the latest update tool version from the remote server...");
        Tool latestVersionDetails = ToolUtil.getLatestToolVersion(printStream);
        String latestVersion = latestVersionDetails.getVersion();
        if (latestVersion == null) {
            printStream.println("Failed to find the latest update tool version");
            return;
        }
        if (latestVersion.equals(version)) {
            printStream.println("The latest update tool version '" + latestVersion + "' is already in use");
            printStream.println("\nIf you want to update the Ballerina distribution, use `bal dist update`");
            return;
        }
        ToolUtil.downloadTool(printStream, latestVersion);
    }
}
