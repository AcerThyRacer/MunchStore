import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

apply(plugin = "jacoco")

configure<JacocoPluginExtension> {
    toolVersion = "0.8.11"
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val exclusions = listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/*\$Test*.*",
        "android/**/*.*",
        "**/Lambda\$*.class",
        "**/Lambda.class",
        "**/*Lambda.class",
        "**/*Lambda*.class",
        "**/*_MembersInjector.class",
        "**/Dagger*.*",
        "**/*Dagger*.*",
        "**/*_Factory.class",
        "**/*_Provide*Factory.class",
        "**/*_ViewBinding*.*",
        "**/BR.class",
        "**/DataBinderMapperImpl.class",
        "**/DataBinderMapperImpl\$*.class",
        "**/DataBindingInfo.class",
        "**/*\$Creator.class",
        "**/*\$DefaultImpls.class",
        "**/*\$Companion.class"
    )

    val mainSrc = "${project.projectDir}/src/main/kotlin"
    sourceDirectories.setFrom(files(mainSrc))

    val buildDir = project.file("build")
    val javaClasses = fileTree("$buildDir/intermediates/javac/debug/classes") {
        exclude(exclusions)
    }
    val kotlinClasses = fileTree("$buildDir/tmp/kotlin-classes/debug") {
        exclude(exclusions)
    }
    classDirectories.setFrom(files(javaClasses, kotlinClasses))

    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "jacoco/testDebugUnitTest.exec",
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
            )
        }
    )
    // Target: 80% minimum coverage. Run with -PcoverageThreshold=0.8 to enforce (custom task).
}
