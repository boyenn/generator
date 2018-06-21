package com.boyen.personal;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;

public class ClassGenerator {
  public static JavaFile generateValueClass(String name, String packageName) throws IOException {
    TypeSpec typeSpec = TypeSpec.classBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(Data.class)
        .addAnnotation(Builder.class)
        .addAnnotation(NoArgsConstructor.class)
        .addAnnotation(AllArgsConstructor.class)
        .build();

    JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
        .build();

    javaFile.writeTo(System.out);
    return javaFile;
  }

  public static JavaFile generateEntityClass(String name, String packageName) throws IOException {
    FieldSpec idField = FieldSpec
        .builder(ClassName.get(Long.class), "id", Modifier.PRIVATE)
        .addAnnotation(Id.class)
        .addAnnotation(
            AnnotationSpec
                .builder(GeneratedValue.class)
                .addMember("strategy",
                    CodeBlock.builder().add("$T.IDENTITY", ClassName.bestGuess("javax.persistence.GenerationType")).build())
                .build()
        )
        .build();

    TypeSpec typeSpec = TypeSpec.classBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(Data.class)
        .addAnnotation(Builder.class)
        .addAnnotation(NoArgsConstructor.class)
        .addAnnotation(AllArgsConstructor.class)
        .addAnnotation(Entity.class)
        .addAnnotation(Table.class)
        .addField(idField)
        .build();

    JavaFile javaFile = JavaFile
        .builder(packageName, typeSpec)
        .skipJavaLangImports(true)
        .build();
    javaFile.writeTo(System.out);
    return javaFile;
  }

  public static MethodSpec buildMappingMethod(JavaFile classFrom, JavaFile classTo, String methodName, String paramName) {
    return MethodSpec
        .methodBuilder(methodName)
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(getClassName(classTo))
        .addParameter(
            ParameterSpec.builder(getClassName(classFrom), paramName).build()
        ).build();
  }

  private static ClassName getClassName(JavaFile javaFile) {
    return ClassName.get(javaFile.packageName, javaFile.typeSpec.name);
  }

  public static JavaFile generateMapper(String name, String packageName, List<MethodSpec> methodSpecs)
      throws IOException {

    TypeSpec typeSpec = TypeSpec.interfaceBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addMethods(methodSpecs)
        .addAnnotation(Mapper.class)
        .build();

    JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
        .skipJavaLangImports(true)
        .build();
    javaFile.writeTo(System.out);
    return javaFile;
  }

  public static JavaFile generateRepository(String name, String packageName, JavaFile entityClass,
      List<ClassName> additionalSuperInterfaces) throws IOException {
    ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
        ClassName.bestGuess("org.springframework.data.jpa.repository.JpaRepository"),
        getClassName(entityClass),
        ClassName.get(Long.class)
    );

    TypeSpec typeSpec = TypeSpec.interfaceBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(ClassName.bestGuess("org.springframework.stereotype.Repository"))
        .addSuperinterface(parameterizedTypeName)
        .addSuperinterfaces(additionalSuperInterfaces)
        .build();

    JavaFile javaFile = JavaFile
        .builder(packageName, typeSpec)
        .skipJavaLangImports(true)
        .build();

    javaFile.writeTo(System.out);
    return javaFile;
  }
}
