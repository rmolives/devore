plugins {
    application
}

group = "org.devore"
version = "0.1-alpha"

application {
    mainClass.set("org.devore.Main")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "org.devore.Main",
            "Implementation-Version" to version
        )
    }
}