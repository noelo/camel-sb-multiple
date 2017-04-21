package com.ocp.resources;
import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.openshift.api.model.TemplateBuilder;

public class RouteKubernetesModelProcessor {

    public void on(KubernetesListBuilder builder) {
        builder.addNewRouteItem()
                .withNewMetadata()
                    .withName("service-template-route")
                    .withLabels(getLabels())
                .endMetadata()
                .withNewSpec()
                    .withNewTo()
                        .withKind("Service")
                        .withName("service-template-service")
                    .endTo()
                .endSpec()
            .endRouteItem()
            .build();
    }

    private Map<String, String> getLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", "service-template");
        labels.put("group", "com.ofbizian");
        labels.put("artifact", "service-template");
        labels.put("version", "1.0-SNAPSHOT");
        return labels;
    }
}