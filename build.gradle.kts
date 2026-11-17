import de.siphalor.jcyo.gradle.JcyoTask
import de.siphalor.minecraft_modding_toolkit.gradle.project_plugin.filter.JsonMergeFilterReader
import de.siphalor.mousewheelie.gradle.FormatReadmeForModSites

plugins {
	java
	`maven-publish`
	alias(mcLibs.plugins.smcmtk)
	alias(mcLibs.plugins.fabric.loom)
	alias(libs.plugins.licenser)
	alias(libs.plugins.jcyo)
	alias(libs.plugins.modPublisher)
	alias(libs.plugins.modrinth)
}

group = "de.siphalor.${project.name}"
val archivesBaseName = "${project.name}-mc${property("minecraft.version.descriptor")}"
val shortVersion = "${property("version")}"
version = "${shortVersion}+mc${mcLibs.versions.minecraft.get()}"

license {
	rule(file("LICENSE_HEADER"))
	include("**/*.java")
}

repositories {
	maven {
		name = "shedaniel"
		url = uri("https://maven.shedaniel.me/")
	}
	maven {
		name = "TerraformersMC"
		url = uri("https://maven.terraformersmc.com/releases")
	}
	maven {
		name = "Nucleoid"
		url = uri("https://maven.nucleoid.xyz")
		mavenContent {
			includeGroupAndSubgroups("eu.pb4")
		}
	}
	maven {
		name = "ParchmentMC"
		url = uri("https://maven.parchmentmc.org")
		mavenContent {
			includeGroup("org.parchmentmc.data")
		}
	}
	maven {
		name = "Siphalor"
		url = uri("https://maven.siphalor.de/")
		mavenContent {
			includeGroupAndSubgroups("de.siphalor")
		}
	}
	maven {
		name = "Jitpack"
		url = uri("https://jitpack.io")
		mavenContent {
			includeGroupAndSubgroups("com.github")
		}
	}
	mavenLocal()
}

// Workaround for https://github.com/gradle/gradle/issues/10195
configurations {
	include {
		isTransitive = true
		dependencies.filter { !it.name.contains("bom") }.forEach {
			(it as ModuleDependency).isTransitive = false
		}
	}
}

smcmtk {
	useMojangMappings()
	createModConfigurations(listOf(sourceSets.main.get()))
}

dependencies {
	annotationProcessor(libs.lombok)
	compileOnly(libs.lombok)

	minecraft(mcLibs.minecraft)
	"modImplementation"(libs.fabric.loader)

	compileOnly(libs.jspecify)

	for (mod in listOfNotNull(
		"fabric-api-base",
		"fabric-events-interaction-v0",
		"fabric-item-api-v1",
		smcmtk.mcProps.getting("fabric.api.item_group").orNull,
		"fabric-lifecycle-events-v1",
		smcmtk.mcProps.getting("fabric.api.key_mapping").get(),
		"fabric-message-api-v1",
		"fabric-networking-api-v1",
		"fabric-resource-loader-v0",
		"fabric-registry-sync-v0",
		"fabric-screen-api-v1",
	)) {
		"modImplementation"(fabricApi.module(mod, mcLibs.versions.fabric.api.get()))
	}

	"modImplementation"(mcLibs.modmenu)

	include(mcLibs.bundles.config)
	"modApi"(mcLibs.bundles.config) {
		exclude(group = "net.fabricmc.fabric-api")
		exclude(group = "de.siphalor.amecs-api")
	}

	include(mcLibs.amecs.key.mapping.descriptions)
	"modImplementation"(mcLibs.amecs.key.mapping.descriptions) {
		exclude(group = "net.fabricmc.fabric-api")
	}
	include(mcLibs.amecs.mouse.inputs)
	"modImplementation"(mcLibs.amecs.mouse.inputs) {
		exclude(group = "net.fabricmc.fabric-api")
	}
	include(mcLibs.amecs.priority.key.mappings)
	"modImplementation"(mcLibs.amecs.priority.key.mappings) {
		exclude(group = "net.fabricmc.fabric-api")
	}
}

tasks.processResources {
    inputs.property("version", project.version)
	inputs.property("extraClientMixins", smcmtk.mcProps.getting("mixins.extra.client"))

	filesMatching("fabric.mod.json") {
		filter<JsonMergeFilterReader>("merge" to mapOf(
			"version" to project.version,
			"depends" to mapOf(
				smcmtk.mcProps.getting("fabric.api.key_mapping").get() to "*"
			)
		))
	}
	filesMatching("mousewheelie.mixins.json") {
		filter<JsonMergeFilterReader>("merge" to mapOf(
			"client" to (smcmtk.mcProps.getting("mixins.extra.client").orNull?.let {
				it.split(",").map { mixin -> mixin.trim() }
			} ?: listOf())
		))
	}
}

java {
	sourceCompatibility = JavaVersion.toVersion(mcLibs.versions.java.get())
	targetCompatibility = JavaVersion.toVersion(mcLibs.versions.java.get())
	withSourcesJar()
}

val jcyo = tasks.register<JcyoTask>("jcyo") {
	inputDirectory = file("src/main/java")
	variables = smcmtk.mcProps.map {
		it.filterKeys { key -> key.startsWith("preprocessor.") }.mapKeys { (key, _) -> key.substring("preprocessor.".length) }
	}
	importOrder = listOf(
		"",
		"net.minecraft",
		"\\#",
	)
}

tasks.compileJava {
	dependsOn(jcyo)
}

publishing {
	publications {
		create<MavenPublication>("mod") {
			artifactId = archivesBaseName
			version = shortVersion

			from(components["java"])

			pom {
				name.set("Mouse Wheelie")
				description.set("A \"small\" client-side Minecraft mod providing various mouse wheel related utilities.")
				url.set("https://github.com/Siphalor/mouse-wheelie")

				scm {
					url.set("https://github.com/Siphalor/mouse-wheelie")
				}
			}
		}
	}

	repositories {
		if (project.hasProperty("siphalor.maven.user")) {
			maven {
				name = "Siphalor"
				url = uri("https://maven.siphalor.de/upload.php")
				credentials {
					username = project.property("siphalor.maven.user") as String
					password = project.property("siphalor.maven.password") as String
				}
			}
		}
	}
}

publisher {
	apiKeys {
		project.findProperty("modrinth.token")?.let { modrinth(it as String) }
		project.findProperty("curseforge.token")?.let { curseforge(it as String) }
		project.findProperty("github.token")?.let { github(it as String) }
	}

	curseID = "317514"
	modrinthID = "u5Ic2U1u"

	artifact.set(tasks.findByName("remapJar") ?: tasks.jar)

	projectVersion = project.version as String
	versionType = project.property("version.type") as String
	loaders = listOf("fabric")
	curseEnvironment = "client"

	gameVersions = smcmtk.mcProps.getting("mc.version.supported").map { it.split(", ") }

	displayName = "[${smcmtk.mcProps.getting("mc.version.title").get()}] $shortVersion"
	changelog.set(providers.exec {
		commandLine("git", "log", "-1", "--format=format:##%x20%s%n%n%b", "--grep", "Version")
	}.standardOutput.asText.map { it.trim() })

	curseDepends {
		required("fabric-api")
	}
	modrinthDepends {
		required("fabric-api")
	}

	github {
		repo("Siphalor/mouse-wheelie")
		tag(shortVersion)
		displayName(shortVersion)
		createTag(true)
		createRelease(true)
	}
}

val formatReadmeForModSites = tasks.register<FormatReadmeForModSites>("formatReadmeForModSites") {
	input = layout.projectDirectory.file("README.md")
	output = layout.buildDirectory.dir("readme")
}

modrinth {
	project.findProperty("modrinth.token")?.let { token = it as String }
	projectId = "u5Ic2U1u"

	syncBodyFrom = providers.fileContents(layout.buildDirectory.file("readme/modrinth.md")).asText
}

tasks.modrinth {
	enabled = false
}

tasks.modrinthSyncBody {
	dependsOn(formatReadmeForModSites)
}
