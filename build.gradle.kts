plugins {
    id("java")
    application
}

group = "org.devore"
version = "0.1-alpha"

repositories {
    mavenCentral()
}

dependencies {}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.devore.Main"
        attributes["Implementation-Version"] = project.version
    }
}