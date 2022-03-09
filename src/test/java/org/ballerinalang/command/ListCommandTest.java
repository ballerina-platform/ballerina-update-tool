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
import org.ballerinalang.command.util.ToolUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;

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
        Assert.assertTrue(outContent.toString().contains("1.2.20"));
        Assert.assertTrue(outContent.toString().contains("Swan Lake channel"));
    }

    @Test
    public void listCommandHelpTest() {
        ListCommand listCommand = new ListCommand(testStream);
        new CommandLine(listCommand).parse("-h");
        listCommand.execute();
        Assert.assertTrue(outContent.toString().contains("List locally and remotely available distributions"));
    }

    @Test
    public void listAllCommandTest(){
        ListCommand listCommand = new ListCommand(testStream);
        new CommandLine(listCommand).parse("-a");
        listCommand.execute();
        Assert.assertTrue(outContent.toString().contains("1.2.1"));
    }

    @Test
    public void listCommandWithArgsTest() {
        try {
            ListCommand listCommand = new ListCommand(testStream);
            new CommandLine(listCommand).parse("arg1");
            listCommand.execute();
        } catch (CommandException e) {
            Assert.assertTrue(e.getMessages().get(0).contains("too many arguments"));
        }
    }

    @Test
    public void listCommandWithLocalDistributionTest(){
        PullCommand pullCommand = new PullCommand(testStream);
        new CommandLine(pullCommand).parse("1.2.1");
        pullCommand.execute();

        ListCommand listCommand = new ListCommand(testStream);
        listCommand.execute();
        Assert.assertTrue(outContent.toString().contains("1.2.1"));
    }
}
