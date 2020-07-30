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
import org.ballerinalang.command.exceptions.CommandException;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ballerinalang.command.util.Channel;
import org.ballerinalang.command.util.Distribution;
import org.ballerinalang.command.util.ErrorUtil;
import org.ballerinalang.command.util.ToolUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.IOException;

/**
 * This class represents the "Update" command and it holds arguments and flags specified by the user.
 */
@CommandLine.Command(name = "list", description = "List Ballerina Distributions")
public class ListCommand extends Command implements BCommand {

    @CommandLine.Parameters(description = "Command name")
    private List<String> listCommands;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    private CommandLine parentCmdParser;

    public ListCommand(PrintStream printStream) {
        super(printStream);
    }

    public void execute() {
        if (helpFlag) {
            printUsageInfo(ToolUtil.CLI_HELP_FILE_PREFIX + BallerinaCliCommands.LIST);
            return;
        }

        if (listCommands == null) {
            listDistributions(getPrintStream());
            return;
        }

        if (listCommands.size() > 0) {
            throw ErrorUtil.createDistSubCommandUsageExceptionWithHelp("too many arguments", BallerinaCliCommands.LIST);
        }
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.LIST;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina dist list\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    /**
     * List distributions in the local and remote.
     *
     * @param outStream stream outputs need to be printed
     */
    private static void listDistributions(PrintStream outStream) {
        String currentBallerinaVersion = ToolUtil.getCurrentBallerinaVersion();
        try {
            File folder = new File(ToolUtil.getDistributionsPath());
            File[] listOfFiles;
            listOfFiles = folder.listFiles();
            Arrays.sort(listOfFiles);
            List<Channel> channels = ToolUtil.getDistributions();
            outStream.println("Distributions available locally: \n");
            List<String> installedVersions = new ArrayList<>();
            for (Channel channel : channels) {
                for (Distribution distribution : channel.getDistributions()) {
                    for (int i = 0; i < listOfFiles.length; i++) {
                        if (listOfFiles[i].isDirectory()) {
                            String version = listOfFiles[i].getName().split("-")[1];
                            if (version.equals(distribution.getVersion())) {
                                outStream.println(markVersion(currentBallerinaVersion, version)
                                        + " " + ToolUtil.getType(version));
                                installedVersions.add(version);
                            }
                        }
                    }
                }
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter("localDists.json");
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(String str: installedVersions) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
            outStream.println("\nDistributions available remotely:\n");
            for (Channel channel : channels) {
                outStream.println("\n" + channel.getName() + "\n");
                for (Distribution distribution : channel.getDistributions()) {
                    outStream.println(markVersion(currentBallerinaVersion, distribution.getVersion())
                            + " " + distribution.getName());
                }
            }
        } catch (CommandException e) {
            ErrorUtil.printLauncherException(e, outStream);
            outStream.println("Distributions available locally: \n");
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader("localDists.json"));
                String line = reader.readLine();
                while (line != null) {
                    try {
                        line = reader.readLine();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    outStream.println(markVersion(currentBallerinaVersion, line)
                            + "  " + ToolUtil.getType(line));
                }
                reader.close();
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outStream.println();
            outStream.println("Use 'ballerina help dist' for more information on specific commands.");
        }
    }

    /**
     * Checks used Ballerina version and mark the output.
     *
     * @param used    Used Ballerina version
     * @param current Version needs to be checked
     * @return Marked output
     */
    private static String markVersion(String used, String current) {
        if (used.equals(current)) {
            return "* [" + current + "]";
        } else {
            return "  [" + current + "]";
        }
    }
}
