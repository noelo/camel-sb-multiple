package com.redhat.test;

/**
 * Created by admin on 17/3/17.
 */


import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class IPRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("servlet:///java?servletName=CamelServlet&matchOnUriPrefix=true").routeId("javaroute").streamCaching().tracing()
                .log("Incoming request")
                .to("direct:getip")
                .log("Outgoing request ==> ${body} ${headers}");

        from("direct:getip").tracing().routeId("slowserviceRoute").streamCaching().id("slowservicecaller")
                .hystrix()
                    .hystrixConfiguration().executionTimeoutInMilliseconds(2000).corePoolSize(20).end()
                    .id("slowservicecaller")
                    .log("Hystrix processing start: ${threadName}")
//                    .to("http4://127.0.0.1:8080/camel/slowservice?bridgeEndpoint=true&amp;&connectionClose=true")
                    .to("direct:slow")
                    .log("Hystrix processing end: ${threadName}")
                .onFallback()
                    .log("Hystrix fallback start: ${threadName}")
                    .transform().constant("Downstream Unavailable")
                    .setHeader("FAILBACKPROCESSED").simple("TRUE")
                    .log("Hystrix fallback end: ${threadName}")
                .end()
                .log("Request response: ${body} ${headers}");

        from("direct:fast").tracing()
                // this is a fast route and takes 1 second to respond
                .log("Fast processing start: ${threadName}")
                .delay(1000)
                .transform().constant("Fast response")
                .log("Fast processing end: ${threadName}");

        from("direct:slow").tracing()
                // this is a slow route and takes 3 second to respond
                .log("Slow processing start: ${threadName}")
                .delay(3000)
                .transform().constant("Slow response")
                .log("Slow processing end: ${threadName}");



    }
}

