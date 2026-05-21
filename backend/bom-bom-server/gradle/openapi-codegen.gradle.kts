import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

val openApiSpecFile = layout.projectDirectory.file("openapi-spec/openapi.yaml")
val generatedOpenApiDir = layout.buildDirectory.dir("generated/openapi")
val openApiTemplateDir = "$projectDir/src/main/resources/openapi-templates"
val openApiApiPackage = "me.bombom.openapi.api"
val openApiModelPackage = "me.bombom.openapi.model"
val openApiConfigOptions = mapOf(
    "interfaceOnly" to "true",
    "useTags" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useResponseEntity" to "false",
    "useSpringController" to "false",
    "openApiNullable" to "false",
    "skipDefaultInterface" to "true",
    "dateLibrary" to "java8",
    "documentationProvider" to "springdoc",
    "annotationLibrary" to "swagger2",
)
val openApiGlobalProperties = mapOf(
    "apis" to "",
    "models" to "",
    "supportingFiles" to "false",
    "apiDocs" to "false",
    "modelDocs" to "false",
    "apiTests" to "false",
    "modelTests" to "false",
)

val verifyOpenApiSpec by tasks.registering {
    doLast {
        if (!openApiSpecFile.asFile.exists()) {
            throw GradleException(
                "openapi-spec/openapi.yaml not found. Initialize or update the spec submodule first " +
                    "(for example: git submodule update --init --recursive backend/bom-bom-server/openapi-spec)."
            )
        }
    }
}

tasks.named<GenerateTask>("openApiGenerate") {
    dependsOn(verifyOpenApiSpec)
    generatorName.set("spring")
    inputSpec.set(openApiSpecFile.asFile.absolutePath)
    outputDir.set(generatedOpenApiDir.map { it.asFile.absolutePath })
    apiPackage.set(openApiApiPackage)
    modelPackage.set(openApiModelPackage)
    templateDir.set(openApiTemplateDir)
    configOptions.set(openApiConfigOptions)
    globalProperties.set(openApiGlobalProperties)
}

sourceSets {
    main {
        java {
            srcDir(generatedOpenApiDir.map { it.dir("src/main/java") })
        }
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}
