import org.bonitasoft.gradle.plugin.db.DatabasePluginExtension
import org.bonitasoft.gradle.plugin.db.DatabaseResourcesConfigurator
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.application.CreateBootStartScripts
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    kotlin("jvm") version "1.3.40"
    application
    id("org.springframework.boot") version "2.1.6.RELEASE"

}
apply(plugin = "io.spring.dependency-management")
apply(plugin = "maven-publish")

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

group = "org.bonitasoft.engine.purge"

configurations {
    create("distributionZip")
}

application {
    mainClassName = "org.bonitasoft.engine.purge.ApplicationKt"
}
springBoot {
    distributions.getByName("boot") {
        contents {
            from("distrib/") {
                include("*")
            }
        }
        distributionBaseName.set(project.name)
    }
}
tasks["distZip"].enabled = false
tasks["distTar"].enabled = false

tasks.getByName<BootRun>("bootRun") {
    if (project.hasProperty("args")) {
        args = project.property("args").toString().split(',')
    }
}

artifacts {
    add("distributionZip", tasks["bootDistZip"])
}

val startScripts: CreateBootStartScripts = project.tasks.getByName("bootStartScripts") as CreateBootStartScripts
startScripts.apply {
    doLast {
        val windowsContent = String(windowsScript.readBytes())
        windowsScript.printWriter().use { writer ->
            val newContent = windowsContent.replaceFirst("-jar ", "-Dspring.config.location=%APP_HOME%/ -classpath %APP_HOME%/lib/*  -jar ")
            writer.write(newContent)
        }
        val unixContent = String(unixScript.readBytes())
        unixScript.printWriter().use { writer ->
            val newContent = unixContent.replaceFirst("-jar ", "-Dspring.config.location=\$APP_HOME/ -classpath \"\$APP_HOME/lib/*\"  -jar ")
            writer.write(newContent)
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.projectreactor:reactor-core:3.2.10.RELEASE")
    implementation("org.springframework.boot", "spring-boot-starter-jdbc")
    implementation("org.jetbrains.exposed", "exposed", "0.14.2")

    implementation("mysql:mysql-connector-java:8.0.14")
    implementation("org.postgresql:postgresql:42.2.5")
    implementation("com.microsoft.sqlserver:mssql-jdbc:7.2.1.jre8")

    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.apache.commons", "commons-csv", "1.6")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("io.mockk:mockk:1.9.3")

    testRuntimeOnly("com.oracle:ojdbc:8.12.2.0.1")

}
tasks.withType<Test> {
    useJUnitPlatform()
}

project.extensions.create("database", DatabasePluginExtension::class)

project.afterEvaluate {
    DatabaseResourcesConfigurator.configureDatabaseResources(project)
    DatabaseResourcesConfigurator.finalizeTasksDependenciesOnDatabaseResources(project)
}


// Configure INTEGRATION TESTS:
val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests on a real database."
    group = "Verification"
    testClassesDirs = project.sourceSets.getByName("test").output.classesDirs
    classpath = project.sourceSets.getByName("test").runtimeClasspath
    reports.html.destination = project.file("${project.buildDir}/reports/integrationTest")
    include("**/*IT.class")
    shouldRunAfter("test")
    doFirst {
        val configuration = project.extensions.getByType(DatabasePluginExtension::class)
        val mapOf = mapOf(
                "spring.datasource.url" to configuration.dburl,
                "spring.datasource.username" to configuration.dbuser,
                "spring.datasource.password" to configuration.dbpassword,
                "spring.datasource.driver-class-name" to configuration.dbdriverClass
        )
        logger.quiet("Using database configuration: $mapOf")
        systemProperties = mapOf
    }
}
tasks.check { dependsOn(integrationTest) }
