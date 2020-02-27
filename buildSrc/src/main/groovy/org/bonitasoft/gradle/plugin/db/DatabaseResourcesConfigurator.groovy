package org.bonitasoft.gradle.plugin.db

import org.gradle.api.Project

class DatabaseResourcesConfigurator {

    def static configureDatabaseResources(Project project) {
        String dbVendor = System.getProperty("db.vendor", "postgres")
        DatabasePluginExtension extension = project.extensions.getByType(DatabasePluginExtension.class)
        extension.dbvendor = dbVendor
        project.logger.info("no system property db.url set, using docker to test on $dbVendor")
        DockerDatabaseContainerTasksCreator.createTasks(project)
    }

    def static finalizeTasksDependenciesOnDatabaseResources(Project project) {
        DatabasePluginExtension extension = project.extensions.getByType(DatabasePluginExtension.class)
        def integrationTestTask = project.tasks.findByName('integrationTest')
        def uniqueName = "${extension.dbvendor.capitalize()}"
        def vendorConfigurationTask = project.tasks.findByName("${uniqueName}Configuration")
        def removeVendorContainer = project.tasks.findByName("remove${uniqueName}Container")

        // Integration tests
        integrationTestTask.dependsOn(vendorConfigurationTask)
        removeVendorContainer.mustRunAfter(integrationTestTask)
    }

}
