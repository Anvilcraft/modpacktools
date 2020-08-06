import groovy.lang.GroovyObject
import org.gradle.jvm.tasks.Jar

plugins {
    id("java")
    id("application")
    id("idea")
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
}

val jarName = "ModPackTools"
group = "ley.anvil"
application.mainClassName = "ley.anvil.modpacktools.Main"

version = "1.1-SNAPSHOT"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    //Compile
    arrayOf(
        "com.github.Anvilcraft:addonscript-java:c412911790",

        "org.jetbrains.kotlin:kotlin-reflect:1.3.72",
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
        "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.8",
        "com.squareup.okhttp3:okhttp:4.8.0",
        "com.moandjiezana.toml:toml4j:0.7.2",
        "com.github.TheRandomLabs:CurseAPI:master-SNAPSHOT",
        "org.apache.commons:commons-csv:1.8",
        "org.slf4j:slf4j-simple:2.0.0-alpha1",
        "com.j2html:j2html:1.4.0",
        "org.reflections:reflections:0.9.12",
        "commons-io:commons-io:2.7",
        "net.sourceforge.argparse4j:argparse4j:0.8.1",
        "com.github.ajalt:mordant:1.2.1",
        "com.google.code.gson:gson:2.8.6"
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {kOptions(this as GroovyObject)}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinTest> {kOptions(this as GroovyObject)}

@Suppress("DEPRECATION")
task("fatJar", Jar::class) {
    manifest {
        attributes["Main-Class"] = application.mainClassName
        attributes["Implementation-Version"] = version
        attributes["Implementation-Title"] = jarName
    }
    archiveBaseName.set(jarName)
    from(configurations.runtimeClasspath.get().map {if(it.isDirectory) it else zipTree(it)})
    with(tasks.jar.get())
}