plugins {
	java
	id("org.springframework.boot") version "3.2.3"
	id("io.spring.dependency-management") version "1.1.4"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
}

group = "kr.online"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

val snippetsDir by extra { file("build/generated-snippets") }
val asciidoctorExt: Configuration by configurations.creating

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-test")
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("com.mysql:mysql-connector-j")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs("-Dfile.encoding=UTF-8")
	outputs.dir(snippetsDir)
}

tasks.asciidoctor {
	configurations(asciidoctorExt.name)
	dependsOn(tasks.test)
	doFirst {
		delete(file("src/main/resources/static/docs"))
	}
	inputs.dir(snippetsDir)
	doLast {
		copy {
			from("build/docs/asciidoc")
			into("src/main/resources/static/docs")
		}
	}
}

sourceSets {
	main {
		java.srcDirs("src/main/java")
		resources.srcDirs("src/main/resources/static/docs")
	}
}

tasks.getByName<Jar>("jar") {
	enabled = false
}