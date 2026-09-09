plugins {
    id("java")
    // Shadow plugin para crear fat/uber JARs
    id("com.gradleup.shadow") version "9.4.3"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Repositorio oficial de Hytale necesario para las dependencias del servidor
    maven {
        name = "hytale"
        url = uri("https://maven.hytale.com/release")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Hytale Server API - compileOnly porque el servidor ya provee estas clases en runtime
    compileOnly("com.hypixel.hytale:Server:+")
    // Gson se empaqueta dentro del JAR porque es dependencia propia
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.test {
    useJUnitPlatform()
}

// Configuración adicional para asegurar que el JAR se construya correctamente
tasks.shadowJar {
    archiveClassifier.set("") // Elimina el sufijo -all del archivo resultante
}