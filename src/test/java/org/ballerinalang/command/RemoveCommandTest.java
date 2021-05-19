/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.command;

import org.ballerinalang.command.cmd.PullCommand;
import org.ballerinalang.command.cmd.RemoveCommand;
import org.ballerinalang.command.cmd.UpdateCommand;
import org.ballerinalang.command.exceptions.CommandException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

/**
 * Test cases for dist remove command.
 *
 * @since 2.0.0
 */
public class RemoveCommandTest extends CommandTest {

    @Test(expectedExceptions = { CommandException.class })
    public void removeCommandwithoutArgsTest() throws CommandException {
        RemoveCommand removeCommand = new RemoveCommand(testStream);
        new CommandLine(removeCommand).parse();
        removeCommand.execute();
    }

    @Test(expectedExceptions = { CommandException.class })
    public void removeCommandwithMultipleArgsTest() throws CommandException {
        RemoveCommand removeCommand = new RemoveCommand(testStream);
        new CommandLine(removeCommand).parse("arg1", "arg2");
        removeCommand.execute();
    }

    @Test
    public void removeCommandHelpTest() throws CommandException {
        RemoveCommand removeCommand = new RemoveCommand(testStream);
        new CommandLine(removeCommand).parse("-h");
        removeCommand.execute();
        Assert.assertTrue(outContent.toString().contains("Remove distributions in your local environment"));
    }

    @Test(expectedExceptions = { CommandException.class })
    public void removeActiveDistTest() throws CommandException {
        PullCommand pullCommand = new PullCommand(testStream);
        new CommandLine(pullCommand).parse("1.2.0");
        pullCommand.execute();

        RemoveCommand removeCommand = new RemoveCommand(testStream);
        new CommandLine(removeCommand).parse("1.2.0");
        removeCommand.execute();
        Assert.assertTrue(outContent.toString().contains("The active Ballerina distribution cannot be removed"));
    }

    @Test
    public void removeCommandTest() throws CommandException {
        PullCommand pullCommand = new PullCommand(testStream);
        new CommandLine(pullCommand).parse("1.2.0");
        pullCommand.execute();
        UpdateCommand updateCommand = new UpdateCommand(testStream);
        new CommandLine(updateCommand).parse();
        updateCommand.execute();

        PullCommand pullCmd = new PullCommand(testStream);
        new CommandLine(pullCmd).parse("slp1");
        pullCmd.execute();
        updateCommand.execute();

        RemoveCommand removeCommand2 = new RemoveCommand(testStream);
        new CommandLine(removeCommand2).parse("1.2.0");
        removeCommand2.execute();
        Assert.assertTrue(outContent.toString().contains("successfully removed"));

        RemoveCommand removeCommand3 = new RemoveCommand(testStream);
        new CommandLine(removeCommand3).parse("-a");
        removeCommand3.execute();
        Assert.assertTrue(outContent.toString().contains("All non-active distributions are successfully removed"));

        RemoveCommand removeCommand4 = new RemoveCommand(testStream);
        new CommandLine(removeCommand4).parse("-a");
        removeCommand4.execute();
        Assert.assertTrue(outContent.toString().contains("There is nothing to remove. Only active distribution is remaining"));
    }

    @Test(expectedExceptions = { CommandException.class })
    public void removeAllCommandwithMultipleArgsTest() throws CommandException {
        RemoveCommand removeCommand = new RemoveCommand(testStream);
        new CommandLine(removeCommand).parse("-a", "arg1");
        removeCommand.execute();
    }
}
