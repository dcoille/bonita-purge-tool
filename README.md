# bonita-purge-tool

[![Build](https://github.com/bonitasoft/bonita-purge-tool/workflows/Build%20&%20test%20Bonita%20Purge%20Tool/badge.svg)](https://github.com/bonitasoft/bonita-purge-tool/actions)

This tool provides the capability to purge finished (archived) process instances from [Bonita](https://documentation.bonitasoft.com) Runtime environment.
By default, all archives are preserved forever in Bonita runtime, but if your functional context allows you to lose old unused data, use this tool to cleanup your Bonita database.

## Build this tool
Bonita Purge Tool uses gradle & spring boot to build. Before building, it's recommended to modify the application.properties file located in src/main/resources to enter your database connection information.

Make sure you have Java 8+ installed and simply run:
    ./gradlew bootJar
    
### Run the tool

    java -jar build/libs/bonita-purge-tool.jar <PROCESS_DEFINITION_ID> <OLDEST_DATE_TIMESTAMP> [<TENAND_ID>]
    
This command will delete all archived process instances belonging to the process identified by **PROCESS_DEFINITION_ID**, that are finished since at least **OLDEST_DATE_TIMESTAMP**.

An optional TENAND_ID parameter can be given if the platform uses multiple tenants to specify on which tenant should the process instances be deleted. If multi-tenancy is used and the TENAND_ID is not set, an error is issued and the program stops.

OLDEST_DATE_TIMESTAMP must be a DATE from which all process instances that finished before that date will be deleted. Unfinished process instances and process instances that finished after that date will not be affected.
    
### CLI (Command line interface)

    bin/bonita-purge-tool(.bat) <PROCESS_DEFINITION_ID> <OLDEST_DATE_TIMESTAMP> [<TENAND_ID>]
