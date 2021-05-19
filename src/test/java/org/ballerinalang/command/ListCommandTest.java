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

import org.ballerinalang.command.cmd.ListCommand;
import org.ballerinalang.command.cmd.PullCommand;
import org.ballerinalang.command.exceptions.CommandException;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

/**
 * Test cases for dist list command.
 *
 * @since 2.0.0
 */
public class ListCommandTest extends CommandTest {

    @Test
    public void listCommandTest() {
        ListCommand listCommand = new ListCommand(testStream);
        listCommand.execute();
        Assert.assertTrue(outContent.toString().contains("Distributions available locally"));
        Assert.assertTrue(outContent.toString().contains("Distributions available remotely"));
        Assert.assertTrue(outContent.toString().contains("1.* channel"));
        Assert.assertTrue(outContent.toString().contains("[1.0.1] jballerina version 1.0.1"));
        Assert.assertTrue(outContent.toString().contains("Swan Lake channel"));
        Assert.assertTrue(outContent.toString().contains("[slp5] Preview 5"));
    }

    @Test
    public void listCommandHelpTest() {
        ListCommand listCommand = new ListCommand(testStream);
        new CommandLine(listCommand).parse("-h");
        listCommand.execute();
        Assert.assertTrue(outContent.toString().contains("List locally and remotely available distributions"));
    }

    @Test(expectedExceptions = { CommandException.class })
    public void listCommandWithArgsTest() {
        ListCommand listCommand = new ListCommand(testStream);
        new CommandLine(listCommand).parse("arg1");
        listCommand.execute();
    }
}
