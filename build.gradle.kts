plugins {
    application
}

group = "org.devore"
version = "0.1-alpha"

application {
    mainClass.set("org.devore.Main")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

val isWindows = System.getProperty("os.name").lowercase().contains("win")
val windowsNativeBuildDir = layout.buildDirectory.dir("native/windows")

val compileWindowsReplJni by tasks.registering(Exec::class) {
    group = "build"
    description = "Compiles the Windows JNI library used by the REPL manual echo mode."
    enabled = isWindows
    onlyIf { isWindows }
    inputs.file("src/main/native/windows/repl_console.c")
    outputs.file(windowsNativeBuildDir.map { it.file("devore-repl-console.dll") })
    doFirst {
        windowsNativeBuildDir.get().asFile.mkdirs()
        val javaHome = providers.environmentVariable("JAVA_HOME").orNull
            ?: providers.systemProperty("java.home").get()
        val jdkHome = file(javaHome).let { home ->
            if (home.resolve("include/jni.h").isFile) home else home.parentFile
        }
        if (jdkHome == null || !jdkHome.resolve("include/jni.h").isFile)
            throw GradleException("Cannot find jni.h. Set JAVA_HOME to a JDK installation.")
        commandLine(
            "cmd", "/c",
            "cl",
            "/nologo",
            "/LD",
            "/I${jdkHome}\\include",
            "/I${jdkHome}\\include\\win32",
            "/Fo:${windowsNativeBuildDir.get().asFile}\\",
            "src\\main\\native\\windows\\repl_console.c",
            "/Fe:${windowsNativeBuildDir.get().asFile}\\devore-repl-console.dll",
            "/link",
            "/NOLOGO",
            "/IMPLIB:${windowsNativeBuildDir.get().asFile}\\devore-repl-console.lib"
        )
    }
}

tasks.processResources {
    if (isWindows) {
        dependsOn(compileWindowsReplJni)
        from(windowsNativeBuildDir) {
            include("devore-repl-console.dll")
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
