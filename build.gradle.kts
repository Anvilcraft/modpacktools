import groovy.lang.GroovyObject
import org.gradle.api.JavaVersion.VERSION_1_8
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinTest
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.JSON
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN

//settings for manifest and stuff
val specTitle = "ModPackTools"
val artifact = specTitle.toLowerCase()
val jarName = specTitle
val implTitle = "ley.anvil.modpacktools"
val jarVersion = "1.2-SNAPSHOT"
group = "ley.anvil"

application.mainClassName = "ley.anvil.modpacktools.Main"

plugins {
    id("java")
    id("application")
    id("idea")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "1.4.0"
    id("org.jetbrains.dokka") version "1.4.0-rc"
    id("org.jlleitschuh.gradle.ktlint") version "9.3.0"
}

configure<JavaPluginConvention> {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
}

repositories {
    mavenCentral()
    jcenter() //Used for dokka jdoc
    maven("https://data.tilera.xyz/maven/") //Used for Addonscript
}

dependencies {
    //Implementation
    arrayOf(
        //Anvilcraft
        "ley.anvil:addonscript:1.0-SNAPSHOT",

        //Kotlin
        "org.jetbrains.kotlin:kotlin-reflect:1.4.0",
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.0",

        //IO
        "com.squareup.okhttp3:okhttp:4.8.1", //HTTP client
        "com.moandjiezana.toml:toml4j:0.7.2", //Config file parser
        "org.apache.commons:commons-csv:1.8",
        "com.j2html:j2html:1.4.0", //HTML builder
        "commons-io:commons-io:2.7",
        "com.google.code.gson:gson:2.8.6",

        //CLI
        "net.sourceforge.argparse4j:argparse4j:0.8.1", //CLI argument parser
        "com.github.ajalt:mordant:1.2.1", //CLI text formatting
        "com.jakewharton.fliptables:fliptables:1.1.0", //Text Table

        //Other
        "org.slf4j:slf4j-simple:2.0.0-alpha1",
        "eu.infomas:annotation-detector:3.0.5"
    ).forEach {implementation(it)}

    testImplementation("junit:junit:4.12")
}

//This exists so options can be set for test compile and compile at once
val kOptions = {obj: GroovyObject ->

//This is sorta garbage, but if i would do this the proper way Intelli'ntJ would think its an error even tho it is not
    obj.withGroovyBuilder {
        "kotlinOptions" {
            setProperty("jvmTarget", "1.8")
        }
    }
}

tasks.withType<KotlinCompile> {kOptions(this as GroovyObject)}
tasks.withType<KotlinTest> {kOptions(this as GroovyObject)}

@Suppress("DEPRECATION") //"version" is deprecated and archiveVersion cannot be set outside task scope. Too Bad!
task("fatJar", Jar::class) {
    group = "build" //sets group in intelliJ's side bar
    manifest.attributes.apply {
        set("Main-Class", application.mainClassName)
        set("Implementation-Version", jarVersion)
        set("Specification-Title", specTitle)
        set("Implementation-Title", implTitle)
    }
    archiveBaseName.set("$jarName-$jarVersion")
    from(
        configurations.runtimeClasspath.get()
            .map {if(it.isDirectory) it else zipTree(it)}
    )
    with(tasks.jar.get())
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = artifact
            artifact(tasks["fatJar"])
        }
    }

    repositories {
        maven {
            url = uri("https://data.tilera.xyz/maven/")
            credentials {
                username = findProperty("mvnUsername") as String?
                password = findProperty("mvnPassword") as String?
            }
        }
    }
}

ktlint {
    version.set("0.37.2")
    enableExperimentalRules.set(true)
    coloredOutput.set(true)

    reporters {
        reporter(HTML)
        reporter(JSON)
        reporter(PLAIN)
    }

    disabledRules.set(
        setOf(
            //Disable really stupid rules
            "experimental:multiline-if-else",
            "comment-spacing",
            "curly-spacing",
            "keyword-spacing",
            "no-wildcard-imports"
        )
    )
}

//RUN THIS BEFORE COMMITTING! (i recommend adding a git pre-commit hook for this)
tasks.register("preCommit") {
    group = "verification"
    description = "runs the linter and tests"

    dependsOn(
        "ktlintCheck",
        "test"
    )
}

//tilera, please run this
tasks.register("installPreCommitHook") {
    group = "verification"
    description = "Installs a git pre-commit hook that runs the preCommit task"

    doLast {
        val git = File(project.projectDir, ".git")

        if(!git.exists()) {
            logger.warn("git dir not found")
            return@doLast
        }

        val hookFile = git.resolve("hooks/pre-commit")
        logger.info("hook file: $hookFile")

        if(hookFile.exists()) {
            logger.warn("Hook file already exists!")
            return@doLast
        }

        hookFile.writeText(
            """
                #!/bin/sh

                ###PRE COMMIT HOOK
                ./gradlew preCommit
            """.trimIndent()
        )
    }
}
