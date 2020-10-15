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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents the "Update" command and it holds arguments and flags specified by the user.
 */
@CommandLine.Command(name = "list", description = "List Ballerina Distributions")
public class ListCommand extends Command implements BCommand {
    private static final String LOCAL_DISTRIBUTIONS_FILE = "local-dists.json";

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
                    for (File file : listOfFiles) {
                        if (file.isDirectory()) {
                            String version = "";
                            String[] parts =  file.getName().split("-");
                            if (parts.length == 2) {
                                version = parts[1];
                            }
                            if (version.equals(distribution.getVersion())) {
                                outStream.println(markVersion(currentBallerinaVersion, version)
                                        + " " + ToolUtil.getTypeName(version));
                                installedVersions.add(version);
                            }
                        }
                    }
                }
            }
            writeLocalDistsIntoJson(installedVersions);
            outStream.println("\nDistributions available remotely:");
            for (Channel channel : channels) {
                outStream.println("\n" + channel.getName() + "\n");
                for (Distribution distribution : channel.getDistributions()) {
                    outStream.println(markVersion(currentBallerinaVersion, distribution.getVersion())
                            + " " + distribution.getName());
                }
            }
        } catch (CommandException | IOException e) {
            outStream.println("Distributions available locally: \n");
            readLocalDistsFromJson(outStream, currentBallerinaVersion);
            ErrorUtil.printLauncherException((CommandException) e, outStream);
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

    /**
     * Writes the locally available distributions into a json file to fetch local distributions when offline.
     *
     * @param installedVersions Installed ballerina versions
     */
    private static void writeLocalDistsIntoJson(List<String> installedVersions) throws IOException {
        FileWriter writer;
        try {
            writer = new FileWriter(LOCAL_DISTRIBUTIONS_FILE);
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("cannot write into " + LOCAL_DISTRIBUTIONS_FILE + " file.");
        }
        for(String str: installedVersions) {
            writer.write(str + System.lineSeparator());
        }
        writer.close();
    }

    /**
     * Reads the locally available distributions from previously saved json file.
     *
     * @param outStream stream outputs need to be printed
     * @param currentBallerinaVersion Current active version
     */
    private static void readLocalDistsFromJson(PrintStream outStream, String currentBallerinaVersion) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(LOCAL_DISTRIBUTIONS_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                outStream.println(markVersion(currentBallerinaVersion, line) + "  " + ToolUtil.getTypeName(line));
            }
            outStream.println("\n");
            reader.close();
        } catch (FileNotFoundException fileNotFoundException) {
            throw ErrorUtil.createCommandException("cannot find " + LOCAL_DISTRIBUTIONS_FILE + " file.");
        } catch (IOException ioException) {
            throw ErrorUtil.createCommandException("cannot read " + LOCAL_DISTRIBUTIONS_FILE + " file.");
        }
    }
}
