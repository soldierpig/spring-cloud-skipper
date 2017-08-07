/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.skipper.index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.skipper.AbstractIntegrationTest;
import org.springframework.cloud.skipper.config.SkipperServerProperties;
import org.springframework.cloud.skipper.repository.PackageMetadataRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 */
public class PackageIndexLoaderTests extends AbstractIntegrationTest {

	@Autowired
	private PackageIndexDownloader packageIndexLoader;

	@Autowired
	private SkipperServerProperties skipperServerProperties;

	@Autowired
	private PackageMetadataRepository packageMetadataRepository;

	@Before
	public void cleanPackageIndexDir() throws IOException {
		packageMetadataRepository.deleteAll();

		List<File> files;
		Path indexPath = Paths.get(skipperServerProperties.getPackageIndexDir());
		if (Files.exists(indexPath)) {
			try (Stream<Path> paths = Files.walk(indexPath, 1)) {
				files = paths.map(i -> i.toAbsolutePath().toFile()).collect(Collectors.toList());
			}
			for (File file : files) {
				if (file.getName().startsWith("file") || file.getName().startsWith("localhost")) {
					file.delete();
				}
			}
		}
	}

	@Test
	public void testDownloadPackageIndex() throws IOException {
		packageIndexLoader.downloadPackageIndexes();
		List<File> files;
		Path indexPath = Paths.get(skipperServerProperties.getPackageIndexDir());
		try (Stream<Path> paths = Files.walk(indexPath, 1)) {
			files = paths
					.filter(Files::isRegularFile)
					.map(i -> i.toAbsolutePath().toFile()).collect(Collectors.toList());
		}
		assertThat(files.size()).isEqualTo(1);
		assertThat(files.get(0).getName()).endsWith("target_test-classes_index.yml");
	}
}
