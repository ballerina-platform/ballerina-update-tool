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
package org.ballerinalang.command;

import org.ballerinalang.command.cmd.HelpCommand;
import org.ballerinalang.command.exceptions.CommandException;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

/**
 * Test cases for help command.
 *
 * @since 2.0.0
 */
public class HelpCommandTest extends CommandTest {
    @Test
    public void helpCommandTest() {
        HelpCommand helpCommand = new HelpCommand();
        helpCommand.setPrintStream(testStream);
        helpCommand.execute();
        Assert.assertTrue(outContent.toString().contains("dist            Manage Ballerina distributions"));
        Assert.assertTrue(outContent.toString().contains("update          Update the Ballerina tool"));
    }

    @Test
    public void helpCommandWithOneArgTest() {
        HelpCommand helpCommand = new HelpCommand();
        helpCommand.setPrintStream(testStream);
        new CommandLine(helpCommand).parse("dist");
        helpCommand.execute();
        Assert.assertTrue(outContent.toString().contains("update     Update to the latest patch version of the active distribution"));
        Assert.assertTrue(outContent.toString().contains("pull       Fetch a distribution and set it as the active version"));
    }

    @Test
    public void helpCommandWithTwoArgsTest() {
        HelpCommand helpCommand = new HelpCommand();
        helpCommand.setPrintStream(testStream);
        new CommandLine(helpCommand).parse("dist", "pull");
        helpCommand.execute();
        Assert.assertTrue(outContent.toString().contains("bal-dist-pull - Fetch a given distribution and set it as the active version"));
    }

    @Test(expectedExceptions = { CommandException.class })
    public void helpCommandWithMultipleArgsTest() {
        HelpCommand helpCommand = new HelpCommand();
        helpCommand.setPrintStream(testStream);
        new CommandLine(helpCommand).parse("arg1", "arg2", "arg3");
        helpCommand.execute();
    }
}
