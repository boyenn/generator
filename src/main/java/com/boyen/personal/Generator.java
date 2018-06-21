package com.boyen.personal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Generator {

  public static void main(String[] args) throws IOException {
    String baseName = args[0];
    String srcDirectory = args[1];
    generateFiles(baseName, srcDirectory);
  }

  public static void generateFiles(String baseName, String srcDirectory) throws IOException {
    generateFiles(baseName, srcDirectory, getDefaultMysiteConfiguration());
  }

  public static void generateFiles(String baseName, String srcDirectory, GeneratorConfiguration configuration) throws IOException {
    List<ClassName> additionalRepositorySuperInterfaces = Arrays.stream(configuration.getAdditionalRepositorySuperInterfaces())
        .map(ClassName::bestGuess)
        .collect(Collectors.toList());

    JavaFile model = ClassGenerator.generateValueClass(baseName + "Model", configuration.getRestModelsPackage());
    JavaFile dto = ClassGenerator.generateValueClass(baseName + "Dto", configuration.getDtoMappersPackage());
    JavaFile entity = ClassGenerator.generateEntityClass(baseName, configuration.getEntitiesPackage());

    MethodSpec dtoToModel = ClassGenerator.buildMappingMethod(dto, model, "toModel", "dto");
    MethodSpec modelToDto = ClassGenerator.buildMappingMethod(model, dto, "toDto", "model");
    MethodSpec entityToDto = ClassGenerator.buildMappingMethod(entity, dto, "toDto", "entity");

    JavaFile modelMapper =
        ClassGenerator
            .generateMapper(baseName + "ModelMapper", configuration.getRestMappersPackage(), Arrays.asList(dtoToModel, modelToDto));
    JavaFile entityMapper =
        ClassGenerator.generateMapper(baseName + "Mapper", configuration.getDtoMappersPackage(), Arrays.asList(entityToDto));
    JavaFile repository = ClassGenerator
        .generateRepository(baseName + "Repository", configuration.getRepositoriesPackage(), entity, additionalRepositorySuperInterfaces);

    Stream.of(model, dto, entity, modelMapper, entityMapper, repository)
        .forEach(javaFile -> {
          try {
            javaFile.writeTo(new File(srcDirectory));
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  private static GeneratorConfiguration getDefaultMysiteConfiguration() {
    GeneratorConfiguration generatorConfiguration = new GeneratorConfiguration();
    generatorConfiguration
        .setAdditionalRepositorySuperInterfaces(new String[] {"com.tvh.mysite.dbaccess.querydsl.AdvancedQueryDslPredicateExecutor"});
    generatorConfiguration.setDtoMappersPackage("com.tvh.mysite.business.dtos");
    generatorConfiguration.setRestModelsPackage("com.tvh.mysite.rest.models");
    generatorConfiguration.setEntitiesPackage("com.tvh.mysite.dbaccess.entities");
    generatorConfiguration.setRestMappersPackage("com.tvh.mysite.rest.mappers");
    generatorConfiguration.setDtoMappersPackage("com.tvh.mysite.business.mapper");
    generatorConfiguration.setRepositoriesPackage("com.tvh.mysite.dbaccess.repositories");
    return generatorConfiguration;
  }
}
