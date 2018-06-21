package com.boyen.personal;

import java.io.IOException;
import java.util.Optional;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GeneratorGradlePlugin implements Plugin<Project> {
  @Override public void apply(Project project) {
    String srcDirectory = project.getProjectDir().getAbsolutePath() + "/src/main/java";
    String baseName = Optional
        .ofNullable(
            project.getProperties().get("generatorBaseName").toString()
        )
        .orElseThrow(IllegalArgumentException::new);
    project.task("generateFiles")
        .doLast(task -> {
          try {
            Generator.generateFiles(baseName, srcDirectory);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }
}
