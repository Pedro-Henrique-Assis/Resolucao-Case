plugins {
	java
	id("org.springframework.boot") version "3.4.11"
	id("io.spring.dependency-management") version "1.1.7"
	id("jacoco")
}

group = "com.case"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
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
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
	runtimeOnly("com.microsoft.sqlserver:mssql-jdbc")
	implementation("io.micrometer:micrometer-registry-prometheus")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Adiciona a finalização do JaCoCo à tarefa de teste padrão
tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.named("jacocoTestReport")) // Garante que o relatório seja gerado após os testes
}

// Configura o relatório JaCoCo
tasks.named<JacocoReport>("jacocoTestReport") {
	reports {
		xml.required = true
		csv.required = false
		html.outputLocation = layout.buildDirectory.dir("reports/jacoco/test/html")
	}
	// Define quais classes devem ser analisadas (excluindo DTOs, Exceptions, etc.)
	classDirectories.setFrom(files(classDirectories.files.map {
		fileTree(it) {
			exclude(
					"**/json/**", // Exclui DTOs de request/response
					"**/model/**", // Exclui Entidades (geralmente só getters/setters)
					"**/exception/**", // Exclui classes de Exceção
					"**/infra/**", // Exclui classes de infra (RestExceptionHandler)
					"**/*Application.class" // Exclui a classe principal da aplicação
			)
		}
	}))
}

// Configura a verificação de cobertura
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
	// Garante que a verificação só rode depois que o relatório for gerado
	dependsOn(tasks.named("jacocoTestReport"))

	violationRules {
		rule {
			element = "CLASS" // Define que a regra se aplica a nível de Classe

			// Configura a regra de cobertura de linhas
			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "0.80".toBigDecimal()
			}
		}
	}

	// Define quais classes devem ser verificadas
	classDirectories.setFrom(files(classDirectories.files.map {
		fileTree(it) {
			exclude(
					"**/json/**",
					"**/model/**",
					"**/exception/**",
					"**/infra/**",
					"**/*Application.class"
			)
		}
	}))
}

// Faz a tarefa 'build' depender da verificação
tasks.named("build") {
	dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
