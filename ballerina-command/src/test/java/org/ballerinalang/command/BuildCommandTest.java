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

import org.ballerinalang.command.cmd.BuildCommand;
import org.ballerinalang.command.util.OSUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test cases for build command.
 *
 * @since 2.0.0
 */
public class BuildCommandTest extends CommandTest {
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
}
