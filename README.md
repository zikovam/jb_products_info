# Readme file
## Description
After start:
- Application downloading file __update.xml__ from address 
https://www.jetbrains.com/updates/updates.xml. 
- Then it accessing https://data.services.jetbrains.com/products for every product and get builds information for every build not older than one year from today. 
- All information collected then saved-or-updated to database
- All builds in the db with linux version and without stored __build-info.json__ information are sent to downloading

Every hour first three steps are running again, and then application sending new builds to download query

# Run application
To run application (all necessary files will appear in project folder):
```
./gradlew bootRun
```
---
To run tests:
```
./gradlew clean tests
```


## API
To call API you can use __Postman__, there are two files with collection and variables for this application:
- [JB_Test_Task_Variables](JB_Test_Task_Variables.postman_environment.json)
- [JB_Test_Task_Collection](JB_Test_Task_Collection.postman_collection.json)

| **Endpoint**                   	 | **Description**                                             	 |
|----------------------------------|---------------------------------------------------------------|
| /                              	 | Status information                                          	 |
| /status                        	 | JSON with status information                                	 |
| /<product-code>                	 | For every stored build in DB, returns JSON with information 	 |
| /<product-code>/<build-number> 	 | product-info.json data for requested build                  	 |
| /refresh                       	 | Refresh all information                                     	 |
| /refresh/<product-code>        	 | Refresh information for particular product                  	 |