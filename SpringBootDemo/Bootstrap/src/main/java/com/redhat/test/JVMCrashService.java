package com.redhat.test;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class JVMCrashService extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("servlet:///crash?servletName=CamelServlet&matchOnUriPrefix=true").routeId("crashservice").streamCaching()
                .log("Deliberately crashing the JVM")
                .bean(new JVMCrash(), "doCrash")
                .log("Deliberately crashing the JVM.....we shouldn't get here")
                .transform().constant("crash response")
                .log("Deliberately crashing the JVM.....end route but this shouldn't happen");
    }
}