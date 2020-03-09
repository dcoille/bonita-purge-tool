# bonita-purge-tool

[![Build](https://github.com/bonitasoft/bonita-purge-tool/workflows/Build%20&%20test%20Bonita%20Purge%20Tool/badge.svg)](https://github.com/bonitasoft/bonita-purge-tool/actions)

This tool provides the capability to purge finished (archived) process instances from [Bonita](https://documentation.bonitasoft.com) Runtime environment.
By default, all archives are preserved forever in Bonita runtime, but if your functional context allows you to remove old unused process instances, as example, if you only need to keep a history of last 6 months, use this tool to cleanup your Bonita database. 

## How to use this tool

### Build this tool
Bonita Purge Tool uses gradle & spring boot to build. Before building, it's recommended to modify the application.properties file located in src/main/resources to enter your database connection information.

Make sure you have Java 8+ installed and simply run:
    ./gradlew bootJar
    
### Run the tool

    java -jar build/libs/bonita-purge-tool.jar <PROCESS_DEFINITION_ID> <OLDEST_DATE_TIMESTAMP> [<TENANT_ID>]
    
This command will delete all archived process instances belonging to the process identified by **PROCESS_DEFINITION_ID**, that are finished since at least **OLDEST_DATE_TIMESTAMP**.

An optional TENANT_ID parameter can be given if the platform uses multiple tenants to specify on which tenant should the process instances be deleted. If multi-tenancy is used and the TENANT_ID is not set, an error is issued and the program stops.

OLDEST_DATE_TIMESTAMP must be a Timestamp (in milliseconds) from which all process instances that finished before that date will be deleted.
Unfinished process instances and process instances that finished after that date will not be affected.
Its format is a standard Java timestamp since [EPOCH](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/Instant.html#EPOCH) (in milliseconds).
You can use websites such as [Epoch Converter](https://www.epochconverter.com/) to format such a timestamp.
    
### Run the tool using the CLI (Command line interface)

    bin/bonita-purge-tool(.bat) <PROCESS_DEFINITION_ID> <OLDEST_DATE_TIMESTAMP> [<TENANT_ID>]

## Developer's corner

### Run the unit tests
    ./gradlew test

### Run the integration tests
Integration tests rely on database docker images that are only available for internal Bonitasofter's for now.

    ./gradlew integrationTest

or

    ./gradlew iT

to specify the database vendor on which to run the integration tests add `-Ddb.vendor=<DB_VENDOR>`. For instance:

    ./gradlew iT -Ddb.vendor=mysql
    
valid DB vendor values are: `mysql | oracle | postgres | sqlserver`.  
If not specified, it will start a **Postgres** docker container and run tests against it.