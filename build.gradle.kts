plugins {
    java
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.flywaydb.flyway") version "10.15.0"
}

group = "com.charge.station"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    
    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    
    // PostGIS for spatial data
    implementation("net.postgis:postgis-jdbc:2023.1.0")
    implementation("org.hibernate:hibernate-spatial:6.5.2.Final")
    
    // Redis client
    implementation("org.redisson:redisson-spring-boot-starter:3.27.2")
    
    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // SpringDoc OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    
    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("com.h2database:h2")
    
    // Test runtime
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()

    // 并行测试执行 - 利用多核处理器
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

    // JVM 优化
    jvmArgs(
        "-XX:+UseG1GC",
        "-XX:+UseStringDeduplication",
        "-Xmx2g",
        "-XX:MaxMetaspaceSize=512m"
    )

    // 测试输出优化
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        showExceptions = true
        showCauses = true
        showStackTraces = false
    }

    // 失败快速停止 (可选)
    failFast = false
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "-parameters",
        "-Xlint:unchecked",
        "-Xlint:deprecation"
    ))

    // 编译器优化
    options.isFork = true
    options.forkOptions.jvmArgs?.addAll(listOf(
        "-Xmx2g",
        "-XX:+UseG1GC"
    ))

    // 增量编译
    options.isIncremental = true
}

// Flyway configuration
flyway {
    url = "jdbc:postgresql://localhost:5432/charge_station"
    user = "postgres"
    password = "postgres"
    schemas = arrayOf("public")
    locations = arrayOf("classpath:db/migration")
}
