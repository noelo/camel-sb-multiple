
### Template Generation
The templates are composed of resource (DC, SVC) fragments in the src/main/fabric8 directory.

The BuildConfig & ImageStream templates reside in the src/main/resources/static directory

### Application & Resource Naming
As part of the resource generation using template the template will automatically name resources based on a combination of the the *APPNAME* and a random generated string.

This ***name*** is used in all resources as well as the **spring.application.name** property which is used in the retrieval of *ConfigMaps* and *Secrets* 

In the examples below all resource use the name **springdemo-pgk2ierggpft**


### Creating the Buildconfig and ImageStream from a template

```
$oc process -v APPNAME=springdemo -v ISTAG=iter1 -v APPOWNER=`oc whoami`  -f build-config.yml |oc create -f-
buildconfig "springdemo-bc" created
imagestream "springdemo-is" created

$oc start-build bc/springdemo-bc --from-file=/Users/admin/dev/ose-projects/fis2.0/SpringBootDemo/Bootstrap/target/Bootstrap-0.0.1-SNAPSHOT.jar
Uploading file "/Users/admin/dev/ose-projects/fis2.0/SpringBootDemo/Bootstrap/target/Bootstrap-0.0.1-SNAPSHOT.jar" as binary input for the build ...
build "springdemo-bc-1" started
```

### Creating the DC, Service, External Service from a template
```
$oc process  -f bootstrap-template.yml -v APPNAME=springdemo -v EXTERNAL_HOST1=www.google.com -v ISTAG=iter1 APPOWNER=`oc whoami` | oc create -f-
service "springdemo-pgk2ierggpft-ext1-svc" created
service "springdemo-pgk2ierggpft-intsvc" created
deploymentconfig "springdemo-pgk2ierggpft" created
```
### Adding role permissions to pull config maps into the application
```
oc policy add-role-to-user view --serviceaccount=default
```

### Creating the ConfigMaps
```
oc create configmap springdemo-pgk2ierggpft --from-file=application.properties
```


### Creating Secrets
```
oc create secret generic springdemo-pgk2ierggpft --from-literal=secret.username=value1 --from-literal=secret.password=value1
```

### Mounting the secret for consumption in the application

```
oc volume dc/springdemo-pgk2ierggpft --add -t secret -m /etc/ocp/secrets --secret-name=springdemo-pgk2ierggpft
```

The mount point is defined in the Spring bootstrap.properties
```
spring.cloud.kubernetes.secrets.paths=/etc/ocp/secrets
```

E.g. three different DC, RC and SVC groups

````
NAME                      REVISION   DESIRED   CURRENT   TRIGGERED BY
springdemo-pgk2ierggpft   5          1         1         config,image(springdemo-is:iter1)

NAME                        DESIRED   CURRENT   READY     AGE
springdemo-pgk2ierggpft-1   0         0         0         59m
springdemo-pgk2ierggpft-2   0         0         0         32m
springdemo-pgk2ierggpft-3   0         0         0         27m
springdemo-pgk2ierggpft-4   0         0         0         12m
springdemo-pgk2ierggpft-5   1         1         1         9m

NAME                               CLUSTER-IP     EXTERNAL-IP      PORT(S)   AGE
springdemo-pgk2ierggpft-ext1-svc                  www.google.com   80/TCP    59m
springdemo-pgk2ierggpft-intsvc     172.30.22.65   <none>           80/TCP    59m
````

### Image Change Triggers
The BuildConfig and ImageStream resources only occur once per project. Images are used based on the tags in the images change 
triggers set in the DC. 

The Buildconfig is configure (using the ISTAG parameters) to generate images using the "built" tag.

ImageStreams need to be tagged to start a deployment  e.g.

```
oc tag springdemo-is:latest springdemo-is:iter1
oc tag springdemo-is:latest springdemo-is:test2

```


### Full resource list
```
NAME               TYPE      FROM      LATEST    LABELS
bc/springdemo-bc   Source    Binary    4         appinstance=springdemo

NAME                     TYPE      FROM      STATUS     STARTED             DURATION   LABELS
builds/springdemo-bc-1   Source    Binary    Complete   About an hour ago   9s         appinstance=springdemo,buildconfig=springdemo-bc,openshift.io/build-config.name=springdemo-bc,openshift.io/build.start-policy=Serial
builds/springdemo-bc-2   Source    Binary    Complete   37 minutes ago      8s         appinstance=springdemo,buildconfig=springdemo-bc,openshift.io/build-config.name=springdemo-bc,openshift.io/build.start-policy=Serial
builds/springdemo-bc-3   Source    Binary    Complete   32 minutes ago      8s         appinstance=springdemo,buildconfig=springdemo-bc,openshift.io/build-config.name=springdemo-bc,openshift.io/build.start-policy=Serial
builds/springdemo-bc-4   Source    Binary    Complete   16 minutes ago      8s         appinstance=springdemo,buildconfig=springdemo-bc,openshift.io/build-config.name=springdemo-bc,openshift.io/build.start-policy=Serial

NAME               DOCKER REPO                                     TAGS      UPDATED          LABELS
is/springdemo-is   172.30.197.150:5000/spring-test/springdemo-is   iter1     16 minutes ago   appinstance=springdemo

NAME                         REVISION   DESIRED   CURRENT   TRIGGERED BY                        LABELS
dc/springdemo-pgk2ierggpft   5          1         1         config,image(springdemo-is:iter1)   appinstance=springdemo-pgk2ierggpft,appowner=developer,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT

NAME                           DESIRED   CURRENT   READY     AGE       LABELS
rc/springdemo-pgk2ierggpft-1   0         0         0         1h        appinstance=springdemo-pgk2ierggpft,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-pgk2ierggpft,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT
rc/springdemo-pgk2ierggpft-2   0         0         0         37m       appinstance=springdemo-pgk2ierggpft,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-pgk2ierggpft,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT
rc/springdemo-pgk2ierggpft-3   0         0         0         32m       appinstance=springdemo-pgk2ierggpft,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-pgk2ierggpft,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT
rc/springdemo-pgk2ierggpft-4   0         0         0         16m       appinstance=springdemo-pgk2ierggpft,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-pgk2ierggpft,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT
rc/springdemo-pgk2ierggpft-5   1         1         1         14m       appinstance=springdemo-pgk2ierggpft,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-pgk2ierggpft,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT

NAME                                   CLUSTER-IP     EXTERNAL-IP      PORT(S)   AGE       LABELS
svc/springdemo-pgk2ierggpft-ext1-svc                  www.google.com   80/TCP    1h        appinstance=springdemo-pgk2ierggpft,appowner=developer,expose=true,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT
svc/springdemo-pgk2ierggpft-intsvc     172.30.22.65   <none>           80/TCP    1h        appinstance=springdemo-pgk2ierggpft,appowner=developer,expose=true,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT

NAME                                 READY     STATUS      RESTARTS   AGE       LABELS
po/springdemo-bc-1-build             0/1       Completed   0          1h        openshift.io/build.name=springdemo-bc-1
po/springdemo-bc-2-build             0/1       Completed   0          37m       openshift.io/build.name=springdemo-bc-2
po/springdemo-bc-3-build             0/1       Completed   0          32m       openshift.io/build.name=springdemo-bc-3
po/springdemo-bc-4-build             0/1       Completed   0          16m       openshift.io/build.name=springdemo-bc-4
po/springdemo-pgk2ierggpft-5-ggkne   1/1       Running     0          14m       appinstance=springdemo-pgk2ierggpft,appowner=developer,deployment=springdemo-pgk2ierggpft-5,deploymentconfig=springdemo-pgk2ierggpft,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT
```
