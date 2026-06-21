plugins {
    id("java")
    application
}

group = "org.devore"
version = "0.1-alpha"

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.devore.Main"
        attributes["Implementation-Version"] = project.version
    }
}