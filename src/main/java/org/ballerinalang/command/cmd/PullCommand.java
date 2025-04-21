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
import org.ballerinalang.command.util.Channel;
import org.ballerinalang.command.util.Distribution;
import org.ballerinalang.command.util.ErrorUtil;
import org.ballerinalang.command.util.Tool;
import org.ballerinalang.command.util.ToolUtil;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class represents the "Pull" command and it holds arguments and flags specified by the user.
 */
@CommandLine.Command(name = "pull", description = "Pull Ballerina distribution")
public class PullCommand extends Command implements BCommand {

    @CommandLine.Parameters(description = "Command name")
    private List<String> pullCommands;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--test", "-t"}, hidden = true)
    private boolean testFlag;

    private CommandLine parentCmdParser;

    public PullCommand(PrintStream printStream) {
        super(printStream);
    }


    public void execute() {
        if (helpFlag) {
            printUsageInfo(ToolUtil.CLI_HELP_FILE_PREFIX + getName());
            return;
        }

        if (pullCommands == null || pullCommands.size() == 0) {
            throw ErrorUtil.createDistributionRequiredException("pull");
        }

        if (pullCommands.size() > 1) {
            throw ErrorUtil.createDistSubCommandUsageExceptionWithHelp("too many arguments", getName());
        }
        PrintStream printStream = getPrintStream();
        String distribution = pullCommands.get(0);

        if (!testFlag) {
            // Check and update the tool if any latest version available
            Tool toolDetails = ToolUtil.updateTool(printStream);
            if (!toolDetails.getCompatibility().equals("true")) {
                return;
            }
        }

        // To handle bal dist pull latest
        if (distribution.equals(ToolUtil.LATEST_PULL_INPUT)) {
            printStream.println("Fetching the latest distribution from the remote server...");
            List<Channel> channels = ToolUtil.getDistributions(printStream);
            // Assume channels are sorted descending
            Channel latestChanel = channels.get(0);
            List<Distribution> distributions = latestChanel.getDistributions();
            distributions.sort(Comparator.comparing(Distribution::getVersion));
            Collections.reverse(distributions);
            distribution = ToolUtil.getLatest(distributions.get(0).getVersion(), "patch", printStream);
        }

        // To check whether the distribution is a valid one
        if (!distribution.equals(ToolUtil.LATEST_PULL_INPUT)) {
            boolean validDist = false;
            List<Channel> channels = ToolUtil.getDistributions(printStream);
            for (Channel channel : channels) {
                List<Distribution> distributions = channel.getDistributions();
                for (Distribution dist : distributions) {
                    if (dist.getVersion().equals(distribution)) {
                        validDist = true;
                        break;
                    }
                }
            }
            if (!validDist) {
                throw ErrorUtil.createDistributionNotFoundException(distribution);
            }
        }

        if (distribution.equals(ToolUtil.getCurrentBallerinaVersion())) {
            printStream.println("'" + distribution + "' is already the active distribution");
            return;
        }
        ToolUtil.handleInstallDirPermission();
        ToolUtil.downloadDistribution(printStream, distribution, ToolUtil.getType(distribution), distribution, testFlag);
        ToolUtil.useBallerinaVersion(printStream, distribution);
        printStream.println("'" + distribution + "' successfully set as the active distribution");
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.PULL;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal dist pull\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }
}
