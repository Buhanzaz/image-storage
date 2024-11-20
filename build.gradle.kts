plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.blockBox"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("software.amazon.awssdk:s3:2.29.15")
    implementation("io.minio:minio:8.5.13")
    implementation("net.coobird:thumbnailator:0.4.20")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    runtimeOnly("software.amazon.awssdk:bom:2.29.15")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    implementation("software.amazon.awssdk:s3:2.29.15")
    implementation("software.amazon.awssdk:netty-nio-client:2.29.15")
    implementation("com.twelvemonkeys.imageio:imageio-jpeg:3.12.0")


    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


}

tasks.withType<Test> {
    useJUnitPlatform()
}
