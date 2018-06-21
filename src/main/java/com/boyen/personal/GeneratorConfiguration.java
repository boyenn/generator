package com.boyen.personal;

import lombok.Data;

@Data
public class GeneratorConfiguration {
  private String[] additionalRepositorySuperInterfaces;
  private String restModelsPackage;
  private String dtosPackage;
  private String entitiesPackage;
  private String restMappersPackage;
  private String dtoMappersPackage;
  private String repositoriesPackage;

}
