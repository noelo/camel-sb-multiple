# Spring boot FIS 2.0 playground project

* Multi-module Spring boot applicaton using routes defined in modules
* Spring-cloud-kubernetes configmap & secrets 


## To use Spring-cloud-kubernetes 
1. Add the spring.application.name to application.properties, e.g. spring.application.name=demoapp. Ensure that the name follows the naming conventions for configmaps
2. Add the following properties to application.properties file 
* spring.cloud.kubernetes.enabled=true
* spring.cloud.kubernetes.configmap.enabled=true
3. Create and populate the related configmap e.g. oc create configmap demoapp --from-file=application.properties  
NOTE the configmap name much match the value specified in spring.application.name

