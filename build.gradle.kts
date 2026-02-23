plugins {
    id("java")
    // Se añade el plugin de Shadow para habilitar la tarea 'shadowJar'
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Repositorio oficial de Hytale necesario para las dependencias del servidor [3]
    maven {
        name = "hytale"
        url = uri("https://maven.hytale.com/release")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // En lugar de usar un archivo local, se recomienda la dependencia oficial [4]
    implementation("com.hypixel.hytale:Server:+")
}

tasks.test {
    useJUnitPlatform()
}

// Configuración adicional para asegurar que el JAR se construya correctamente
tasks.shadowJar {
    archiveClassifier.set("") // Elimina el sufijo -all del archivo resultante
}