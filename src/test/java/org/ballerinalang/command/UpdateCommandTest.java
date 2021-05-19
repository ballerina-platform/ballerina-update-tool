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
import org.ballerinalang.command.exceptions.CommandException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

/**
 * Test cases dist update command.
 *
 * @since 2.0.0
 */
public class UpdateCommandTest extends CommandTest {
    @Test
    public void updateCommandHelpTest() {
        UpdateCommand updateCommand = new UpdateCommand(testStream);
        new CommandLine(updateCommand).parse("-h");
        updateCommand.execute();
        Assert.assertTrue(outContent.toString().contains("Update to the latest patch version of the active distribution"));
    }

    @Test(expectedExceptions = { CommandException.class })
    public void UpdateCommandWithArgTest() throws CommandException {
        UpdateCommand updateCommand = new UpdateCommand(testStream);
        new CommandLine(updateCommand).parse("arg1");
        updateCommand.execute();
    }

    @Test
    public void updateCommandTest() {
        PullCommand pullCommand = new PullCommand(testStream);
        new CommandLine(pullCommand).parse("slp1");
        pullCommand.execute();
        UpdateCommand updateCommand = new UpdateCommand(testStream);
        new CommandLine(updateCommand).parse();
        updateCommand.execute();
        Assert.assertTrue(outContent.toString().contains("Fetching the latest patch distribution"));
        Assert.assertTrue(outContent.toString().contains("Successfully set the latest patch distribution"));

        UpdateCommand updateCmd = new UpdateCommand(testStream);
        new CommandLine(updateCmd).parse();
        updateCmd.execute();
        Assert.assertTrue(outContent.toString().contains("is already the active distribution"));
    }
}
