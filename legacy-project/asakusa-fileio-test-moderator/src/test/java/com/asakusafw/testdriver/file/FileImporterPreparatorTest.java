/**
 * Copyright 2011-2021 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.testdriver.file;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.configuration.HadoopEnvironmentChecker;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.vocabulary.external.FileImporterDescription;

/**
 * Test for {@link FileImporterPreparator}.
 */
public class FileImporterPreparatorTest {

    /**
     * Support for Windows platform.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    private static final TestContext EMPTY = new TestContext.Empty();

    /**
     * This test class requires Hadoop is installed.
     */
    @Rule
    public HadoopEnvironmentChecker check = new HadoopEnvironmentChecker(false);

    private ConfigurationFactory factory;

    private FileSystem fileSystem;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        factory = ConfigurationFactory.getDefault();
        Configuration conf = factory.newInstance();
        fileSystem = FileSystem.get(conf);
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        if (fileSystem != null) {
            fileSystem.delete(new Path("target/testing"), true);
        }
    }

    /**
     * minimum.
     * @throws Exception if test was failed
     */
    @Test
    public void simple() throws Exception {
        FileImporterPreparator target = new FileImporterPreparator(factory);
        try (ModelOutput<Text> open = target.createOutput(
                new MockTextDefinition(),
                new MockFileImporter(Text.class, TextInputFormat.class, "target/testing/input"),
                EMPTY)) {
            open.write(new Text("Hello, world!"));
        }
        try (Scanner scanner = new Scanner(loadResult("target/testing/input"), "UTF-8")) {
            assertThat(scanner.hasNextLine(), is(true));
            assertThat(scanner.nextLine(), is("Hello, world!"));
            assertThat(scanner.hasNextLine(), is(false));
        }
    }

    /**
     * sequence file.
     * @throws Exception if test was failed
     */
    @Test
    public void sequenceFile() throws Exception {
        FileImporterPreparator target = new FileImporterPreparator(factory);
        try (ModelOutput<Text> open = target.createOutput(
                new MockTextDefinition(),
                new MockFileImporter(Text.class, SequenceFileInputFormat.class, "target/testing/input"),
                EMPTY)) {
            open.write(new Text("Hello, world!"));
            open.write(new Text("This is a test."));
        }
        try (SequenceFile.Reader reader = new SequenceFile.Reader(
                factory.newInstance(),
                SequenceFile.Reader.file(fileSystem.makeQualified(new Path("target/testing/input"))))) {
            Text text = new Text();
            assertThat(reader.next(NullWritable.get(), text), is(true));
            assertThat(text.toString(), is("Hello, world!"));
            assertThat(reader.next(NullWritable.get(), text), is(true));
            assertThat(text.toString(), is("This is a test."));
            assertThat(reader.next(NullWritable.get(), text), is(false));
        }
    }

    private InputStream loadResult(String path) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (FSDataInputStream input = fileSystem.open(new Path(path))) {
            byte[] bytes = new byte[1024];
            while (true) {
                int read = input.read(bytes);
                if (read < 0) {
                    break;
                }
                buffer.write(bytes, 0, read);
            }
        }
        fileSystem.delete(new Path(path), true);
        return new ByteArrayInputStream(buffer.toByteArray());
    }

    @SuppressWarnings("rawtypes")
    private static class MockFileImporter extends FileImporterDescription {

        private final Class<?> modelType;

        private final Class<? extends FileInputFormat> format;

        private final Set<String> paths;

        MockFileImporter(Class<?> modelType, Class<? extends FileInputFormat> format, String... paths) {
            this.modelType = modelType;
            this.format = format;
            this.paths = new HashSet<>(Arrays.asList(paths));
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public Class<? extends FileInputFormat> getInputFormat() {
            return format;
        }

        @Override
        public Set<String> getPaths() {
            return paths;
        }
    }
}
