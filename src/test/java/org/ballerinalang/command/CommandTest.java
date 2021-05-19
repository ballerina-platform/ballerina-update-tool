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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.*;
import java.nio.file.Files;

/**
 * Command tests super class.
 *
 * @since 2.0.0
 */
public abstract class CommandTest {
    protected ByteArrayOutputStream outContent;
    protected PrintStream testStream;

    @BeforeClass
    public void setup() {
        this.outContent = new ByteArrayOutputStream();
        this.testStream = new PrintStream(this.outContent);
    }

    @BeforeMethod
    public void beforeMethod() {
        this.outContent = new ByteArrayOutputStream();
        this.testStream = new PrintStream(this.outContent);
        System.setOut(testStream);
    }

    public void writeOutput(String str) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
            writer.write(str);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() throws IOException {
        outContent.close();
        testStream.close();
    }
}
