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

import org.ballerinalang.command.cmd.VersionCommand;
import org.ballerinalang.command.exceptions.CommandException;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

/**
 * Test cases for version command.
 *
 * @since 2.0.0
 */
public class VersionCommandTest extends CommandTest{
    @Test
    public void versionCommandTest() {
        VersionCommand versionCommand = new VersionCommand(testStream);
        versionCommand.execute();
        Assert.assertTrue(outContent.toString().contains("Update Tool " + System.getProperty("COMMAND_VERSION")));
    }

    @Test
    public void versionCommandwithArgs() {
        try {
            VersionCommand versionCommand = new VersionCommand(testStream);
            new CommandLine(versionCommand).parse("-v", "arg1");
            versionCommand.execute();
        } catch (CommandException e) {
            Assert.assertTrue(e.getMessages().get(0).contains("too many arguments"));
        }
    }
}
