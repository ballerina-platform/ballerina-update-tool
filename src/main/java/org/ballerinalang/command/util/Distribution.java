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

/**
 * Consist of information.
 */
public class Distribution {
    private String name;
    private String version;
    private String type;
    private String channel;
    private String dependency;

    public Distribution() {
        this.name = "";
        this.version = "";
        this.type = "";
        this.channel = "";
        this.dependency = "";
    }

    public Distribution(String version) {
        this.name = "";
        this.version = version;
        this.type = "";
        this.channel = "";
        this.dependency = "";
    }

    public Distribution(String name, String version, String type, String channel, String dependency) {
        this.name = name;
        this.version = version;
        this.type = type;
        this.channel = channel;
        this.dependency = dependency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }
}
