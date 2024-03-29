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
import org.ballerinalang.command.util.OSUtils;
import org.ballerinalang.command.util.ToolUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * This class represents the "Remove" command and it holds arguments and flags specified by the user.
 */
@CommandLine.Command(name = "remove", description = "Remove Ballerina distribution")
public class RemoveCommand extends Command implements BCommand {

    @CommandLine.Parameters(description = "Command name")
    private List<String> removeCommands;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--all", "-a"})
    private boolean allFlag;

    private CommandLine parentCmdParser;

    public RemoveCommand(PrintStream printStream) {
        super(printStream);
    }

    public void execute() {
        if (helpFlag) {
            printUsageInfo(ToolUtil.CLI_HELP_FILE_PREFIX + getName());
            return;
        }

        if (allFlag) {
            if (removeCommands == null) {
                ToolUtil.handleInstallDirPermission();
                removeAll();
                return;
            }
            throw ErrorUtil.createDistSubCommandUsageExceptionWithHelp("too many arguments", getName());
        }

        if (removeCommands == null || removeCommands.size() == 0) {
            throw ErrorUtil.createDistributionRequiredException("remove", "--all, -a");
        }

        if (removeCommands.size() > 1) {
            throw ErrorUtil.createDistSubCommandUsageExceptionWithHelp("too many arguments",
                    BallerinaCliCommands.REMOVE);
        }

        ToolUtil.handleInstallDirPermission();
        remove(removeCommands.get(0));
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.REMOVE;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal dist remove\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    private boolean isCurrentVersion(String version) {
        return version.equals(ToolUtil.getCurrentBallerinaVersion());
    }

    private void remove(String version) {
        try {
            if (isCurrentVersion(version)) {
                throw ErrorUtil.createCommandException("The active Ballerina distribution cannot be removed");
            } else {
                String file = ToolUtil.getType(version) + "-" + version;
                File directory = new File(ToolUtil.getDistributionsPath() + File.separator + file);
                if (directory.exists()) {
                    OSUtils.deleteFiles(directory.toPath());
                    OSUtils.deleteCaches(version, getPrintStream());
                    getPrintStream().println("Distribution '" + version + "' successfully removed");
                    ToolUtil.removeUnusedDependencies(version, getPrintStream());
                } else {
                    throw ErrorUtil.createCommandException("distribution '" + version + "' not found");
                }
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("error occurred while removing '" + version + "'");
        }
    }

    private void removeAll() {
        try {
            File folder = new File(ToolUtil.getDistributionsPath());
            File[] listOfFiles;
            listOfFiles = folder.listFiles();
            // checking for 2 files for zip pack and 3 files for installers
            if (listOfFiles.length == 2 || (listOfFiles.length == 3 && folder.toPath().resolve("installer-version").
                    toFile().exists())) {
                getPrintStream().println("There is nothing to remove. Only active distribution is remaining");
                return;
            }
            for (File file : listOfFiles) {
                if (file.isDirectory()) {
                    String version = "";
                    String fileName = file.getName();
                    String[] parts = fileName.split("-");
                    if (parts.length == 2) {
                        version = parts[1];
                    }
                    File directory = new File(ToolUtil.getDistributionsPath() + File.separator + fileName);
                    if (directory.exists() && (!isCurrentVersion(version) || version.equals(""))) {
                        OSUtils.deleteFiles(directory.toPath());
                    }
                }
            }
            getPrintStream().println("All non-active distributions are successfully removed");
            String activeDistribution = ToolUtil.getCurrentBallerinaVersion();
            String dependencyForActiveDistribution = ToolUtil.getDependency(getPrintStream(), activeDistribution,
                    ToolUtil.getType(activeDistribution), activeDistribution);
            File[] dependencies = new File(ToolUtil.getDependencyPath()).listFiles();
            if (dependencies != null) {
                if (dependencies.length > 1) {
                    getPrintStream().println("Removing unused dependencies");
                    for (File dependency : dependencies) {
                        if (dependency.isDirectory() && !dependency.getName().equals(dependencyForActiveDistribution)) {
                            OSUtils.deleteFiles(dependency.toPath());
                        }
                    }
                }
            } else {
                throw ErrorUtil.createCommandException("No dependencies found");
            }

        } catch (IOException | NullPointerException e) {
            throw ErrorUtil.createCommandException("error occurred while removing the distributions" + e);
        }
    }
}
