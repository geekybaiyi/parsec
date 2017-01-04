package com.yahoo.ec.parsec.tools.mojo.generators

import com.yahoo.parsec.tools.mojo.generators.ParsecApplicationGenerator
import com.yahoo.parsec.tools.mojo.generators.ParsecFileGenerator
import com.yahoo.parsec.tools.mojo.generators.ParsecGeneratorUtil
import com.yahoo.parsec.tools.mojo.generators.ParsecPackageResolver
import com.yahoo.parsec.tools.mojo.generators.ParsecPackageStruct
import com.yahoo.parsec.tools.mojo.generators.ParsecValidationGroupGenerator
import com.yahoo.parsec.tools.mojo.utils.FileUtils
import org.apache.maven.project.MavenProject
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

/**
 * ParsecFilesGenerator Unit Test
 */
class ParsecFileGeneratorTest extends Specification {

    Path generatedSourceRootPath
    MavenProject project
    ParsecPackageResolver packageResolver
    ParsecGeneratorUtil generatorUtil
    FileUtils fileUtils
    ParsecApplicationGenerator applicationGenerator
    ParsecValidationGroupGenerator validationGroupGenerator

    ParsecPackageStruct packageStruct
    def javaSourceRoot, generatedNamespace, packageName, parsecGeneratedRoot, projectPackageRoot

    def setup() {
        generatorUtil = Mock()
        fileUtils = Spy(FileUtils)
        project = Mock()
        packageResolver = Mock(constructorArgs: [generatorUtil, fileUtils])
        generatedSourceRootPath = Paths.get("./src/test/resources/generated-sources")
        generatedNamespace = ParsecFileGenerator.PARSEC_GENERATED_NAMESPACE
        javaSourceRoot = ParsecFileGenerator.JAVA_SOURCE_ROOT
        packageName = "java.com.example"
        applicationGenerator = Mock(constructorArgs: [packageStruct, generatorUtil])
        validationGroupGenerator = Mock(constructorArgs: [packageStruct, generatorUtil, fileUtils])

        parsecGeneratedRoot = generatedSourceRootPath.toString() + "/java/com/example/parsec_generated"
        projectPackageRoot = "/tmpTestDir/src/main/java/java/com/example"

        // mocking
        // TODO: this's just a convenient way to build package struct object, should build it directly
        generatorUtil.getIntersectPackageName(_) >> packageName
        packageStruct = new ParsecPackageResolver(generatorUtil, fileUtils).resolve(
                project, javaSourceRoot,
                generatedSourceRootPath, generatedNamespace)

        project.getBasedir() >> {
            new File("/tmpTestDir")
        }

        packageResolver.resolve(project, javaSourceRoot, generatedSourceRootPath, generatedNamespace) >> packageStruct
    }

    def getGenerator() {
        return new ParsecFileGenerator(
                generatedSourceRootPath, project, fileUtils, packageResolver, generatorUtil,
                applicationGenerator, validationGroupGenerator
        )
    }

    @Unroll
    def "generateFromTemplateToSourceRoot(#templateName) should generate from template with expected arguments"() {
        when:
        getGenerator().generateFromTemplateToSourceRoot(templateName)

        then:
        1 * generatorUtil.generateFromTemplateTo(
                templateName, packageName, projectPackageRoot, false)

        where:
        templateName << getTemplateNames()
    }

    @Unroll
    def "generateFromTemplateToIntersectSourceRoot(#templateName) should generate from template with expected arguments"() {
        when:
        getGenerator().generateFromTemplateToIntersectSourceRoot(templateName)

        then:
        1 * generatorUtil.generateFromTemplateTo(
                templateName, packageName, projectPackageRoot, false)

        where:
        templateName << getTemplateNames()
    }

    @Unroll
    def "generateFromTemplateToIntersectGeneratedNamespace(#templateName) should generate from template with expected arguments"() {
        when:
        getGenerator().generateFromTemplateToIntersectGeneratedNamespace(templateName)

        then:
        1 * generatorUtil.generateFromTemplateTo(
                templateName, packageName, parsecGeneratedRoot, true)

        where:
        templateName << getTemplateNames()
    }

    def getTemplateNames() {
        ["UnitTestTemplate.java", "DummyTemplate.java"]
    }

    @Unroll
    def "generateParsecApplication(#handleUncaughtException) should be generated by application generator"() {
        when:
        getGenerator().generateParsecApplication(handleUncaughtException)

        then:
        1 * applicationGenerator.generateParsecApplication(handleUncaughtException)

        where:
        handleUncaughtException << [false, true]
    }

    def "generateParsecValidationGroups() should be generated by validation groups generator"() {
        when:
        getGenerator().generateParsecValidationGroups()

        then:
        1 * validationGroupGenerator.generateParsecValidationGroups()
    }
}