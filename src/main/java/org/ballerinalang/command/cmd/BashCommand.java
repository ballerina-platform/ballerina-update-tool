/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * This class represents the "Bash" command and it holds arguments and flags specified by the user.
 */
@CommandLine.Command(name = "bash", description = "Ballerina bash commands")
public class BashCommand extends Command implements BCommand {
    @CommandLine.Parameters(description = "Command name")
    private List<String> listCommands;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    private CommandLine parentCmdParser;

    public BashCommand(PrintStream printStream) {
        super(printStream);
    }

    public void execute() {
        if (helpFlag) {
            printUsageInfo("completion-" + BallerinaCliCommands.BASH);
            return;
        }

        if (listCommands == null) {
            getBashContent(getPrintStream());
            return;
        }

        if (listCommands.size() > 0) {
            throw ErrorUtil.createDistSubCommandUsageExceptionWithHelp("too many arguments", BallerinaCliCommands.BASH);
        }
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.BASH;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    private static void getBashContent(PrintStream printStream) {
        String bashContent;
        try {
            bashContent = ToolUtil.readFileAsString("completion-script/bal_completion.bash");
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("failed to read the file: " + e.getMessage());
        }
        printStream.println(bashContent);
    }
}
