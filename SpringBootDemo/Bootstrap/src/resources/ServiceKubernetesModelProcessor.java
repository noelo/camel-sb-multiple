package com.ocp.resources;
import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

public class ServiceKubernetesModelProcessor {

    public void on(KubernetesListBuilder builder) {
        builder.addNewServiceItem()
	        		.withNewMetadata()
	        			.withLabels(getLabels())
	        			.withName("service-template-service")
	        		.endMetadata()
	        		.withNewSpec()
	        			.addNewPort()
	        				.withPort(new Integer(8080))
	        				.withProtocol("TCP")
	        				.withTargetPort(new IntOrString(new Integer(8080)))
	        			.endPort()
	        			.withSelector(getSelectors())
	        			.withSessionAffinity("None")
	        			.withType("ClusterIP")
	        			.withLoadBalancerIP("")
	        		.endSpec()
        		.endServiceItem()
        	.build();
    }

    private Map<String, String> getLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", "service-template");
        labels.put("group", "com.ofbizian");
        labels.put("artifact", "service-template");
        labels.put("version", "1.0.0-SNAPSHOT");
        return labels;
    }

    private Map<String, String> getSelectors() {
        Map<String, String> selectors = new HashMap<>();
        selectors.put("app", "service-template");

        return selectors;
    }
}