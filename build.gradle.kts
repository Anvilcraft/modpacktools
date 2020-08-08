import groovy.lang.GroovyObject
import org.gradle.api.JavaVersion.VERSION_1_8
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinTest

//settings for manifest and stuff
val specTitle = "ModPackTools"
val jarName = specTitle
val implTitle = "ley.anvil.modpacktools"
group = "ley.anvil"
application.mainClassName = "ley.anvil.modpacktools.Main"

version = "1.2-SNAPSHOT"

plugins {
    id("java")
    id("application")
    id("idea")
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
}

configure<JavaPluginConvention> {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
}

repositories {
    mavenCentral()
    maven("https://data.tilera.xyz/maven/") //Used for Addonscript
}

dependencies {
    //Implementation
    arrayOf(
        //Anvilcraft
        "ley.anvil:addonscript:1.0-SNAPSHOT",

        //Kotlin
        "org.jetbrains.kotlin:kotlin-reflect:1.3.72",
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
        "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.8",

        //IO
        "com.squareup.okhttp3:okhttp:4.8.0", //HTTP client
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
        "org.reflections:reflections:0.9.12"
    ).forEach {implementation(it)}

    testImplementation("junit:junit:4.12")
}

//This exists so options can be set for test compile and compile at once
val kOptions = {obj: GroovyObject ->
//This is sorta garbage, but if i would do this the proper way Intelli'ntJ would think its an error even tho it is not
    obj.withGroovyBuilder {
        "kotlinOptions" {
            setProperty("jvmTarget", "1.8")
            //without this option, kotlin interfaces dont support default methods in java (will be changed in kt 1.4)
            setProperty("freeCompilerArgs", arrayOf("-Xjvm-default=compatibility"))
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
        set("Implementation-Version", version)
        set("Specification-Title", specTitle)
        set("Implementation-Title", implTitle)
    }
    archiveBaseName.set(jarName)
    from(configurations.runtimeClasspath.get()
            .map {if(it.isDirectory) it else zipTree(it)})
    with(tasks.jar.get())
}