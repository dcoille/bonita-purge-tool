# Bonita Purge Tool

This tool provides the capability to purge finished (archived) process instances from [Bonita](https://documentation.bonitasoft.com) Runtime environment.
By default, all archives are preserved forever in Bonita runtime, but if your functional context allows you to remove old unused process instances,
as example, if you only need to keep a history of last 6 months, use this tool to cleanup your Bonita database. 


## Pre-requisites

This tool can be run on a Bonita runtime environment in version greater than or equal to 7.7.0.  
Bonita runtime environment should be shut down when running this tool, i.e. Bonita server should be stopped.


## Configuration

Enter your database configuration properties in the file `application.properties`


## Run Bonita Purge Tool

This command will delete all archived process instances belonging to the process identified by **PROCESS_DEFINITION_ID**,
that are finished since at least **OLDEST_DATE_TIMESTAMP**.


example (Unix):
>    bin/bonita-purge-tool <PROCESS_DEFINITION_ID> <OLDEST_DATE_TIMESTAMP> [<TENANT_ID>]

example (Windows):
>    bin/bonita-purge-tool.bat <PROCESS_DEFINITION_ID> <OLDEST_DATE_TIMESTAMP> [<TENANT_ID>]



An optional **TENANT_ID** parameter can be given if the platform uses multiple tenants to specify on which tenant should the process instances be deleted. If multi-tenancy is used and the TENANT_ID is not set, an error is issued and the program stops.

**OLDEST_DATE_TIMESTAMP** must be a Timestamp (in milliseconds) from which all process instances that finished before that date will be deleted.
Unfinished process instances and process instances that finished after that date will not be affected.
Its format is a standard Java timestamp since [EPOCH](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/Instant.html#EPOCH) (in milliseconds).
You can use websites such as [Epoch Converter](https://www.epochconverter.com/) to format such a timestamp.
