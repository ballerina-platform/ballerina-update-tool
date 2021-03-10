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

import org.ballerinalang.command.cmd.BashCommand;
import org.ballerinalang.command.cmd.BuildCommand;
import org.ballerinalang.command.cmd.CompletionCommand;
import org.ballerinalang.command.cmd.HelpCommand;
import org.ballerinalang.command.cmd.ListCommand;
import org.ballerinalang.command.cmd.VersionCommand;
import org.ballerinalang.command.util.OSUtils;
import org.junit.Before;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

public class CommandTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private PrintStream testStream = new PrintStream(outContent);

    @Before
    public void setUpStreams() {
        System.setOut(testStream);
    }

    @Test
    public void versionCommandTest() {
        VersionCommand versionCommand = new VersionCommand(testStream);
        versionCommand.execute();
        Assert.assertTrue(outContent.toString().contains("Update Tool"));
        //TODO : Null is returned as implementation version, this needs to fixed
    }

    @Test
    public void listCommandTest() {
        ListCommand listCommand = new ListCommand(testStream);
        listCommand.execute();
        Assert.assertTrue(outContent.toString().contains("Distributions available locally"));
        //TODO : listings are not shown
    }

    @Test
    public void buildCommandTest() {
        //Cleanup the notice file of exists
        File noticeFile = new File(OSUtils.getUpdateNoticePath());
        if (noticeFile.exists()) {
            noticeFile.delete();
        }

        BuildCommand buildCommand = new BuildCommand(testStream);
        buildCommand.execute();
        Assert.assertTrue(outContent.toString().contains("A new version of Ballerina is available:"));

        //Cleanup the notice file created by executing this test method
        if (noticeFile.exists()) {
            noticeFile.delete();
        }
    }

    @Test
    public void helpCommandTest() {
        HelpCommand helpCommand = new HelpCommand();
        helpCommand.setPrintStream(testStream);
        helpCommand.execute();
        Assert.assertTrue(outContent.toString().contains("dist            Manage Ballerina distributions"));
        Assert.assertTrue(outContent.toString().contains("update          Update the Ballerina tool"));
    }

    @Test
    public void completionCommandTest() {
        CompletionCommand completionCommand = new CompletionCommand(testStream);
        completionCommand.execute();
        Assert.assertTrue(outContent.toString().contains("bal-completion"));
    }

    @Test
    public void bashCommandTest() {
        BashCommand bashCommand = new BashCommand(testStream);
        bashCommand.execute();
        Assert.assertTrue(outContent.toString().contains("#!/bin/bash"));
    }
}
