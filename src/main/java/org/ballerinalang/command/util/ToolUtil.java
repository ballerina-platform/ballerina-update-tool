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

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.ballerinalang.command.Main;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Ballerina tool utilities.
 */
public class ToolUtil {
    private static final String PRODUCTION_URL = "https://api.central.ballerina.io/2.0/update-tool";
    private static final String STAGING_URL = "https://api.staging-central.ballerina.io/2.0/update-tool/";
    private static final String DEV_URL = "https://api.dev-central.ballerina.io/2.0/update-tool/";
    public static final String CLI_HELP_FILE_PREFIX = "dist-";
    private static final String BALLERINA_1_X_VERSIONS = "1.0.";
    private static final String CONNECTION_ERROR_MESSAGE = "connection to the remote server failed";
    public static final boolean BALLERINA_STAGING_UPDATE = Boolean.parseBoolean(
            System.getenv("BALLERINA_STAGING_UPDATE"));
    public static final boolean BALLERINA_DEV_UPDATE = Boolean.parseBoolean(
            System.getenv("BALLERINA_DEV_UPDATE"));
    public static final String LATEST_PULL_INPUT = "latest";
    public static final boolean TEST_MODE = Boolean.parseBoolean(
            System.getenv("TEST_MODE_ACTIVE"));

    /**
     * Provides used Ballerina version.
     *
     * @return Used Ballerina version
     */
    public static String getCurrentBallerinaVersion() {
        try {
            String installerVersionFilePath = OSUtils.getInstallerVersionFilePath();
            if (new File(installerVersionFilePath).exists()) {
                String installedInstallerVersion = getInstallerVersion(OSUtils.getInstalledInstallerVersionPath());
                if (!installedInstallerVersion.equals(getInstallerVersion(installerVersionFilePath))) {
                    setCurrentBallerinaVersion(ToolUtil.getCurrentInstalledBallerinaVersion());
                    setInstallerVersion(installerVersionFilePath);
                }
            }
            String userVersion = getVersion(OSUtils.getBallerinaVersionFilePath());
            if (checkDistributionAvailable(userVersion)) {
                return userVersion;
            }
            return getCurrentInstalledBallerinaVersion();
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("current Ballerina version not found: " + e.getMessage());
        }
    }

    /**
     * Provide the installed Ballerina version by the installer.
     *
     * @return Installed Ballerina version
     */
    public static String getCurrentInstalledBallerinaVersion() {
        try {
            return getVersion(OSUtils.getInstalledConfigPath());
        } catch (IOException e) {
            //If we files does not exist it will be empty and update tool continues without a distribution
            return "";
        }
    }

    private static void setCurrentBallerinaVersion(String version) {
        try {
            String ballerinaVersionFilePath = OSUtils.getBallerinaVersionFilePath();
            if (!Files.isWritable(Paths.get(ballerinaVersionFilePath))) {
                throw ErrorUtil.createCommandException("permission denied: you do not have write access to '" +
                        ballerinaVersionFilePath + "'");
            }
            setVersion(ballerinaVersionFilePath, version);
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("failed to set the Ballerina version: " + e.getMessage());
        }
    }

    private static void clearCache(PrintStream outStream) {
        try {
            OSUtils.clearBirCacheLocation(outStream);
            OSUtils.clearJarCacheLocation(outStream);
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("failed to clear the caches.");
        }
    }

    /**
     * Provides used Ballerina tools version.
     *
     * @return Used Ballerina tools version.
     */
    public static String getCurrentToolsVersion() {
        String version;
        try (InputStream inputStream = Main.class.getResourceAsStream("/META-INF/tool.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            version = properties.getProperty("command.version");
        } catch (Throwable ignore) {
            // Exception is ignored
            throw ErrorUtil.createCommandException("version info not available");
        }
        return  version;
    }

    private static String getVersion(String path) throws IOException {
        BufferedReader br = Files.newBufferedReader(Paths.get(path));
        String[] list = br.lines().collect(Collectors.toList()).get(0).split("-");
        String version = list.length == 2 ? list[1] : "";
        return version;
    }

    /**
     * Get the installation version from the file.
     *
     * @param path path to the installer version file
     * @return installer version
     * @throws IOException
     */
    private static String getInstallerVersion(String path) throws IOException {
        BufferedReader br = Files.newBufferedReader(Paths.get(path));
        List<String> list = br.lines().collect(Collectors.toList());
        return list.get(0);
    }

    static void setVersion(String path, String version) throws IOException {
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.println(ToolUtil.getType(version) + "-" + version);
        writer.close();
    }

    static void setInstallerVersion(String path) throws IOException {
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.println(ToolUtil.getInstallerVersion(OSUtils.getInstalledInstallerVersionPath()));
        writer.close();
    }

    public static void useBallerinaVersion(PrintStream printStream, String distribution) {
        setCurrentBallerinaVersion(distribution);
        clearCache(printStream);
    }

    public static boolean checkDistributionAvailable(String distribution) {
        File installFile = new File(getDistributionsPath() + File.separator
                + ToolUtil.getType(distribution) + "-" + distribution);
        return installFile.exists();
    }

    public static boolean checkDependencyAvailable(String dependency) {
        File dependencyLocation = new File(getDependencyPath() + File.separator + dependency);
        return dependencyLocation.exists();
    }

    public static List<Channel> getDistributions(PrintStream printStream) {
        HttpURLConnection conn = null;
        List<Channel> channels = new ArrayList<>();
        List<Distribution> distributions = new ArrayList<>();
        try {
            URL url = new URL(getServerURL() + "/distributions");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("user-agent",
                    OSUtils.getUserAgent(getCurrentBallerinaVersion(),
                            getCurrentToolsVersion(), "jballerina"));
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                conn.disconnect();
                throw ErrorUtil.createCommandException(getServerRequestFailedErrorMessage(conn));
            } else {
                String json = convertStreamToString(conn.getInputStream());
                Matcher matcher = Pattern.compile("\"version\":\"(.*?)\"").matcher(json);
                while (matcher.find()) {
                    distributions.add(new Distribution(matcher.group(1)));
                }

                matcher = Pattern.compile("\"type\":\"(.*?)\"").matcher(json);
                int i = 0;
                while (matcher.find()) {
                    distributions.get(i++).setType(matcher.group(1));
                }

                matcher = Pattern.compile("\"channel\":\"(.*?)\"").matcher(json);
                i = 0;
                while (matcher.find()) {
                    distributions.get(i++).setChannel(matcher.group(1));
                }

                matcher = Pattern.compile("\"name\":\"(.*?)\"").matcher(json);
                i = 0;
                int count = 0;
                while (matcher.find()) {
                    if (count++ % 2 == 0) {
                        distributions.get(i++).setName(matcher.group(1));
                    }
                }

                for (Distribution distribution : distributions) {
                    Channel channel = null;
                    try {
                        channel = channels.stream().filter(e -> e.getName().equals(distribution.getChannel()))
                                .findFirst().orElse(null);
                    } catch (Exception e) {
                        printStream.println(e.getMessage());
                    }

                    if (channel == null) {
                        channel = new Channel(distribution.getChannel());
                        channels.add(channel);
                    }
                    channel.getDistributions().add(distribution);
                }
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(CONNECTION_ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return channels;
    }

    public static String getLatest(String currentVersion, String type) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(getServerURL()
                    + "/distributions/latest?version=" + currentVersion + "&type=" + type);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("user-agent",
                    OSUtils.getUserAgent(getCurrentBallerinaVersion(),
                            getCurrentToolsVersion(), "jballerina"));
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() == 200) {
                return getValue(type, convertStreamToString(conn.getInputStream()));
            }
            if (conn.getResponseCode() == 404) {
                return null;
            }
            throw ErrorUtil.createCommandException(getServerRequestFailedErrorMessage(conn));
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(CONNECTION_ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String getValue(String key, String json) {
        Pattern pattern = Pattern.compile(String.format("\"%s\":\"(.*?)\"", key));
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String getLatestToolVersion() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(getServerURL() + "/versions/latest");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("user-agent", OSUtils.getUserAgent(getCurrentBallerinaVersion(),
                    getCurrentToolsVersion(), "jballerina"));
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() == 200) {
                String json = convertStreamToString(conn.getInputStream());
                Pattern p = Pattern.compile("\"version\":\"(.*?)\"");
                Matcher matcher = p.matcher(json);
                if (matcher.find()) {
                    conn.disconnect();
                    return matcher.group(1);
                } else {
                    return null;
                }
            }
            if (conn.getResponseCode() == 404) {
                return null;
            }
            throw ErrorUtil.createCommandException(getServerRequestFailedErrorMessage(conn));
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(CONNECTION_ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            //TODO : do nothing
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                //TODO : do nothing
            }
        }
        return sb.toString();
    }

    /**
     * Provides path of the installed distributions.
     *
     * @return installed distributions path
     */
    public static String getDistributionsPath() {
        try {
            File distDirectory = new File(OSUtils.getInstallationPath() + File.separator + "distributions");
            if(!distDirectory.exists()) {
                distDirectory.mkdirs();
            }
            return distDirectory.getPath();
        } catch (URISyntaxException e) {
            throw ErrorUtil.createCommandException("failed to get the path of the distributions");
        }
    }

    /**
     * Provides path of the dependencies.
     *
     * @return dependencies path
     */
    public static String getDependencyPath() {
        try {
            File depDirectory = new File(OSUtils.getInstallationPath() + File.separator + "dependencies");
            if(!depDirectory.exists()) {
                depDirectory.mkdirs();
            }
            return depDirectory.getPath();
        } catch (URISyntaxException e) {
            throw ErrorUtil.createCommandException("failed to get the path of the distributions");
        }
    }

    /**
     * Provides path of the tool unzip location.
     *
     * @return temporary directory to unzip update tool zip
     */
    private static String getToolUnzipLocation() {
        try {
            return OSUtils.getInstallationPath() + File.separator + "ballerina-command-tmp";
        } catch (URISyntaxException e) {
            throw ErrorUtil.createCommandException(
                    "failed to get a temporary directory to unzip the update tool zip to");
        }
    }

    /**
     * Checks for command avaiable for current version.
     *
     * @param printStream stream which messages should be printed
     */
    public static void checkForUpdate(PrintStream printStream) {
        try {
            String version = getCurrentBallerinaVersion();
            if (OSUtils.updateNotice()) {
                String latestVersion = ToolUtil.getLatest(version, "patch");
                // For 1.0.x releases we support through jballerina distribution
                if (latestVersion == null || latestVersion.startsWith(BALLERINA_1_X_VERSIONS)) {
                    return;
                }
                if (!latestVersion.equals(version)) {
                    printStream.println("A new version of Ballerina is available: " + latestVersion);
                    printStream.println("Use 'bal dist pull " + latestVersion + "' to " +
                            "download and use the distribution");
                    printStream.println();
                }
            }
        } catch (Throwable e) {
            // If any exception occurs we are not letting users know as check for command is optional
            // TODO Add debug log here.
        }
    }

    public static boolean downloadDistribution(PrintStream printStream, String distribution, String distributionType,
                                               String distributionVersion, boolean testMode) {
        HttpURLConnection conn = null;
        try {
            if (!ToolUtil.checkDistributionAvailable(distribution)) {
                printStream.println("Fetching the '" + distribution + "' distribution from the remote server...");
                URL url = new URL(ToolUtil.getServerURL() + "/distributions/" + distributionVersion);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("user-agent",
                        OSUtils.getUserAgent(getCurrentBallerinaVersion(), ToolUtil.getCurrentToolsVersion(),
                                distributionType));
                conn.setRequestProperty("Accept", "application/json");
                if (testMode || TEST_MODE) {
                    conn.setRequestProperty("testMode", "true");
                }
                if (conn.getResponseCode() == 302) {
                    String newUrl = conn.getHeaderField("Location");
                    conn = (HttpURLConnection) new URL(newUrl).openConnection();
                    conn.setRequestProperty("content-type", "binary/data");
                    ToolUtil.downloadAndSetupDist(printStream, conn, distribution);
                    ToolUtil.getDependency(printStream, distribution, distributionType, distributionVersion);
                    return false;
                } else if (conn.getResponseCode() == 200) {
                    ToolUtil.downloadAndSetupDist(printStream, conn, distribution);
                    ToolUtil.getDependency(printStream, distribution, distributionType, distributionVersion);
                    return false;
                } else {
                    throw ErrorUtil.createDistributionNotFoundException(distribution);
                }
            } else {
                printStream.println("'" + distribution + "' is already available locally");
                return true;
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(CONNECTION_ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void downloadAndSetupDist(PrintStream printStream, HttpURLConnection conn,
                                             String distribution) {
        try {
            String distPath = getDistributionsPath();
            String zipFileLocation = getDistributionsPath() + File.separator + distribution + ".zip";
            downloadFile(conn, zipFileLocation, distribution, printStream);
            unzip(zipFileLocation, distPath);
            addExecutablePermissionToFile(new File(distPath + File.separator + ToolUtil.getType(distribution)
                    + "-" + distribution + File.separator + "bin"
                    + File.separator + OSUtils.getExecutableFileName(distribution)));


            String langServerPath = distPath + File.separator + distribution + File.separator + "lib"
                    + File.separator + "tools";
            File launcherServer = new File(langServerPath + File.separator + "lang-server"
                    + File.separator + "launcher" + File.separator + OSUtils.getLangServerLauncherName());
            File debugAdpater = new File(langServerPath + File.separator + "debug-adapter"
                    + File.separator + "launcher" + File.separator + OSUtils.getDebugAdapterName());

            if (debugAdpater.exists()) {
                addExecutablePermissionToFile(debugAdpater);
            }

            if (launcherServer.exists()) {
                addExecutablePermissionToFile(launcherServer);
            }

            new File(zipFileLocation).delete();
        } finally {
            conn.disconnect();
        }
    }

    public static void getDependency(PrintStream printStream, String distribution, String distributionType,
                                     String distributionVersion) {
        HttpURLConnection conn = null;
        try {
            printStream.println("\nFetching the dependencies for '" + distribution + "' from the remote server...");
            URL url = new URL(ToolUtil.getServerURL() + "/distributions");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("user-agent",
                    OSUtils.getUserAgent(distributionVersion, ToolUtil.getCurrentToolsVersion(),
                            distributionType));
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() == 200) {
                String response = convertStreamToString(conn.getInputStream());
                String infoRegex = "\\{.*?\\]\\}";
                Pattern infoPattern = Pattern.compile(infoRegex);
                Matcher infoMatcher = infoPattern.matcher(response);
                while (infoMatcher.find()) {
                    String distInfo = infoMatcher.group();
                    if (distInfo.contains(distributionVersion)) {
                        String dependencyRegex = "\"(jdk.*?)\"";
                        Pattern dependencyPattern = Pattern.compile(dependencyRegex);
                        Matcher dependencyMatcher = dependencyPattern.matcher(distInfo);
                        while (dependencyMatcher.find()) {
                            String dependencyName = dependencyMatcher.group(1);
                            if (!ToolUtil.checkDependencyAvailable(dependencyName)) {
                                downloadDependency(printStream, conn, dependencyName, distributionType,
                                        distributionVersion);
                            } else {
                                printStream.println("Dependency '" + dependencyName +
                                        "' is already available locally");
                            }
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(CONNECTION_ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void downloadDependency(PrintStream printStream, HttpURLConnection conn,
                                           String dependency, String distributionType,
                                           String distributionVersion) {
        try {
            String url = ToolUtil.getServerURL() + "/dependencies/" + dependency;
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("user-agent",
                    OSUtils.getUserAgent(distributionVersion, ToolUtil.getCurrentToolsVersion(),
                            distributionType));
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() == 302) {
                String newUrl = conn.getHeaderField("Location");
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                conn.setRequestProperty("content-type", "binary/data");
                downloadAndSetupDependency(conn, printStream, dependency);
            } else if (conn.getResponseCode() == 200) {
                downloadAndSetupDependency(conn, printStream, dependency);
            } else {
                throw ErrorUtil.createDependencyNotFoundException(dependency);
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(CONNECTION_ERROR_MESSAGE);
        }
    }

    private static void downloadAndSetupDependency(HttpURLConnection conn, PrintStream printStream, String dependency) {
        File zipFile = null;
        String dependencyLocation = getDependencyPath();
        String zipFileLocation = dependencyLocation + File.separator + dependency + ".zip";
        zipFile = Paths.get(zipFileLocation).toFile();
        downloadFile(conn, zipFileLocation, dependency, printStream);
        unzip(zipFileLocation, dependencyLocation);
        if(OSUtils.isMac()) {
            addExecutablePermissionToDirectory(dependencyLocation + File.separator + dependency);
        } else {
            addExecutablePermissionToFile(new File(dependencyLocation + File.separator + dependency
                    + File.separator + "bin" + File.separator + "java"));
        }
        if (zipFile.exists()) {
            zipFile.delete();
        }
    }

    public static void downloadTool(PrintStream printStream, String toolVersion) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(ToolUtil.getServerURL() + "/versions/" + toolVersion);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("user-agent", OSUtils.getUserAgent(getCurrentBallerinaVersion(),
                    getCurrentToolsVersion(), "jballerina"));
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() == 302) {
                String newUrl = conn.getHeaderField("Location");
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                conn.setRequestProperty("content-type", "binary/data");
                downloadAndSetupTool(printStream, conn, "ballerina-command-" + toolVersion);
            } else if (conn.getResponseCode() == 200) {
                downloadAndSetupTool(printStream, conn, "ballerina-command-" + toolVersion);
            } else {
                throw ErrorUtil.createCommandException("tool version '" + toolVersion + "' not found ");
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(CONNECTION_ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }

    private static void downloadAndSetupTool(PrintStream printStream, HttpURLConnection conn,
                                             String toolFileName) {
        File tempUnzipDirectory = null;
        File zipFile = null;
        try {
            String toolUnzipLocation = getToolUnzipLocation();
            tempUnzipDirectory = Paths.get(toolUnzipLocation).toFile();
            if (tempUnzipDirectory.exists()) {
                tempUnzipDirectory.delete();
            }
            tempUnzipDirectory.mkdir();
            String zipFileLocation = toolUnzipLocation + File.separator + toolFileName + ".zip";
            zipFile = Paths.get(zipFileLocation).toFile();
            downloadFile(conn, zipFileLocation, toolFileName, printStream);
            unzip(zipFileLocation, toolUnzipLocation);
            copyScripts(toolUnzipLocation, toolFileName);
        } finally {
            if (tempUnzipDirectory != null && tempUnzipDirectory.exists()) {
                tempUnzipDirectory.delete();
            }
            if (zipFile != null && zipFile.exists()) {
                zipFile.delete();
            }
        }
    }

    private static void downloadFile(HttpURLConnection conn, String zipFileLocation,
                                     String fileName, PrintStream printStream) {
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(zipFileLocation)) {
            byte[] b = new byte[1024];
            int count;
            int progress = 0;
            long totalSizeInMB = conn.getContentLengthLong() / (1024 * 1024);

            try (ProgressBar progressBar = new ProgressBar("Downloading " + fileName, totalSizeInMB,
                    1000, printStream, ProgressBarStyle.ASCII, " MB", 1)) {
                while ((count = in.read(b)) > 0) {
                    out.write(b, 0, count);
                    progress++;
                    if (progress % 1024 == 0) {
                        progressBar.step();
                    }
                }
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("failed to download file " + fileName + " to " +
                    zipFileLocation + ".");
        }
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
            throw ErrorUtil.createCommandException("failed to unzip zip the file in '" + zipFilePath + "' to '" +
                    destDirectory + "'");
        }
    }

    private static void copyScripts(String unzippedUpdateToolPath, String ballerinaCommandDir) {
        String installScriptFileName = OSUtils.getInstallScriptFileName();
        Path installScript = Paths.get(unzippedUpdateToolPath, installScriptFileName);
        try {
            Files.copy(Paths.get(unzippedUpdateToolPath, ballerinaCommandDir, "scripts", installScriptFileName),
                    installScript);
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("failed to copy the update scripts to temporary directory '" +
                    unzippedUpdateToolPath + "'");
        }
        addExecutablePermissionToFile(installScript.toFile());
    }

    /**
     * Add read, write, and executable permission to the given file.
     *
     * @param file file path
     */
    public static void addExecutablePermissionToFile(File file) {
        file.setReadable(true, false);
        file.setExecutable(true, false);
        file.setWritable(true, false);
    }

    /**
     * Add read and write permission to the given file.
     *
     * @param file file path
     */
    public static void addWritePermissionToFile(File file) {
        file.setReadable(true, false);
        file.setWritable(true, false);
    }

    public static String readFileAsString(String path) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream(path);
        if (is == null) {
            throw new IOException("path cannot be found in " + path);
        }
        InputStreamReader inputStreamREader = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            inputStreamREader = new InputStreamReader(is, StandardCharsets.UTF_8);
            br = new BufferedReader(inputStreamREader);
            String content = br.readLine();
            if (content == null) {
                return sb.toString();
            }

            sb.append(content);

            while ((content = br.readLine()) != null) {
                sb.append('\n').append(content);
            }
        } finally {
            if (inputStreamREader != null) {
                try {
                    inputStreamREader.close();
                } catch (IOException ignore) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {
                }
            }
        }
        return sb.toString();
    }

    /**
     * Handle user permission to ballerina install location.
     */
    public static void handleInstallDirPermission() {
        try {
            String installationPath = OSUtils.getInstallationPath();
            boolean isWritable = Files.isWritable(Paths.get(installationPath));
            if (!isWritable) {
                throw ErrorUtil.createCommandException("permission denied: you do not have write access to '" +
                        installationPath + "'");
            }
        } catch (URISyntaxException e) {
            throw ErrorUtil.createCommandException("failed to get the path to the Ballerina installation directory");
        }
    }

    private static String getServerRequestFailedErrorMessage(HttpURLConnection conn) throws IOException {
        String responseMessage = conn.getResponseMessage();
        return "server request failed: " + (responseMessage == null ? conn.getResponseCode() : responseMessage);
    }

    /**
     * Provide server url based on the environment variable setting.
     *
     * @return server url
     */
    private static String getServerURL() {
        String url = PRODUCTION_URL;
        url = BALLERINA_STAGING_UPDATE ? STAGING_URL : url;
        url = BALLERINA_DEV_UPDATE ? DEV_URL : url;
        return url;
    }

    /**
     * Get distribution type
     *
     * @param version distribution version
     */
    public static String getType(String version) {
        return version.contains("1.") ? "jballerina" : "ballerina";
    }

    /**
     * Modify the output based on distribution type
     *
     * @param version distribution version
     */
    public static String getTypeName(String version) {
        char lastChar = version.charAt(version.length() - 1);
        if (version.contains("1.")) {
            return "jballerina" + " version " + version;
        } else if(version.contains("slp")) {
            return " Preview " + lastChar;
        } else {
            String versionId = version.substring(2, version.length() - 1) + " " + lastChar;
            return versionId.substring(0, 1).toUpperCase() + versionId.substring(1);
        }
    }

    private static void addExecutablePermissionToDirectory(String filePath) {
        Process process;
        try {
            process = Runtime.getRuntime().exec("chmod -R 755 " + filePath);
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            throw ErrorUtil.createCommandException("permission denied: you do not have write access to '" + filePath
                    + "'");
        }
    }

    /**
     * Update the update tool if any latest version available.
     *
     * @param printStream stream which messages should be printed
     */
    public static void updateTool(PrintStream printStream) {
        String version = ToolUtil.getCurrentToolsVersion();
        printStream.println("Checking for newer versions of the update tool...");
        String latestVersion = ToolUtil.getLatestToolVersion();
        if (latestVersion == null) {
            printStream.println("Failed to find the latest update tool version");
        } else if (!latestVersion.equals(version)) {
            ToolUtil.downloadTool(printStream, latestVersion);
            try {
                executeFile(printStream);
                printStream.println("Update successfully completed");
            } catch (IOException | InterruptedException e) {
                printStream.println("Update failed due to errors");
            } finally {
                try {
                    OSUtils.deleteFiles(Paths.get(getToolUnzipLocation()));
                } catch (IOException e) {
                    printStream.println("Error occurred while removing files");
                }
            }
            printStream.println();
        }
    }

    /**
     * Execute the file.
     *
     * @param printStream stream which messages should be printed
     */
    private static void executeFile(PrintStream printStream) throws IOException, InterruptedException {
        Path filePath = Paths.get(getToolUnzipLocation(), OSUtils.getInstallScriptFileName());
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(filePath.toString());
        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            printStream.println(output);
        }
    }
}
