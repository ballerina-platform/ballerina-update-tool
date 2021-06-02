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

package org.ballerinalang.command.test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Update tool Tests
 *
 * @since 2.0.0
 */
public class UpdateToolTest {

    @BeforeClass
    public void setupDistributions() {
        TestUtils.cleanDistribution();
        TestUtils.prepareDistribution();
    }

    @Test(description = "Test version command.")
    public void testVersionCommand() throws IOException, InterruptedException {
        String versionOutput = TestUtils.testInstallation();
        Assert.assertTrue(versionOutput.contains("Update Tool " + TestUtils.MAVEN_VERSION));
    }

    @Test(description = "Test dist pull command.", dependsOnMethods = {"testVersionCommand"})
    public void testPullCommand() throws IOException, InterruptedException {
        List<String> args = new LinkedList<>();
        String output;
        args.add(TestUtils.PATH_ARG);
        args.add("dist");
        args.add("pull");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("a distribution must be specified to pull"));

        args.add("--test");
        args.add("1.2.3");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("Fetching the '1.2.3' distribution from the remote server"));
        Assert.assertTrue(output.contains("Fetching the dependencies for '1.2.3' from the remote server"));
        Assert.assertTrue(output.contains("successfully set as the active distribution"));
        Assert.assertTrue(Files.isDirectory(TestUtils.getDistPath("1.2.3")));
        output = TestUtils.testInstallation();
        Assert.assertEquals(output, TestUtils.getVersionOutput("1.2.3", "2020R1",
                TestUtils.MAVEN_VERSION, "1.2.3"));

        args.remove(args.size() - 1);
        args.add("slp1");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("Fetching the 'slp1' distribution from the remote server"));
        Assert.assertTrue(output.contains("Fetching the dependencies for 'slp1' from the remote server"));
        Assert.assertTrue(output.contains("successfully set as the active distribution"));
        Assert.assertTrue(Files.isDirectory(TestUtils.getDistPath("slp1")));
        output = TestUtils.testInstallation();
        Assert.assertEquals(output, TestUtils.getVersionOutput("swan-lake-preview1", "v2020-06-18",
                TestUtils.MAVEN_VERSION, "Preview 1"));

        args.add("1.2.5");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("too many arguments"));
    }

    @Test(description = "Test help command.", dependsOnMethods = {"testPullCommand"})
    public void testHelpCommand() throws IOException, InterruptedException {
        List<String> args = new LinkedList<>();
        String output;
        args.add(TestUtils.PATH_ARG);
        args.add("-h");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("dist            Manage Ballerina distributions"));
        Assert.assertTrue(output.contains("update          Update the Ballerina tool"));

        args.remove(args.size() - 1);
        args.add("dist");
        args.add("-h");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("update     Update to the latest patch version of the active distribution"));
        Assert.assertTrue(output.contains("pull       Fetch a distribution and set it as the active version"));

        args.remove(args.size() - 1);
        args.add("pull");
        args.add("-h");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("bal-dist-pull - Fetch a given distribution and set it as the active version"));
    }

    @Test(description = "Test dist update command.", dependsOnMethods = {"testPullCommand"})
    public void testUpdateCommand() throws IOException, InterruptedException {
        List<String> args = new LinkedList<>();
        String output;
        args.add(TestUtils.PATH_ARG);
        args.add("dist");
        args.add("update");
        args.add("--test");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("Fetching the latest patch distribution for 'ballerina-slp1' from the remote server..."));
        Assert.assertTrue(output.contains("Successfully set the latest patch distribution"));
        Assert.assertTrue(Files.isDirectory(TestUtils.getDistPath("slalpha5")));
        output = TestUtils.testInstallation();
        Assert.assertEquals(output, TestUtils.getVersionOutput("swan-lake-alpha5", "v2020-12-17",
                TestUtils.MAVEN_VERSION, "Alpha 5"));

        List<String> useArgs = new LinkedList<>();
        useArgs.add(TestUtils.PATH_ARG);
        useArgs.add("dist");
        useArgs.add("use");
        useArgs.add("1.2.3");
        output = TestUtils.executeCommand(useArgs);
        Assert.assertTrue(output.contains("successfully set as the active distribution"));

        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("Fetching the latest patch distribution"));
        Assert.assertTrue(output.contains("Successfully set the latest patch distribution"));
        Assert.assertTrue(Files.isDirectory(TestUtils.getDistPath("1.2.13")));
        output = TestUtils.testInstallation();
        Assert.assertEquals(output, TestUtils.getVersionOutput("1.2.13", "2020R1",
                TestUtils.MAVEN_VERSION, "1.2.13"));

        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("is already the active distribution"));
        args.add("arg1");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("too many arguments"));
    }

    @Test(description = "Test dist use command.", dependsOnMethods = {"testUpdateCommand"})
    public void testUseCommand() throws IOException, InterruptedException {
        List<String> args = new LinkedList<>();
        String output;
        args.add(TestUtils.PATH_ARG);
        args.add("dist");
        args.add("use");
        args.add("slalpha5");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("successfully set as the active distribution"));

        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("is the current active distribution version"));

        args.remove(args.size() - 1);
        args.add("slp3");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("not found"));

        args.add("arg1");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("too many arguments"));
    }


    @Test(description = "Test dist list command.", dependsOnMethods = {"testUseCommand"})
    public void testListCommand() throws IOException, InterruptedException {
        List<String> args = new LinkedList<>();
        String output;
        args.add(TestUtils.PATH_ARG);
        args.add("dist");
        args.add("list");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("Distributions available locally"));
        Assert.assertTrue(output.contains("* [slalpha5] Alpha 5"));
        Assert.assertTrue(output.contains("Distributions available remotely"));
        Assert.assertTrue(output.contains("1.* channel"));
        Assert.assertTrue(output.contains("[1.0.1] jballerina version 1.0.1"));
        Assert.assertTrue(output.contains("Swan Lake channel"));
        Assert.assertTrue(output.contains("[slp5] Preview 5"));
        Assert.assertTrue(output.contains("[slalpha1] Alpha 1"));

        args.add("arg1");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("too many arguments"));
    }

    @Test(description = "Test dist remove command.", dependsOnMethods = {"testUseCommand"})
    public void testRemoveCommand() throws IOException, InterruptedException {
        List<String> args = new LinkedList<>();
        String output;
        args.add(TestUtils.PATH_ARG);
        args.add("dist");
        args.add("remove");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("a distribution or `--all, -a` must be specified to remove"));

        args.add("slp2");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("distribution 'slp2' not found"));

        args.remove(args.size() - 1);
        args.add("slalpha5");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("The active Ballerina distribution cannot be removed"));

        args.remove(args.size() - 1);
        args.add("1.2.3");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("successfully removed"));
        Assert.assertFalse(Files.exists(TestUtils.getDistPath("1.2.3")));

        args.add("arg1");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("too many arguments"));

        args.remove(args.size() - 1);
        args.remove(args.size() - 1);
        args.add("-a");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("All non-active distributions are successfully removed"));
        Assert.assertTrue(Files.exists(TestUtils.getDistPath("slalpha5")));
        Assert.assertFalse(Files.exists(TestUtils.getDistPath("slp1")));
        Assert.assertFalse(Files.exists(TestUtils.getDistPath("1.2.13")));

        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("There is nothing to remove. Only active distribution is remaining"));
    }
}
