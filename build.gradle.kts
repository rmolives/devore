plugins {
    id("java")
}

group = "org.wumoe.devore"
version = "0.1-alpha"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.obermuhlner:big-math:2.3.2")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.wumoe.devore.Main"
    }

    from(configurations.runtimeClasspath.get().filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) })
}