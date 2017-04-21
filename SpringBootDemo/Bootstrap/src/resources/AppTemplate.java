package com.ocp.resources;

import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.generator.annotation.KubernetesProvider;

public class AppTemplate {

	@KubernetesProvider("deploy.yml")
	public KubernetesList withKubernetesListBuilder() {
		
		KubernetesListBuilder builder = new KubernetesListBuilder();
		
//		builder.addNewTemplateItem()
//					.addNewParameter()
//					.withDisplayName("Image Name")
//					.withName("IMAGE_NAME")
//					.withValue("service-template")
//				.endParameter()
//					.addNewParameter()
//					.withDisplayName("Image Tag")
//					.withName("IMAGE_TAG")
//					.withValue("stage")
//				.endParameter()
//					.addNewParameter()
//					.withDisplayName("Source Namespace")
//					.withName("SOURCE_NAMESPACE")
//					.withValue("staging")
//				.endParameter()
//					.withNewMetadata()
//					.withName("service-template-prod")
//					.withAnnotations(getAnnotations())
//					.endMetadata()
//				.endTemplateItem();
		
		new DeploymentConfigKubernetesModelProcessor().on(builder);
		new ServiceKubernetesModelProcessor().on(builder);
		new RouteKubernetesModelProcessor().on(builder);
		
		return builder.build();
	}
	
	private Map<String, String> getAnnotations()
	{
		Map<String, String> annotations = new HashMap<>();
		annotations.put("description", "Example template service");
		annotations.put("iconClass", "icon-jboss");
		return annotations;
	}
}
