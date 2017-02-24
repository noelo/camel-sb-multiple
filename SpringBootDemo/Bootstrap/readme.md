
### Template Generation
The templates are composed of resource (DC, SVC) fragments in the src/main/fabric8 directory.

The BuildConfig & ImageStream templates reside in the src/main/resources/static directory


### Creating the Buildconfig and ImageStream from a template

```
oc process -v APPNAME=try1 -v ISTAG=built -f build-config.yml | oc create -f-
oc start-build bc/try1-bc --from-file=/Users/admin/dev/ose-projects/fis2.0/SpringBootDemo/Bootstrap/target/Bootstrap-0.0.1-SNAPSHOT.jar
```


### Creating the DC, Service, External Service from a template
```
oc process  -f bootstrap-template.yml -v APPNAME=noctest -v EXTERNAL_HOST1=www.google.com -v ISTAG=test3|oc create -f-
```

The template will inject an auto-generated random string which can be used to identify different resource groups in the
same namespace/project

E.g. three different DC, RC and SVC groups

````
NAME                      REVISION   DESIRED   CURRENT   TRIGGERED BY
dc/noctest-2e68r53ctq66   1          1         1         config,image(noctest-is:test1)
dc/noctest-6h004etp1n7n   1          1         1         config,image(noctest-is:test2)
dc/noctest-r0xugbbx2i20   1          1         1         config,image(noctest-is:test3)

NAME                        DESIRED   CURRENT   READY     AGE
rc/noctest-2e68r53ctq66-1   1         1         1         57m
rc/noctest-6h004etp1n7n-1   1         1         1         56m
rc/noctest-r0xugbbx2i20-1   1         1         1         51m

NAME                                CLUSTER-IP       EXTERNAL-IP      PORT(S)   AGE
svc/noctest-2e68r53ctq66-ext1-svc                    www.google.com   80/TCP    1h
svc/noctest-2e68r53ctq66-intsvc     172.30.125.50    <none>           80/TCP    1h
svc/noctest-6h004etp1n7n-ext1-svc                    www.google.com   80/TCP    1h
svc/noctest-6h004etp1n7n-intsvc     172.30.252.243   <none>           80/TCP    1h
svc/noctest-r0xugbbx2i20-ext1-svc                    www.google.com   80/TCP    51m
svc/noctest-r0xugbbx2i20-intsvc     172.30.92.236    <none>           80/TCP    51m
````

Image Change Triggers
The BuildConfig and ImageStream only occur once per project. Images are used based on the tags in the images change 
triggers set in the DC. 

In the example above the three DC only deploy images when the tag is one of test1/test2/test3.

The Buildconfig is configure (using the ISTAG parameters) to generate images using the "built" tag.

ImageStreams need to be tagged to start a deployment  e.g.

```
oc tag noctest-is:latest noctest-is:test1
oc tag noctest-is:latest noctest-is:test2

```
