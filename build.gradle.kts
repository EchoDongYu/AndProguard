plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.murphy.proguard"
version = "3.6.3"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
// http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/build_number_ranges.html
intellij {
    version.set("2021.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java", "org.jetbrains.kotlin", "org.jetbrains.android"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("211")
        untilBuild.set("")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        // perm:eXVfZG9uZw==.OTItMTAxODM=.OxxeFjW1mdccTSV8Xk1LybzC44YuOv
        token.set(System.getenv("perm:eXVfZG9uZw==.OTItMTAxODM=.OxxeFjW1mdccTSV8Xk1LybzC44YuOv"))
        channels.set(listOf("eap"))
    }
}
