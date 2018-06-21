import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class Generator {
  public static void main(String[] args) throws IOException {
    String baseName = args[0];
    JavaFile model = ClassGenerator.generateValueClass(baseName + "Model", "com.tvh.mysite.rest.models");
    JavaFile dto = ClassGenerator.generateValueClass(baseName + "Dto", "com.tvh.mysite.business.dtos");
    JavaFile entity = ClassGenerator.generateEntityClass(baseName, "com.tvh.mysite.dbaccess.entities");

    MethodSpec dtoToModel = ClassGenerator.buildMappingMethod(dto, model, "toModel", "dto");
    MethodSpec modelToDto = ClassGenerator.buildMappingMethod(model, dto, "toDto", "model");
    MethodSpec entityToDto = ClassGenerator.buildMappingMethod(entity, dto, "toDto", "entity");

    JavaFile modelMapper =
        ClassGenerator.generateMapper(baseName + "ModelMapper", "com.tvh.mysite.rest.mappers", Arrays.asList(dtoToModel, modelToDto));
    JavaFile entityMapper =
        ClassGenerator.generateMapper(baseName + "Mapper", "com.tvh.mysite.business.mapper", Arrays.asList(entityToDto));
    JavaFile repository =
        ClassGenerator.generateRepository(baseName + "Repository", "com.tvh.mysite.dbaccess.repositories", entity);

    Stream.of(model, dto, entity, modelMapper, entityMapper,repository)
        .forEach(javaFile -> {
          try {
            javaFile.writeTo(new File("/home/boyen/Documents/Fenego/Mateco/ms-mysite/src/main/java"));
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }


}
