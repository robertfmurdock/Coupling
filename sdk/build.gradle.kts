@file:OptIn(ApolloExperimental::class)

import com.apollographql.apollo.annotations.ApolloExperimental

plugins {
    alias(libs.plugins.com.zegreatrob.tools.certifier)
    alias(libs.plugins.com.apollographql.apollo)
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js {
        nodejs { testTask { useMocha { timeout = "10s" } } }
    }
    jvm()
}

apollo {

    service("service") {
        generateApolloMetadata = true
        generateAsInternal = false
        generateSourcesDuringGradleSync = true
        packageName = "com.zegreatrob.coupling.sdk.schema"
        schemaFiles.from(
            file("../server/src/jsMain/resources/prerelease-schema.graphqls"),
            file("../server/src/jsMain/resources/schema.graphqls"),
        )
        introspection {
            endpointUrl = "https://localhost/graphql"
            headers.put("api-key", "1234567890abcdef")
            schemaFile = file("src/main/graphql/schema.graphqls")
        }
        mapScalar(
            "ContributionId",
            "com.zegreatrob.coupling.json.ContributionIdString",
            "com.zegreatrob.coupling.sdk.adapter.contributionAdapter"
        )
        mapScalar("DateTimeISO", "kotlin.time.Instant", "com.apollographql.adapter.core.KotlinInstantAdapter")
        mapScalar("Duration", "kotlin.time.Duration", "com.zegreatrob.coupling.sdk.adapter.KotlinDurationAdapter")
        mapScalar(
            "Email",
            "kotools.types.text.NotBlankString",
            "com.zegreatrob.coupling.sdk.adapter.notBlankStringAdapter"
        )
        mapScalar("Float", "Double")
        mapScalar("ID", "String")
        mapScalar(
            "PairAssignmentDocumentId",
            "com.zegreatrob.coupling.json.PairAssignmentDocumentIdString",
            "com.zegreatrob.coupling.sdk.adapter.pairAssignmentDocumentIdAdapter"
        )
        mapScalar(
            "PartyId",
            "com.zegreatrob.coupling.json.PartyIdString",
            "com.zegreatrob.coupling.sdk.adapter.partyIdAdapter"
        )
        mapScalar(
            "PinId",
            "com.zegreatrob.coupling.json.PinIdString",
            "com.zegreatrob.coupling.sdk.adapter.pinIdAdapter"
        )
        mapScalar(
            "PlayerId",
            "com.zegreatrob.coupling.json.PlayerIdString",
            "com.zegreatrob.coupling.sdk.adapter.playerIdAdapter"
        )
        mapScalar(
            "SecretId",
            "com.zegreatrob.coupling.json.SecretIdString",
            "com.zegreatrob.coupling.sdk.adapter.secretIdAdapter"
        )
        mapScalar(
            "UserId",
            "com.zegreatrob.coupling.json.UserIdString",
            "com.zegreatrob.coupling.sdk.adapter.userIdAdapter"
        )
    }
}

dependencies {
    "commonMainApi"(project(":libraries:action"))
    "commonMainApi"(project(":libraries:model"))
    "commonMainApi"(project(":libraries:json"))
    "commonMainApi"("com.apollographql.apollo:apollo-runtime")
    "commonMainApi"("com.apollographql.adapters:apollo-adapters-core")
    "commonMainImplementation"(project(":libraries:repository:core"))
    "commonMainImplementation"("com.apollographql.ktor:apollo-engine-ktor")
    "commonMainImplementation"("io.ktor:ktor-client-core")
    "commonMainImplementation"("io.ktor:ktor-client-logging")
    "commonMainImplementation"("io.ktor:ktor-client-websockets")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    "commonTestImplementation"("io.ktor:ktor-serialization-kotlinx-json")
    "commonTestImplementation"("io.ktor:ktor-client-content-negotiation")
    "commonTestImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-json")
    "commonTestImplementation"(project(":libraries:repository:validation"))
    "commonTestImplementation"(project(":libraries:stub-model"))
    "commonTestImplementation"(project(":libraries:test-logging"))
    "commonTestImplementation"("com.zegreatrob.testmints:async")
    "commonTestImplementation"("com.zegreatrob.testmints:minassert")
    "commonTestImplementation"("com.zegreatrob.testmints:standard")
    "commonTestImplementation"("io.github.oshai:kotlin-logging")
    "commonTestImplementation"("org.jetbrains.kotlin:kotlin-test")

    "jsTestImplementation"(project(":server:slack"))
    "jvmTestImplementation"("io.ktor:ktor-client-java")
}

tasks {
    val jsNodeTest by getting {
        dependsOn(":composeUp")
        outputs.cacheIf { true }
    }
    installCert {
        dependsOn(":caddyComposeUp")
        jdkSelector = "22"
        certificatePath = "${System.getenv("HOME")}/caddy_data/caddy/pki/authorities/local/root.crt"
    }
    "jvmTest" {
        mustRunAfter(jsNodeTest)
        dependsOn(":composeUp")
        dependsOn(installCert)
    }
}
