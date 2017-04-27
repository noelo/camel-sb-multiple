
### Template Generation
The templates are composed of resource (DC, SVC) fragments in the src/main/fabric8 directory.

The BuildConfig & ImageStream templates reside in the src/main/resources/static directory

### Application & Resource Naming
As part of the resource generation using template the template will automatically name resources based on a combination of the the *APPNAME* and a random generated string.

This ***name*** is used in all resources as well as the **spring.application.name** property which is used in the retrieval of *ConfigMaps* and *Secrets* .
***Name*** can be a string of 12 characters consisting of (a-z, and 0-9)

See https://github.com/kubernetes/community/blob/master/contributors/design-proposals/identifiers.md for limitations on names

In the examples below all resource use the name **springdemo-pgk2ierggpft**


### Creating the Buildconfig and ImageStream from a template

```
oc process -p APPNAME=springdemo -p ISTAG=iter1 -p APPOWNER=`oc whoami`  -p SERVICEVERSION="1-10" -p UNIQUE_ID="abc" -f ./Bootstrap/src/main/resources/static/build-config.yml |oc create -f-

buildconfig "springdemo-1-10-abc" created
imagestream "springdemo-1-10-abc" created

$oc start-build bc/springdemo-1-10-abc --from-file=./Bootstrap/target/Bootstrap-0.0.5-SNAPSHOT.jar
 Uploading file "Bootstrap/target/Bootstrap-0.0.5-SNAPSHOT.jar" as binary input for the build ...
build "springdemo-1-10-abc-1" started
```

### Creating the DC, Service, External Service from a template
```
oc process  -f ./Bootstrap/target/classes/META-INF/fabric8/openshift/bootstrap-template.yml -p APPNAME=springdemo -p SERVICEVERSION="1-10" -p ISTAG=iter1 UNIQUE_ID=abc APPOWNER=`oc whoami` | oc create -f-
service "springdemo-v1-10-abc" created
deploymentconfig "springdemo-1-10-abc" created
route "springdemo-v1-10-abc" created
```
### Adding role permissions to pull config maps into the application
```
oc policy add-role-to-user view --serviceaccount=default
```

#### To get the generated application name you can use the following
```
oc get dc --template='{{range .items}}{{.metadata.name}}{{end}}'

APPNAME=`oc get dc --template='{{range .items}}{{.metadata.name}}{{end}}'`
```

### Creating the ConfigMaps
```
oc create configmap ${APPNAME} --from-file=./Bootstrap/application.properties
```


### Creating Secrets
```
oc create secret generic ${APPNAME} --from-literal=secret.username=value1 --from-literal=secret.password=value1
```

### Mounting the secret for consumption in the application

```
oc volume dc/${APPNAME} --add -t secret -m /etc/ocp/secrets --secret-name=${APPNAME} --name=${APPNAME}-secret
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
oc get all --show-labels=true
NAME                     TYPE      FROM      LATEST    LABELS
bc/springdemo-1-10-abc   Source    Binary    6         appinstance=springdemo-1-10-abc

NAME                           TYPE      FROM      STATUS     STARTED       DURATION   LABELS
builds/springdemo-1-10-abc-1   Source    Binary    Complete   4 hours ago   14s        appinstance=springdemo-1-10-abc,buildconfig=springdemo-1-10-abc,openshift.io/build-config.name=springdemo-1-10-abc,openshift.io/build.start-policy=Serial
builds/springdemo-1-10-abc-2   Source    Binary    Complete   4 hours ago   14s        appinstance=springdemo-1-10-abc,buildconfig=springdemo-1-10-abc,openshift.io/build-config.name=springdemo-1-10-abc,openshift.io/build.start-policy=Serial
builds/springdemo-1-10-abc-3   Source    Binary    Complete   4 hours ago   14s        appinstance=springdemo-1-10-abc,buildconfig=springdemo-1-10-abc,openshift.io/build-config.name=springdemo-1-10-abc,openshift.io/build.start-policy=Serial
builds/springdemo-1-10-abc-4   Source    Binary    Complete   4 hours ago   16s        appinstance=springdemo-1-10-abc,buildconfig=springdemo-1-10-abc,openshift.io/build-config.name=springdemo-1-10-abc,openshift.io/build.start-policy=Serial
builds/springdemo-1-10-abc-5   Source    Binary    Complete   2 hours ago   14s        appinstance=springdemo-1-10-abc,buildconfig=springdemo-1-10-abc,openshift.io/build-config.name=springdemo-1-10-abc,openshift.io/build.start-policy=Serial
builds/springdemo-1-10-abc-6   Source    Binary    Complete   2 hours ago   16s        appinstance=springdemo-1-10-abc,buildconfig=springdemo-1-10-abc,openshift.io/build-config.name=springdemo-1-10-abc,openshift.io/build.start-policy=Serial

NAME                 DOCKER REPO                              TAGS                UPDATED       LABELS
is/springdemo-1-10   172.30.1.1:5000/spring/springdemo-1-10   iter3,iter1,iter2   2 hours ago   appinstance=springdemo-1-10-abc

NAME                     REVISION   DESIRED   CURRENT   TRIGGERED BY                          LABELS
dc/springdemo-1-10-abc   7          2         2         config,image(springdemo-1-10:iter3)   appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.4-SNAPSHOT

NAME                       DESIRED   CURRENT   READY     AGE       LABELS
rc/springdemo-1-10-abc-1   0         0         0         4h        appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-1-10-abc,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT
rc/springdemo-1-10-abc-2   0         0         0         4h        appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-1-10-abc,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT
rc/springdemo-1-10-abc-3   0         0         0         4h        appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-1-10-abc,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT
rc/springdemo-1-10-abc-4   0         0         0         3h        appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-1-10-abc,project=Bootstrap,provider=fabric8,version=0.0.1-SNAPSHOT
rc/springdemo-1-10-abc-5   0         0         0         3h        appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-1-10-abc,project=Bootstrap,provider=fabric8,version=0.0.2-SNAPSHOT
rc/springdemo-1-10-abc-6   0         0         0         1h        appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-1-10-abc,project=Bootstrap,provider=fabric8,version=0.0.2-SNAPSHOT
rc/springdemo-1-10-abc-7   2         2         2         1h        appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-1-10-abc,project=Bootstrap,provider=fabric8,version=0.0.4-SNAPSHOT

NAME                          HOST/PORT                                           PATH      SERVICES               PORT      TERMINATION   WILDCARD   LABELS
routes/springdemo-v1-10-abc   springdemo-v1-10-abc-spring.192.168.106.35.xip.io             springdemo-v1-10-abc   8080                    None       appinstance=springdemo-abc,appowner=developer,expose=true,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.4-SNAPSHOT

NAME                       CLUSTER-IP      EXTERNAL-IP   PORT(S)   AGE       LABELS
svc/springdemo-v1-10-abc   172.30.41.170   <none>        80/TCP    4h        appinstance=springdemo-abc,appname=springdemo,appowner=developer,custom-app-uid=abc,expose=true,group=com.redhat.demo,interface-version=1.1,project=Bootstrap,provider=fabric8,version=0.0.4-SNAPSHOT

NAME                             READY     STATUS      RESTARTS   AGE       LABELS
po/springdemo-1-10-abc-1-build   0/1       Completed   0          4h        openshift.io/build.name=springdemo-1-10-abc-1
po/springdemo-1-10-abc-2-build   0/1       Completed   0          4h        openshift.io/build.name=springdemo-1-10-abc-2
po/springdemo-1-10-abc-3-build   0/1       Completed   0          3h        openshift.io/build.name=springdemo-1-10-abc-3
po/springdemo-1-10-abc-4-build   0/1       Completed   0          3h        openshift.io/build.name=springdemo-1-10-abc-4
po/springdemo-1-10-abc-5-build   0/1       Completed   0          1h        openshift.io/build.name=springdemo-1-10-abc-5
po/springdemo-1-10-abc-6-build   0/1       Completed   0          1h        openshift.io/build.name=springdemo-1-10-abc-6
po/springdemo-1-10-abc-7-0w19w   1/1       Running     0          1h        appinstance=springdemo-abc,appowner=developer,deployment=springdemo-1-10-abc-7,deploymentconfig=springdemo-1-10-abc,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.4-SNAPSHOT
po/springdemo-1-10-abc-7-28660   1/1       Running     0          1h        appinstance=springdemo-abc,appowner=developer,deployment=springdemo-1-10-abc-7,deploymentconfig=springdemo-1-10-abc,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.4-SNAPSHOT
```
