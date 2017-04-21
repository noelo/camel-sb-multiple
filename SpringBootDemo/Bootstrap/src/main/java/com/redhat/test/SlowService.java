package com.redhat.test;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SlowService extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("servlet:///slow?servletName=CamelServlet&matchOnUriPrefix=true").routeId("slowservice").streamCaching()
//                .log(" slowservice request: ${body}")
                .setHeader("DELAYHEADER").simple("${random(400,2000)}")
                .transform(simple("slowservice-${header.DELAYHEADER}"))
                .delay(simple("${header.DELAYHEADER}"));
//                .log("slowservice responding: ${body}");
    }

}