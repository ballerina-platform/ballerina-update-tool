/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.command.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Utility functions used by tools.
 */
public class OSUtils {

    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.getDefault());
    public static final String BALLERINA_HOME_DIR = ".ballerina";
    private static final String BALLERINA_CONFIG = "ballerina-version";
    private static final String INSTALLER_VERSION = "installer-version";
    public static final String BALLERINA_LIST_JSON = "local-dists.json";
    private static final String UPDATE_NOTICE = "command-notice";
    private static final String BIR_CACHE = "bir_cache";
    private static final String JAR_CACHE = "jar_cache";
    private static final String REPOSITORIES = "repositories";
    private static final String CENTRAL_CACHE_ROOT = "central.ballerina.io";
    private static final String LOCAL_CACHE_ROOT = "local";
    private static final String CACHE = "cache";

    /**
     * Provide the path of configuration file.
     *
     * @return File path
     */
    public static String getInstallationPath() throws URISyntaxException {
        File file = new File(OSUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return file.getParentFile().getParentFile().getPath();
    }

    /**
     * Provide file name of executable for current operating system.
     *
     * @return name of the file
     */
    public static String getExecutableFileName(String distribution) {
        String fileName = OSUtils.isWindows() ? "bal.bat" : "bal";
        File file = new File(ToolUtil.getDistributionsPath() + File.separator + ToolUtil.getType(distribution) + "-" +
                distribution + File.separator + "bin" + File.separator + fileName);
        if (file.exists()) {
            return fileName;
        }
        return OSUtils.isWindows() ? "ballerina.bat" : "ballerina";
    }

    /**
     * Provide file name of tool update install script for current operating system.
     *
     * @return name of the file
     */
    public static String getInstallScriptFileName() {
        return OSUtils.isWindows() ? "install.bat" : "install";
    }

    /**
     * Provide file name of debug adapter script for current operating system.
     *
     * @return name of the file
     */
    public static String getDebugAdapterName() {
        return OSUtils.isWindows() ? "debug-adapter-launcher.bat" : "debug-adapter-launcher.sh";
    }

    /**
     * Provide file name of language server launcher script for current operating system.
     *
     * @return name of the file
     */
    public static String getLangServerLauncherName() {
        return OSUtils.isWindows() ? "language-server-launcher.bat" : "language-server-launcher.sh";
    }

    /**
     * Provides the ballerina home directory path
     *
     * @return path to the ballerina home directory
     */
    public static String getBallerinaHomePath() {
        return getUserHome() + File.separator + BALLERINA_HOME_DIR;
    }

    /**
     * Provides the ballerina version path
     *
     * @return path to the file
     */
    public static String getBallerinaVersionFilePath() throws IOException {
        File ballerinaHome = new File(getBallerinaHomePath());
        File ballerinaVersionfile = new File(ballerinaHome.getPath() + File.separator + BALLERINA_CONFIG);
        if (!ballerinaVersionfile.exists()) {
            ballerinaVersionfile.getParentFile().mkdirs();
            ballerinaVersionfile.createNewFile();
            ToolUtil.addWritePermissionToFile(ballerinaHome);
            ToolUtil.addWritePermissionToFile(ballerinaVersionfile);
            ToolUtil.setVersion(ballerinaVersionfile.getPath(), ToolUtil.getCurrentInstalledBallerinaVersion());
        }
        return ballerinaVersionfile.getPath();
    }

    /**
     * Provides the installer version path
     *
     * @return path to the file
     */
    public static String getInstallerVersionFilePath() throws IOException {
        File ballerinaHome = new File(getBallerinaHomePath());
        File installerVersionfile = new File(ballerinaHome.getPath() + File.separator + INSTALLER_VERSION);

        if (!new File(OSUtils.getInstalledInstallerVersionPath()).exists() && installerVersionfile.exists()) {
            installerVersionfile.delete();
        }

        if (new File(OSUtils.getInstalledInstallerVersionPath()).exists() && !installerVersionfile.exists()) {
            installerVersionfile.getParentFile().mkdirs();
            installerVersionfile.createNewFile();
            ToolUtil.addWritePermissionToFile(ballerinaHome);
            ToolUtil.addWritePermissionToFile(installerVersionfile);
            ToolUtil.setInstallerVersion(installerVersionfile.getPath());
            ToolUtil.setVersion(getBallerinaVersionFilePath(), ToolUtil.getCurrentInstalledBallerinaVersion());
        }
        return installerVersionfile.getPath();
    }

    public static String getBallerinaDistListFilePath() {
        return getUserHome() + File.separator + BALLERINA_HOME_DIR + File.separator + BALLERINA_LIST_JSON;
    }

    /**
     * Provide installer version file path of the installation.
     *
     * @return path to the file
     */
    public static String getInstalledConfigPath() {
        return ToolUtil.getDistributionsPath() + File.separator + OSUtils.BALLERINA_CONFIG;
    }

    /**
     * Provide configuration file path of the installation.
     *
     * @return path to the file
     */
    public static String getInstalledInstallerVersionPath() {
        return ToolUtil.getDistributionsPath() + File.separator + OSUtils.INSTALLER_VERSION;
    }

    /**
     * Provide update notice file path.
     *
     * @return
     */
    public static String getUpdateNoticePath() {
        return getUserHome() + File.separator + BALLERINA_HOME_DIR + File.separator + UPDATE_NOTICE;
    }

    /**
     * Check file and specify notice needs to be shown.
     *
     * @return needs to be shown
     * @throws IOException occurs when reading files
     */
    static boolean updateNotice() throws IOException {
        boolean showNotice;

        LocalDate today = LocalDate.now();
        File file = new File(getUpdateNoticePath());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            showNotice = true;
        } else {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(file.getPath()))) {
                LocalDate lastUpdatedDate = LocalDate.parse(br.lines().collect(Collectors.toList()).get(0));
                showNotice = today.minusDays(1).isAfter(lastUpdatedDate);
            }
        }
        if (showNotice) {
            PrintWriter writer = new PrintWriter(file.getPath(), "UTF-8");
            writer.println(today.toString());
            writer.close();
        }
        return showNotice;
    }

    /**
     * Delete BIR cache directory.
     *
     * @param outStream output stream to indicate errors
     * @throws IOException could occur accessing the file
     */
    public static void clearBirCacheLocation(PrintStream outStream) throws IOException {
        deleteDirectory(new File(getUserHome() + File.separator
                + BALLERINA_HOME_DIR + File.separator + BIR_CACHE), outStream);
    }

    /**
     * Delete jar cache directory.
     *
     * @param outStream output stream to indicate errors
     * @throws IOException could occur accessing the file
     */
    public static void clearJarCacheLocation(PrintStream outStream) throws IOException {
        deleteDirectory(new File(getUserHome() + File.separator
                + BALLERINA_HOME_DIR + File.separator + JAR_CACHE), outStream);
    }

    /**
     * Delete provided file.
     *
     * @param file      file needs to be deleted
     * @param outStream output stream to indicate errors
     * @throws IOException could occur accessing the file
     */
    public static void deleteDirectory(File file, PrintStream outStream) throws IOException {
        if (file.exists()) {
            Files.walk(file.toPath())
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            outStream.println(file.getPath() + " cannot remove");
                        }
                    });
        }
    }

    public static String getUserAgent(String ballerinaVersion, String toolVersion, String distributionType) {
        String os = "none";
        if (OSUtils.isWindows()) {
            os = "win-64";
        } else if (OSUtils.isUnix() || OSUtils.isSolaris()) {
            os = "linux-64";
        } else if (OSUtils.isMac()) {
            if (isArmArchitecture()) {
                os = "macos-arm-64";
            } else {
                os = "macos-64";
            }
        }
        return distributionType + "/" + ballerinaVersion + " (" + os + ") Updater/" + toolVersion;
    }

    static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    private static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    private static boolean isSolaris() {
        return OS.contains("sunos");
    }

    private static boolean  isArmArchitecture() {
        try {
            Process process = Runtime.getRuntime().exec("uname -m");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String architecture = reader.readLine();
            return architecture.contains("arm");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Provide user home directory based on command.
     *
     * @return user home directory
     */
    public static String getUserHome() {
        String userHome = System.getenv("HOME");
        if (isUnix() && userHome.contains("root")) {
            userHome = "/home/" + System.getenv("SUDO_USER");
        }
        if (userHome == null) {
            userHome = System.getProperty("user.home");
        }
        return userHome;
    }

    /**
     * Delete files inside directories.
     *
     * @param dirPath   directory path
     * @throws IOException throw an exception if an issue occurs
     */
    public static void deleteFiles(Path dirPath) throws IOException {
        if (dirPath == null) {
            return;
        }

        Files.walk(dirPath)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    if (!Files.isWritable(path)) {
                        throw ErrorUtil.createCommandException("permission denied: you do not have write access to '" +
                                dirPath + "'");
                    }
                });

        Files.walk(dirPath)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw ErrorUtil.createCommandException("cannot remove '" + path + "'");
                    }
                });
    }

    /**
     * Deletes the repository caches in user home.
     *
     * @param version distribution version
     */
    public static void deleteCaches(String version, PrintStream outStream) throws IOException {
        Path centralCache = Paths.get(getUserHome(),
                BALLERINA_HOME_DIR, REPOSITORIES, CENTRAL_CACHE_ROOT, CACHE + "-" + version);

        Path localCache = Paths.get(getUserHome(),
                BALLERINA_HOME_DIR, REPOSITORIES, LOCAL_CACHE_ROOT, CACHE + "-" + version);
        deleteDirectory(centralCache.toFile(), outStream);
        deleteDirectory(localCache.toFile(), outStream);
    }
}
