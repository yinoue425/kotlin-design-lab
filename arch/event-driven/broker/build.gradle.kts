plugins {
    application
}

application {
    mainClass.set(providers.gradleProperty("mainClass").getOrElse("eventdriven.broker.MainKt"))
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.9.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}
