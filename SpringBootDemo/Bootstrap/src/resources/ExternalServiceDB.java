package com.ocp.resources;

import java.util.Collections;
import java.util.HashMap;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.generator.annotation.KubernetesProvider;

public class ExternalServiceDB {
	
	@KubernetesProvider("typesafe-kubernetes-dsl-service.yml.noc")
    public KubernetesList buildList() {
		
        return new KubernetesListBuilder()
        		.addNewServiceItem()
        			.withNewMetadata()
        				.addToLabels("appname", "${appname}")
        				.withName("${appname}")
        			.endMetadata()
        			.withNewSpec()
        				.addNewPort()
        					.withPort(80)
        					.withNewTargetPort(8080)
        				.endPort()
	        			.withSessionAffinity("None")
	        			.withType("ClusterIP")
	        			.withLoadBalancerIP("")
        				.withSelector(Collections.unmodifiableMap(new HashMap<String, String>() {
        					{
        						put("appname", "${appname}");
        						put("expose", "true");
        					}})) 
        			.endSpec()
        		.endServiceItem()
        		.build();
    }

}
