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

package org.ballerinalang.distribution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    String swanLakeVersion = System.getProperty("swan-lake-version");
    String swanLakeSpecVersion = System.getProperty("swan-lake-spec-version");
    String swanLakeLatestVersion = System.getProperty("swan-lake-latest-version");
    String swanLakeLatestSpecVersion = System.getProperty("swan-lake-latest-spec-version");
    String previouschannelVersion = System.getProperty("1-x-channel-version");
    String previousChannelSpecVersion = System.getProperty("1-x-channel-spec-version");
    String previousChanneLatestVersion = System.getProperty("1-x-channel-latest-version");

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
        List<String> args = TestUtils.addPathArg();
        String output;
        args.add("dist");
        args.add("pull");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("a distribution must be specified to pull"));

        args.add("--test");
        args.add(previouschannelVersion);
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("Fetching the '" + previouschannelVersion +
                "' distribution from the remote server"));
        Assert.assertTrue(output.contains("Fetching the dependencies for '" + previouschannelVersion +
                "' from the remote server"));
        Assert.assertTrue(output.contains("successfully set as the active distribution"));
        Assert.assertTrue(Files.isDirectory(TestUtils.getDistPath(previouschannelVersion)));
        output = TestUtils.testInstallation();
        Assert.assertEquals(output, TestUtils.getVersionOutput(previouschannelVersion, previousChannelSpecVersion,
                TestUtils.MAVEN_VERSION, previouschannelVersion));

        args.remove(args.size() - 1);
        args.add(swanLakeVersion);
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("Fetching the '" + swanLakeVersion + "' distribution from the remote server"));
        Assert.assertTrue(output.contains("Fetching the dependencies for '" + swanLakeVersion +
                "' from the remote server"));
        Assert.assertTrue(output.contains("successfully set as the active distribution"));
        Assert.assertTrue(Files.isDirectory(TestUtils.getDistPath(swanLakeVersion)));
        output = TestUtils.testInstallation();
        Assert.assertEquals(output, TestUtils.getVersionOutput(swanLakeVersion, swanLakeSpecVersion,
                TestUtils.MAVEN_VERSION, TestUtils.getDisplayText(swanLakeVersion)));

        args.add("1.2.5");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("too many arguments"));
    }

    @Test(description = "Build and run a project", dependsOnMethods = {"testPullCommand"})
    public void projectTest() throws IOException, InterruptedException {
        List<String> args = TestUtils.addPathArg();
        String output;
        args.add("new");
        args.add("project1");
        TestUtils.executeCommand(args);
        Path projectPath = TestUtils.TEST_DIR.resolve("project1");
        Assert.assertTrue(Files.exists(projectPath));

        args.remove(args.size() - 1);
        args.remove(args.size() - 1);
        args.add("add");
        args.add("module1");
        TestUtils.executeCommand(args, projectPath);
        Assert.assertTrue(Files.isDirectory(projectPath.resolve("src").resolve("module1")));

        args.remove(args.size() - 2);
        args.add(args.size() - 1, "build");
        output = TestUtils.executeCommand(args, projectPath);
        Assert.assertTrue(output.contains("Compiling source"));
        Assert.assertTrue(output.contains("Creating balos"));
        Assert.assertTrue(output.contains("Running Tests"));
        Assert.assertTrue(output.contains("Generating executables"));

        Assert.assertTrue(Files.exists(projectPath.resolve("target/bin/module1.jar")));

        args.remove(args.size() - 2);
        args.add(args.size() - 1, "run");
        output = TestUtils.executeCommand(args, projectPath);
        Assert.assertTrue(output.contains("Hello World!"));
    }

    @Test(description = "Test help command.")
    public void testHelpCommand() throws IOException, InterruptedException {
        List<String> args = TestUtils.addPathArg();
        String output;
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

    @Test(description = "Test dist update command.", dependsOnMethods = {"testPullCommand", "projectTest"})
    public void testUpdateCommand() throws IOException, InterruptedException {
        List<String> args = TestUtils.addPathArg();
        String output;
        args.add("dist");
        args.add("update");
        args.add("--test");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("Fetching the latest patch distribution for 'ballerina-" + swanLakeVersion +
                "' from the remote server..."));
        Assert.assertTrue(output.contains("Successfully set the latest patch distribution"));
        Assert.assertTrue(Files.isDirectory(TestUtils.getDistPath(swanLakeLatestVersion)));
        output = TestUtils.testInstallation();
        Assert.assertEquals(output, TestUtils.getVersionOutput(swanLakeLatestVersion, swanLakeLatestSpecVersion,
                TestUtils.MAVEN_VERSION, TestUtils.getDisplayText(swanLakeLatestVersion)));

        List<String> useArgs = TestUtils.addPathArg();
        useArgs.add("dist");
        useArgs.add("use");
        useArgs.add(previouschannelVersion);
        output = TestUtils.executeCommand(useArgs);
        Assert.assertTrue(output.contains("successfully set as the active distribution"));

        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("Fetching the latest patch distribution for 'jballerina-" +
                previouschannelVersion + "' from the remote server..."));
        Assert.assertTrue(output.contains("Successfully set the latest patch distribution"));
        Assert.assertTrue(Files.isDirectory(TestUtils.getDistPath(previousChanneLatestVersion)));
        output = TestUtils.testInstallation();
        Assert.assertEquals(output, TestUtils.getVersionOutput(previousChanneLatestVersion,
                previousChannelSpecVersion, TestUtils.MAVEN_VERSION, previousChanneLatestVersion));

        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("is already the active distribution"));
        args.add("arg1");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("too many arguments"));
    }

    @Test(description = "Test dist use command.", dependsOnMethods = {"testUpdateCommand"})
    public void testUseCommand() throws IOException, InterruptedException {
        List<String> args = TestUtils.addPathArg();
        String output;
        args.add("dist");
        args.add("use");
        args.add(swanLakeLatestVersion);
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("successfully set as the active distribution"));

        output = TestUtils.testInstallation();
        Assert.assertEquals(output, TestUtils.getVersionOutput(swanLakeLatestVersion, swanLakeLatestSpecVersion,
                TestUtils.MAVEN_VERSION, TestUtils.getDisplayText(swanLakeLatestVersion)));

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

    @Test(description = "Build and run a project", dependsOnMethods = {"testUseCommand"})
    public void projectTestWithLatestSpec() throws IOException, InterruptedException {
        List<String> args = TestUtils.addPathArg();
        String output;
        args.add("new");
        args.add("project2");
        TestUtils.executeCommand(args);
        Path projectPath = TestUtils.TEST_DIR.resolve("project2");
        Assert.assertTrue(Files.exists(projectPath));

        args.remove(args.size() - 1);
        args.remove(args.size() - 1);
        args.add("add");
        args.add("module1");
        TestUtils.executeCommand(args, projectPath);
        Assert.assertTrue(Files.isDirectory(projectPath.resolve("modules").resolve("module1")));

        args.remove(args.size() - 1);
        args.remove(args.size() - 1);
        args.add("build");
        output = TestUtils.executeCommand(args, projectPath);
        Assert.assertTrue(output.contains("Compiling source"));
        Assert.assertTrue(output.contains("Generating executable"));

        Assert.assertTrue(Files.exists(projectPath.resolve("target/bin/project2.jar")));

        args.remove(args.size() - 1);
        args.add("run");
        output = TestUtils.executeCommand(args, projectPath);
        Assert.assertTrue(output.contains("Hello, World!"));
    }

    @Test(description = "Test dist list command.", dependsOnMethods = {"testUseCommand"})
    public void testListCommand() throws IOException, InterruptedException {
        List<String> args = TestUtils.addPathArg();
        String output;
        args.add("dist");
        args.add("list");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("Distributions available locally"));
        Assert.assertTrue(output.contains("* " + swanLakeLatestVersion));
        Assert.assertTrue(output.contains("Distributions available remotely"));
        Assert.assertTrue(output.contains("1.* channel"));
        Assert.assertTrue(output.contains("1.2.16"));
        Assert.assertTrue(output.contains("Swan Lake channel"));
//        Assert.assertTrue(output.contains("slp5"));            Should be added after the release
//        Assert.assertTrue(output.contains("[slalpha1] Alpha 1"));

        args.add("arg1");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("too many arguments"));
    }

    @Test(description = "Test dist remove command.", dependsOnMethods = {"testUseCommand", "projectTestWithLatestSpec"})
    public void testRemoveCommand() throws IOException, InterruptedException {
        List<String> args = TestUtils.addPathArg();
        String output;
        args.add("dist");
        args.add("remove");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("a distribution or `--all, -a` must be specified to remove"));

        args.add("slp2");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("distribution 'slp2' not found"));

        args.remove(args.size() - 1);
        args.add(swanLakeLatestVersion);
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("The active Ballerina distribution cannot be removed"));

        args.remove(args.size() - 1);
        args.add(previouschannelVersion);
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("successfully removed"));
        Assert.assertFalse(Files.exists(TestUtils.getDistPath(previouschannelVersion)));

        args.add("arg1");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("too many arguments"));

        args.remove(args.size() - 1);
        args.remove(args.size() - 1);
        args.add("-a");
        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("All non-active distributions are successfully removed"));
        Assert.assertTrue(Files.exists(TestUtils.getDistPath(swanLakeLatestVersion)));
        Assert.assertFalse(Files.exists(TestUtils.getDistPath(swanLakeVersion)));
        Assert.assertFalse(Files.exists(TestUtils.getDistPath(previousChanneLatestVersion)));

        output = TestUtils.executeCommand(args);
        Assert.assertTrue(output.contains("There is nothing to remove. Only active distribution is remaining"));
    }
}
