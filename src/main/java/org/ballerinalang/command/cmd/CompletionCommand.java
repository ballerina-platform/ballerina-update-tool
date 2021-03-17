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
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * This class represents the "Completion" command and it holds arguments and flags specified by the user.
 */
@CommandLine.Command(name = "completion", description = "Ballerina completion commands")
public class CompletionCommand extends Command implements BCommand {
    @CommandLine.Parameters(description = "Command name")
    private List<String> completionCommands;

    @CommandLine.Option(names = { "--help", "-h", "?" }, hidden = true, description = "for more information")
    private boolean helpFlag;

    public CompletionCommand(PrintStream printStream) {
        super(printStream);
    }

    private CommandLine parentCmdParser;

    @Override
    public void execute() {
        if (helpFlag || completionCommands == null) {
            printUsageInfo(BallerinaCliCommands.COMPLETION);
            return;
        }

        if (completionCommands.size() > 1) {
            throw ErrorUtil.createUsageExceptionWithHelp("too many arguments", BallerinaCliCommands.COMPLETION);
        }

        throw ErrorUtil.createUsageExceptionWithHelp("unknown command '" + completionCommands.get(0) + "'",
                BallerinaCliCommands.COMPLETION);
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.COMPLETION;
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
}
