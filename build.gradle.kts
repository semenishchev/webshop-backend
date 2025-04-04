import com.webcohesion.enunciate.Enunciate
import com.webcohesion.enunciate.gradle.EnunciateTask

plugins {
    java
    id("io.quarkus")
    id("com.webcohesion.enunciate") version "2.18.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("org.apache.maven:maven-model:3.8.4")
    implementation("io.quarkus:quarkus-redis-cache")
    implementation("io.quarkus:quarkus-redis-client")
    implementation("io.quarkus:quarkus-cache")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-jdbc-h2")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.stripe:stripe-java:28.4.0")
    implementation("io.quarkus:quarkus-kubernetes-config")
    implementation("io.quarkus:quarkus-mailer")
    implementation("io.quarkus:quarkus-qute")
    implementation("dev.samstevens.totp:totp:1.7.1")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-jdbc-h2")
}

group = "cc.olek"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<EnunciateTask> {
    configurationFileName = "src/main/resources/enunciate.xml"
    sourcepath("src/main/java")
}