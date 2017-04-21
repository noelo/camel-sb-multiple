package com.ocp.resources;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.DeploymentTriggerImageChangeParams;
import io.fabric8.openshift.api.model.DeploymentTriggerPolicy;
import io.fabric8.openshift.api.model.RollingDeploymentStrategyParams;
import io.fabric8.utils.Lists;

public class DeploymentConfigKubernetesModelProcessor {

    public void on(KubernetesListBuilder builder) {
        builder.addNewDeploymentConfigItem()
			    .withNewMetadata()
			        .withName("service-template")
			        .withLabels(getLabels())
			    .endMetadata()
	        	.withNewSpec()
                    .withReplicas(1)
                    .withSelector(getSelectors())
                    .withNewStrategy()
                        .withType("Rolling")
                        .withRollingParams(getRollingDeploymentStrategyParams())
                    .endStrategy()
		             .withNewTemplate()
		             	.withNewMetadata()
		             		.withLabels(getLabels())
		             	.endMetadata()
		             	.withNewSpec()
		             		.withContainers(getContainers())
		             		.withRestartPolicy("Always")
                            .withVolumes(getVolumes())
                        .endSpec()
		             .endTemplate()
		             .withTriggers(getTriggers())
	             .endSpec()
                .endDeploymentConfigItem()
            .build();
    }

    private RollingDeploymentStrategyParams getRollingDeploymentStrategyParams() {
        RollingDeploymentStrategyParams rolling = new RollingDeploymentStrategyParams();
        rolling.setTimeoutSeconds(new Long(240));
        rolling.setMaxSurge(new IntOrString("30%"));
        rolling.setMaxUnavailable(new IntOrString("20%"));

        return rolling;
    }

    private List<DeploymentTriggerPolicy> getTriggers() {
        DeploymentTriggerPolicy configChange = new DeploymentTriggerPolicy();
        configChange.setType("ConfigChange");

        ObjectReference from = new ObjectReference();
        from.setName("service-template:latest");
        from.setKind("ImageStreamTag");

        DeploymentTriggerImageChangeParams imageChangeParms = new DeploymentTriggerImageChangeParams();
        imageChangeParms.setFrom(from);
        imageChangeParms.setAutomatic(true);

        DeploymentTriggerPolicy imageChange = new DeploymentTriggerPolicy();
        imageChange.setType("ImageChange");
        imageChange.setImageChangeParams(imageChangeParms);
        imageChangeParms.setContainerNames(Lists.newArrayList("service-template"));

        List<DeploymentTriggerPolicy> triggers = new ArrayList<DeploymentTriggerPolicy>();
        triggers.add(configChange);
        triggers.add(imageChange);

        return triggers;
    }

    private List<ContainerPort> getPorts() {
        List<ContainerPort> ports = new ArrayList<ContainerPort>();

        ContainerPort cxf = new ContainerPort();
        cxf.setContainerPort(8080);
        cxf.setProtocol("TCP");
        cxf.setName("http");

        ContainerPort jolokia = new ContainerPort();
        jolokia.setContainerPort(8778);
        jolokia.setProtocol("TCP");
        jolokia.setName("jolokia");

        ports.add(cxf);
        ports.add(jolokia);

        return ports;
    }

    private Container getContainers() {
        String livenessProbe= "(curl -f 127.0.0.1:8080) >/dev/null 2>&1; test $? != 7";
        String readinessProbe = "(curl -f 127.0.0.1:8080) >/dev/null 2>&1; test $? != 7";

        Container container = new Container();
        container.setImage("service-template");
        container.setImagePullPolicy("Always");
        container.setName("service-template");
        container.setPorts(getPorts());
        container.setLivenessProbe(getProbe(livenessProbe, new Integer(30), new Integer(60)));
        container.setReadinessProbe(getProbe(readinessProbe, new Integer(30), new Integer(1)));
        container.setResources(getResourceRequirements());
        container.setVolumeMounts(getVolumeMounts());

        return container;
    }

    private Map<String, String> getSelectors() {
        Map<String, String> selectors = new HashMap<>();
        selectors.put("app", "service-template");
        selectors.put("deploymentconfig", "service-template");

        return selectors;
    }

    private Probe getProbe(String curl, Integer initialDelaySeconds, Integer timeoutSeconds) {
        List<String> commands = new ArrayList<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add(curl);

        ExecAction execAction = new ExecAction();
        execAction.setCommand(commands);

        Probe probe = new Probe();
        probe.setInitialDelaySeconds(initialDelaySeconds);
        probe.setTimeoutSeconds(timeoutSeconds);
        probe.setExec(execAction);

        return probe;
    }

    private ResourceRequirements getResourceRequirements() {
        ResourceRequirements resourceRequirements = new ResourceRequirements();
        resourceRequirements.setRequests(getRequests());
        resourceRequirements.setLimits(getLimits());

        return resourceRequirements;
    }

    private Map<String, Quantity> getRequests() {
        Map<String, Quantity> limits = new HashMap<String, Quantity>();
        limits.put("memory", new Quantity("512Mi"));

        return limits;
    }

    private Map<String, Quantity> getLimits() {
        Map<String, Quantity> limits = new HashMap<String, Quantity>();
        limits.put("memory", new Quantity("1024Mi"));

        return limits;
    }

    private Map<String, String> getLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", "service-template");
        labels.put("deploymentconfig", "service-template");
        labels.put("artifact", "service-template");
        labels.put("version", "1.0-SNAPSHOT");
        labels.put("group", "com.ofbizian");

        return labels;
    }

    public List<VolumeMount> getVolumeMounts() {
        List<VolumeMount> volumeMounts = new ArrayList<>();
        final VolumeMount configMapVolumeMount = new VolumeMount();
        configMapVolumeMount.setReadOnly(true);
        configMapVolumeMount.setName("service-template-configmap");
        configMapVolumeMount.setMountPath("/etc/configmap");
        volumeMounts.add(configMapVolumeMount);

        final VolumeMount secretVolumeMount = new VolumeMount();
        secretVolumeMount.setReadOnly(true);
        secretVolumeMount.setName("service-template-secret");
        secretVolumeMount.setMountPath("/etc/secret");
        volumeMounts.add(secretVolumeMount);
        return volumeMounts;
    }

    public List<Volume> getVolumes() {
        List<Volume> volumes = new ArrayList<>();
        final Volume configMapVolume = new Volume();
        configMapVolume.setName("service-template-configmap");
        final ConfigMapVolumeSource configMap = new ConfigMapVolumeSource();
        configMap.setName("service-template");
        configMapVolume.setConfigMap(configMap);
        volumes.add(configMapVolume);

        final Volume secretVolume = new Volume();
        secretVolume.setName("service-template-secret");
        final SecretVolumeSource secretVolumeSource = new SecretVolumeSource();
        secretVolumeSource.setSecretName("service-template");
        secretVolume.setSecret(secretVolumeSource);
        volumes.add(secretVolume);
        return volumes;
    }

}