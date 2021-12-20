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
import org.ballerinalang.command.cmd.UpdateCommand;
import org.ballerinalang.command.cmd.UseCommand;
import org.ballerinalang.command.exceptions.CommandException;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

/**
 * Test cases for dist use command.
 *
 * @since 2.0.0
 */
public class UseCommandTest extends CommandTest {

    @Test
    public void useCommandTest() {
        PullCommand pullCommand = new PullCommand(testStream);
        new CommandLine(pullCommand).parse("slp1");
        pullCommand.execute();
        UpdateCommand updateCommand = new UpdateCommand(testStream);
        new CommandLine(updateCommand).parse();
        updateCommand.execute();

        UseCommand useCommand = new UseCommand(testStream);
        new CommandLine(useCommand).parse("slp1");
        useCommand.execute();
        Assert.assertTrue(outContent.toString().contains("successfully set as the active distribution"));

        useCommand.execute();
        Assert.assertTrue(outContent.toString().contains("is the current active distribution version"));

        UseCommand useCmd = new UseCommand(testStream);
        new CommandLine(useCmd).parse("slp3");
        useCmd.execute();
        Assert.assertTrue(outContent.toString().contains("not found"));

        UseCommand useCommandInvalidDist = new UseCommand(testStream);
        new CommandLine(useCommandInvalidDist).parse("slbeta7");
        useCommandInvalidDist.execute();
        Assert.assertTrue(outContent.toString().contains("not a valid distribution"));
    }

    @Test
    public void useCommandwithMultipleArgsTest() {
        try {
            UseCommand useCommand = new UseCommand(testStream);
            new CommandLine(useCommand).parse("arg1", "arg2");
            useCommand.execute();
        } catch (CommandException e) {
            Assert.assertTrue(e.getMessages().get(0).contains("too many arguments"));
        }
    }

    @Test
    public void useCommandwithoutArgsTest() {
        try {
            UseCommand useCommand = new UseCommand(testStream);
            new CommandLine(useCommand).parse();
            useCommand.execute();
        } catch (CommandException e) {
            Assert.assertTrue(e.getMessages().get(0).contains("a distribution must be specified to use"));
        }
    }

    @Test
    public void useCommandHelpTest() {
        UseCommand useCommand = new UseCommand(testStream);
        new CommandLine(useCommand).parse("-h");
        useCommand.execute();
        Assert.assertTrue(outContent.toString().contains("Mark a distribution as the active distribution"));
    }
}
