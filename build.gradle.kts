plugins {
    application
}

group = "org.devore"
version = "0.1-alpha"

application {
    mainClass.set("org.devore.Main")
}

val isWindows = System.getProperty("os.name").lowercase().contains("win")
val windowsNativeBuildDir = layout.buildDirectory.dir("native/windows")

val compileWindowsReplJni by tasks.registering(Exec::class) {
    group = "build"
    description = "Compiles the Windows JNI library used by the REPL manual echo mode."
    enabled = isWindows
    onlyIf { isWindows }
    val javaHome = providers.systemProperty("java.home")
    inputs.file("src/main/native/windows/repl_console.c")
    outputs.file(windowsNativeBuildDir.map { it.file("devore-repl.dll") })
    doFirst {
        windowsNativeBuildDir.get().asFile.mkdirs()
    }
    commandLine(
        "cmd", "/c",
        "cl",
        "/nologo",
        "/LD",
        "/I${javaHome.get()}\\include",
        "/I${javaHome.get()}\\include\\win32",
        "/Fo:${windowsNativeBuildDir.get().asFile}\\",
        "src\\main\\native\\windows\\repl_console.c",
        "/Fe:${windowsNativeBuildDir.get().asFile}\\devore-repl.dll",
        "/link",
        "/NOLOGO",
        "/IMPLIB:${windowsNativeBuildDir.get().asFile}\\devore-repl.lib"
    )
}

tasks.processResources {
    if (isWindows) {
        dependsOn(compileWindowsReplJni)
        from(windowsNativeBuildDir) {
            include("devore-repl.dll")
            into("native")
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "org.devore.Main",
            "Implementation-Version" to version
        )
    }
}
