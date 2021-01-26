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

package org.ballerinalang.command.util;

import org.ballerinalang.command.BallerinaCliCommands;
import org.ballerinalang.command.exceptions.CommandException;

import java.io.PrintStream;
import java.util.List;

/**
 * Class contains utility methods for ballerina commands error handling.
 *
 * @since 0.8.0
 */
public class ErrorUtil {

    public static CommandException createCommandException(String errorMsg) {
        CommandException launcherException = new CommandException();
        launcherException.addMessage("ballerina: " + errorMsg);
        return launcherException;
    }

    public static CommandException createUsageExceptionWithHelp(String errorMsg) {
        CommandException launcherException = new CommandException();
        launcherException.addMessage("ballerina: " + errorMsg);
        launcherException.addMessage("Run 'bal help' for usage.");
        return launcherException;
    }

    public static CommandException createUsageExceptionWithHelp(String errorMsg, String subCommand) {
        CommandException launcherException = new CommandException();
        launcherException.addMessage("ballerina: " + errorMsg);
        launcherException.addMessage("Run 'bal help " + subCommand + "' for usage.");
        return launcherException;
    }

    public static CommandException createDistSubCommandUsageExceptionWithHelp(String errorMsg, String subCommand) {
        return createUsageExceptionWithHelp(errorMsg, BallerinaCliCommands.DIST + " " + subCommand);
    }

    public static CommandException createDistributionNotFoundException(String distribution) {
        return createCommandException("distribution '" + distribution + "' not found");
    }

    public static CommandException createDependencyNotFoundException(String dependency) {
        return createCommandException("dependency '" + dependency + "' not found");
    }

    public static CommandException createDistributionRequiredException(String operation) {
        return createDistSubCommandUsageExceptionWithHelp("a distribution must be specified to " + operation,
                operation);
    }

    public static CommandException createDistributionRequiredException(String operation, String flags) {
        return createDistSubCommandUsageExceptionWithHelp("a distribution or `" + flags +
                "` must be specified to " + operation, operation);
    }

    public static void printLauncherException(CommandException e, PrintStream outStream) {
        List<String> errorMessages = e.getMessages();
        errorMessages.forEach(outStream::println);
    }
}
