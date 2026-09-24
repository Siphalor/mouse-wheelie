pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
			mavenContent {
				includeGroupAndSubgroups("fabric-loom")
				includeGroupAndSubgroups("net.fabricmc")
			}
		}
		maven {
			name = "Siphalor's Maven"
			url = uri("https://maven.siphalor.de")
			mavenContent {
				includeGroupAndSubgroups("de.siphalor")
			}
		}
		maven {
			url = uri("https://maven.firstdark.dev/releases")
			mavenContent {
				includeGroupAndSubgroups("com.hypherionmc")
			}
		}
		gradlePluginPortal()
		mavenLocal()
	}
}

plugins {
	id("de.siphalor.minecraft-modding-toolkit.settings-plugin") version("0.1.3")
}

smcmtk {
	fabricLoomVersion = "1.19-SNAPSHOT"
}

rootProject.name = "mouse-wheelie"
