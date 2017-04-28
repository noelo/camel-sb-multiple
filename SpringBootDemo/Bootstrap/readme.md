
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
$oc process -p APPNAME=springdemo -p ISTAG=iter1 -p APPOWNER=`oc whoami`  -p SERVICEVERSION="1-10" -p UNIQUE_ID="abc" -f ./Bootstrap/src/main/resources/static/build-config.yml |oc create -f-

buildconfig "springdemo-1-10-abc" created
imagestream "springdemo-1-10-abc" created

$oc start-build bc/springdemo-1-10-abc --from-file=./Bootstrap/target/Bootstrap-0.0.5-SNAPSHOT.jar
 Uploading file "Bootstrap/target/Bootstrap-0.0.5-SNAPSHOT.jar" as binary input for the build ...
build "springdemo-1-10-abc-1" started
```

### Creating the DC, Service, External Service from a template
```
$oc process  -f ./Bootstrap/target/classes/META-INF/fabric8/openshift/bootstrap-template.yml -p APPNAME=springdemo -p SERVICEVERSION="1-10" -p ISTAG=iter1 UNIQUE_ID=abc APPOWNER=`oc whoami` | oc create -f-
service "springdemo-v1-10-abc" created
deploymentconfig "springdemo-1-10-abc" created
route "springdemo-v1-10-abc" created
```
### Adding role permissions to pull config maps into the application
```
$oc policy add-role-to-user view --serviceaccount=default
```

#### To get the generated application name you can use the following
```
$oc get dc --template='{{range .items}}{{.metadata.name}}{{end}}'

APPNAME=`oc get dc --template='{{range .items}}{{.metadata.name}}{{end}}'`
```

### Creating the ConfigMaps
```
$oc create configmap ${APPNAME} --from-file=./Bootstrap/application.properties
```


### Creating Secrets
```
$oc create secret generic ${APPNAME} --from-literal=secret.username=value1 --from-literal=secret.password=value1
```

### Mounting the secret for consumption in the application

```
$oc volume dc/${APPNAME} --add -t secret -m /etc/ocp/secrets --secret-name=${APPNAME} --name=${APPNAME}-secret
```

The mount point is defined in the Spring bootstrap.properties
```
spring.cloud.kubernetes.secrets.paths=/etc/ocp/secrets
```


ImageStreams need to be tagged to start a deployment  e.g.

```
$oc tag springdemo-is:latest springdemo-is:iter1
$oc tag springdemo-is:latest springdemo-is:test2

```

### Scaling 
```
$oc scale dc/springdemo-1-10-abc --replicas=2
```

### Full resource list
```
oc get all --show-labels=true
NAME                     TYPE      FROM      LATEST    LABELS
bc/springdemo-1-10-abc   Source    Binary    1         appinstance=springdemo-1-10-abc

NAME                           TYPE      FROM      STATUS     STARTED          DURATION   LABELS
builds/springdemo-1-10-abc-1   Source    Binary    Complete   10 minutes ago   1m15s      appinstance=springdemo-1-10-abc,buildconfig=springdemo-1-10-abc,openshift.io/build-config.name=springdemo-1-10-abc,openshift.io/build.start-policy=Serial

NAME                     DOCKER REPO                                  TAGS      UPDATED         LABELS
is/springdemo-1-10-abc   172.30.1.1:5000/spring/springdemo-1-10-abc   iter1     9 minutes ago   appinstance=springdemo-1-10-abc

NAME                     REVISION   DESIRED   CURRENT   TRIGGERED BY                              LABELS
dc/springdemo-1-10-abc   2          2         2         config,image(springdemo-1-10-abc:iter1)   appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.5-SNAPSHOT

NAME                       DESIRED   CURRENT   READY     AGE       LABELS
rc/springdemo-1-10-abc-1   0         0         0         8m        appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-1-10-abc,project=Bootstrap,provider=fabric8,version=0.0.5-SNAPSHOT
rc/springdemo-1-10-abc-2   2         2         2         8m        appinstance=springdemo-1-10-abc,appowner=developer,group=com.redhat.demo,openshift.io/deployment-config.name=springdemo-1-10-abc,project=Bootstrap,provider=fabric8,version=0.0.5-SNAPSHOT

NAME                          HOST/PORT                                         PATH      SERVICES               PORT      TERMINATION   WILDCARD   LABELS
routes/springdemo-v1-10-abc   springdemo-v1-10-abc-spring.192.168.64.3.xip.io             springdemo-v1-10-abc   8080                    None       appinstance=springdemo-abc,appowner=developer,expose=true,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.5-SNAPSHOT

NAME                       CLUSTER-IP      EXTERNAL-IP   PORT(S)   AGE       LABELS
svc/springdemo-v1-10-abc   172.30.41.192   <none>        80/TCP    8m        appinstance=springdemo-abc,appname=springdemo,appowner=developer,custom-app-uid=abc,expose=true,group=com.redhat.demo,interface-version=1.1,project=Bootstrap,provider=fabric8,version=0.0.5-SNAPSHOT

NAME                             READY     STATUS      RESTARTS   AGE       LABELS
po/springdemo-1-10-abc-1-build   0/1       Completed   0          10m       openshift.io/build.name=springdemo-1-10-abc-1
po/springdemo-1-10-abc-2-6s6h2   1/1       Running     0          5m        appinstance=springdemo-abc,appowner=developer,deployment=springdemo-1-10-abc-2,deploymentconfig=springdemo-1-10-abc,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.5-SNAPSHOT
po/springdemo-1-10-abc-2-7tik5   1/1       Running     0          8m        appinstance=springdemo-abc,appowner=developer,deployment=springdemo-1-10-abc-2,deploymentconfig=springdemo-1-10-abc,group=com.redhat.demo,project=Bootstrap,provider=fabric8,version=0.0.5-SNAPSHOT
```


### Request Testing
```
for ((i=1;i<=100000;i++)); do sleep 0.5&&curl http://springdemo-v1-10-abc-spring.192.168.64.3.xip.io/info ;date; done
```


