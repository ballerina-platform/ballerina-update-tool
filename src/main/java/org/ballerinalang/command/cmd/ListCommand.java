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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import picocli.CommandLine;

import org.ballerinalang.command.util.Channel;
import org.ballerinalang.command.util.Distribution;
import org.ballerinalang.command.util.ErrorUtil;
import org.ballerinalang.command.util.OSUtils;
import org.ballerinalang.command.util.ToolUtil;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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
        File folder = new File(ToolUtil.getDistributionsPath());
        File[] listOfFiles = folder.listFiles();
        Arrays.sort(listOfFiles);
        try {
            JSONObject distList = new JSONObject();
            JSONArray channelsArr = new JSONArray();
            List<Channel> channels = ToolUtil.getDistributions();
            outStream.println("Distributions available locally: \n");
            for (Channel channel : channels) {
                JSONObject channelJson = new JSONObject();
                JSONArray releases = new JSONArray();
                channelJson.put("name", channel.getName());
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
                                        + " " + distribution.getName());
                                releases.add(version);
                            }
                        }
                    }
                }
                channelJson.put("releases", releases);
                channelsArr.add(channelJson);
            }
            distList.put("channels", channelsArr);
            writeLocalDistsIntoJson(distList);
            outStream.println("\nDistributions available remotely:");
            for (Channel channel : channels) {
                outStream.println("\n" + channel.getName() + "\n");
                for (Distribution distribution : channel.getDistributions()) {
                    outStream.println(markVersion(currentBallerinaVersion, distribution.getVersion())
                            + " " + distribution.getName());
                }
            }
        } catch (CommandException e) {
            outStream.println("Distributions available locally: \n");
            if(!isUpdated(listOfFiles)) {
                listLocalDists(listOfFiles, outStream, currentBallerinaVersion);
            } else {
                readLocalDistsFromJson(outStream, currentBallerinaVersion);
            }
            outStream.println("\nDistributions available remotely: \n");
            ErrorUtil.printLauncherException(e, outStream);
        } finally {
            outStream.println();
            outStream.println("Use 'bal help dist' for more information on specific commands.");
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
     * @param distList Installed ballerina versions
     */
    private static void writeLocalDistsIntoJson(JSONObject distList) {
        try {
            String path = OSUtils.getBallerinaDistListFilePath();
            File file = new File(path);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                ToolUtil.addExecutablePermissionToFile(file);
            }
            Files.write(Paths.get(path), distList.toJSONString().getBytes());
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("failed to write in the file: " + e.getMessage());
        }
    }

    /**
     * Reads the locally available distributions from previously saved json file.
     *
     * @param outStream stream outputs need to be printed
     * @param currentBallerinaVersion Current active version
     */
    private static void readLocalDistsFromJson(PrintStream outStream, String currentBallerinaVersion) {
        try {
            FileReader reader = new FileReader(OSUtils.getBallerinaDistListFilePath());
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            JSONArray channels = (JSONArray) jsonObject.get("channels");
            for (Object channel: channels) {
                JSONObject channelObj = (JSONObject) channel;
                JSONArray releases = (JSONArray) channelObj.get("releases");
                for (Object version : releases) {
                    outStream.println(markVersion(currentBallerinaVersion, version.toString()) + "  " +
                            ToolUtil.getTypeName(version.toString()));
                }
            }
        } catch (IOException | ParseException e) {
            throw ErrorUtil.createCommandException("failed to read the file: " + e.getMessage());
        }
    }

    /**
     * List the locally available distributions from the distributions directory.
     *
     * @param listOfFiles locally available distributions
     * @param outStream stream outputs need to be printed
     * @param currentBallerinaVersion Current active version
     */
    private static void listLocalDists(File[] listOfFiles, PrintStream outStream, String currentBallerinaVersion) {
        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                String version = "";
                String[] parts = file.getName().split("-");
                if (parts.length == 2) {
                    version = parts[1];
                }
                outStream.println(markVersion(currentBallerinaVersion, version) + " " + ToolUtil.getTypeName(version));
            }
        }
    }

    private static boolean isUpdated(File[] listOfFiles) {
        try {
            if (!new File(OSUtils.getBallerinaDistListFilePath()).exists()) {
                return false;
            }

            FileReader reader = new FileReader(OSUtils.getBallerinaDistListFilePath());
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            String jsonString = jsonObject.toJSONString();
            for (File file : listOfFiles) {
                if (file.isDirectory()) {
                    String version = "";
                    String[] parts = file.getName().split("-");
                    if (parts.length == 2) {
                        version = parts[1];
                    }
                    if (!jsonString.contains(version)) {
                        return false;
                    }
                }
            }
        } catch (ParseException | IOException e) {
            throw ErrorUtil.createCommandException("failed to parse the content of the file: " + e.getMessage());
        }
        return true;
    }
}
