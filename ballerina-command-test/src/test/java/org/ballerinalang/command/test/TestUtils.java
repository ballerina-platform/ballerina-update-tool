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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for tests
 *
 * @since 2.0.0
 */
public class TestUtils {
    public static final PrintStream OUT = System.out;
    public static final Path TARGET_DIR = Paths.get(System.getProperty("target.dir"));
    public static final String MAVEN_VERSION = System.getProperty("maven.version");
    public static final Path DISTRIBUTIONS_DIR = Paths.get(System.getProperty("distributions.dir"));
    public static final Path RESOURCES_DIR = Paths.get(System.getProperty("test.resources.dir"));
    public static final Path TEST_DISTRIBUTION_PATH = TARGET_DIR.resolve("test-distribution");
    private static final String DIST_NAME = "ballerina-command-" + MAVEN_VERSION;
    private static final String SWAN_LAKE_KEYWORD = "swan-lake";
    public static final String PATH_ARG = TEST_DISTRIBUTION_PATH.resolve(DIST_NAME).resolve("bin").resolve("bal").
            toString();
    private static final Path DIST_PATH = TEST_DISTRIBUTION_PATH.resolve(DIST_NAME).resolve("distributions");
    public static final Path TEST_DIR = TEST_DISTRIBUTION_PATH.resolve("test");

    /**
     * Execute ballerina build command.
     *
     * @param args The arguments to be passed to the build command.
     * @return Output content of the command execution
     * @throws IOException          Error executing build command.
     * @throws InterruptedException Interrupted error executing build command.
     */
    protected static String executeCommand(List<String> args) throws IOException, InterruptedException {
        return executeCommand(args, TEST_DIR);
    }

    /**
     * Execute ballerina build command.
     *
     * @param args The arguments to be passed to the build command.
     * @param testDir Directory where the command should be executed.
     * @return Output content of the command execution
     * @throws IOException          Error executing build command.
     * @throws InterruptedException Interrupted error executing build command.
     */
    protected static String executeCommand(List<String> args, Path testDir) throws IOException, InterruptedException {
        addExecutablePermissionToFile(TEST_DISTRIBUTION_PATH.resolve(DIST_NAME).resolve("bin").resolve("bal").toFile());
        OUT.println("Executing: " + String.join(" ", args));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(testDir.toFile());
        Process process = pb.start();
        int exitCode = process.waitFor();
        InputStream inputStream = process.getInputStream();;
        String output = "";
        if (exitCode != 0) {
            inputStream = process.getErrorStream();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            output += line + "\n";
        }
        logOutput(inputStream);
        return  output;
    }

    /**
     * Log the output of an input stream.
     *
     * @param inputStream The stream.
     * @throws IOException Error reading the stream.
     */
    private static void logOutput(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            br.lines().forEach(OUT::println);
        }
    }

    /**
     * Extracts a distribution to a temporary directory.
     */
    public static void prepareDistribution() {
        File toolUnzipLocation = TEST_DISTRIBUTION_PATH.toFile();
        if (toolUnzipLocation.exists()) {
            toolUnzipLocation.delete();
        }
        toolUnzipLocation.mkdir();
        Path zipFileLocation = DISTRIBUTIONS_DIR.resolve(DIST_NAME + ".zip");
        unzip(zipFileLocation.toString(), toolUnzipLocation.toString());
        copyFiles();
    }

    private static void unzip(String zipFilePath, String destDirectory) {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                    byte[] bytesIn = new byte[1024];
                    int read;
                    while ((read = zipIn.read(bytesIn)) != -1) {
                        bos.write(bytesIn, 0, read);
                    }
                    bos.close();
                } else {
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        } catch (IOException e) {
            OUT.println("failed to unzip zip the file in '" + zipFilePath + "' to '" + destDirectory + "'");
        }
    }

    /**
     * Copy version files to distributions.
     */
    private static void copyFiles() {
        try {
            Files.createDirectory(TEST_DIR);
            Files.createDirectory(DIST_PATH);
            Files.copy(RESOURCES_DIR.resolve("ballerina-version"), DIST_PATH.resolve("ballerina-version"));
            Files.copy(RESOURCES_DIR.resolve("installer-version"), DIST_PATH.resolve("installer-version"));
        } catch (IOException e) {
            OUT.println("failed to copy files to '" + DIST_PATH + "'");
        }
    }

    /**
     * Delete the temporary directory used to extract distributions.
     */
    public static void cleanDistribution() {
        TEST_DISTRIBUTION_PATH.toFile().delete();
    }

    /**
     * Add read, write, and executable permission to the given file.
     *
     * @param file file path
     */
    private static void addExecutablePermissionToFile(File file) {
        file.setReadable(true, false);
        file.setExecutable(true, false);
        file.setWritable(true, false);
    }

    /**
     * Execute version command and get output.
     *
     * @return version command output
     */
    public static String testInstallation() throws IOException, InterruptedException {
        List<String> args = new LinkedList<>();
        args.add(PATH_ARG);
        args.add("-v");
        return executeCommand(args);
    }

    /**
     * Get version output for version command.
     *  @param ballerinaVersion Installed jBallerina version
     *  @param specVersion Installed language specification
     *  @param toolVersion Installed tool version
     *  @param versionDisplayText display text for installed jBallerina version
     *
     * @return version output
     */
    public static String getVersionOutput(String ballerinaVersion, String specVersion, String toolVersion,
                                          String versionDisplayText) {
        String toolText = TestUtils.isOldToolVersion(toolVersion) ? "Ballerina tool" : "Update Tool";
        if (ballerinaVersion.contains("sl")) {
            return "Ballerina Swan Lake " + versionDisplayText + "\n" + "Language specification " + specVersion +
                    "\n" + toolText + " " + toolVersion + "\n";
        }

        String ballerinaReference = isSupportedRelease(ballerinaVersion) ? "jBallerina" : "Ballerina";
        return ballerinaReference + " " + ballerinaVersion + "\n" + "Language specification " + specVersion + "\n" +
                toolText + " " + toolVersion + "\n";
    }

    /**
     * To check whether older tool version before swan lake support
     *
     * @param toolVersion Tool version.
     * @return returns is a older version
     */
    private static boolean isOldToolVersion(String toolVersion) {
        return toolVersion.equals("0.8.5") || toolVersion.equals("0.8.0");
    }

    /**
     * To check whether installation is a 1.0.x release.
     *
     * @return returns is a 1.0.x release
     */
    private static boolean isSupportedRelease(String version) {
        if (version.contains(SWAN_LAKE_KEYWORD)) {
            return true;
        }

        String[] versions = version.split("\\.");
        return !(versions[0].equals("1") && versions[1].equals("0"));
    }

    /**
     * Get distribution path for the version.
     *
     * @return returns distribution path for the version
     */
    public static Path getDistPath (String version) {
        String type = version.contains("sl") ? "ballerina" : "jballerina";
        return DIST_PATH.resolve(type + "-" + version);
    }

    /**
     * Get the display text using distribution version
     *
     * @param version distribution version
     * @return returns display text
     */
    public static String getDisplayText(String version) {
        char lastChar = version.charAt(version.length() - 1);
        if (version.contains("slp")) {
            return "Preview " + lastChar;
        } else {
            String versionId = version.substring(2, version.length() - 1) + " " + lastChar;
            return versionId.substring(0, 1).toUpperCase() + versionId.substring(1);
        }
    }
}
