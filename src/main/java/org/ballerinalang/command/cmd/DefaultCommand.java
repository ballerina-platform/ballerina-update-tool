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
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * This class represents the "default" command required by picocli.
 */
@CommandLine.Command(description = "Default Command.", name = "default")
public class DefaultCommand extends Command implements BCommand {

    @CommandLine.Option(names = { "--help", "-h", "?" }, hidden = true, description = "for more information")
    private boolean helpFlag;

    // --debug flag is handled by ballerina.sh/ballerina.bat. It will launch ballerina with java debug options.
    @CommandLine.Option(names = "--debug", description = "start Ballerina in remote debugging mode")
    private String debugPort;

    @CommandLine.Option(names = { "--version", "-v" }, hidden = true)
    private boolean versionFlag;

    @CommandLine.Parameters(description = "Help command name")
    private List<String> helpCommands;

    public DefaultCommand(PrintStream printStream) {
        super(printStream);
    }

    @Override
    public void execute() {
        if (versionFlag) {
            printVersionInfo();
            return;
        }

        printUsageInfo(BallerinaCliCommands.HELP);
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.DEFAULT;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
