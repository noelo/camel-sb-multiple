package com.redhat.test;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by admin on 4/4/17.
 */
public class ExternalServiceProcessor implements Processor {

    private InitialDirContext idc;
    private static final String MX_ATTRIB = "MX";
    private static final String ADDR_ATTRIB = "A";
    private static final String CN_ATTRIB = "C";
    private static String[] MX_ATTRIBS = {MX_ATTRIB};
    private static String[] ADDR_ATTRIBS = {ADDR_ATTRIB};
    private static String[] CN_ATTRIBS = {CN_ATTRIB};

    public ExternalServiceProcessor() {

        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

        try {
            idc = new InitialDirContext(env);
            Hashtable<?,?> hm = idc.getEnvironment();
            Set<?> keys = hm.keySet();
            for(Object  key: keys){
                System.out.println("Value of "+key+" is: "+hm.get(key));
            }

//            Attributes attributes = idc.getAttributes("springdemo-byaa1n-ext1-svc");
            Attributes attributes = idc.getAttributes("springdemo-byaa1n-ext1-svc");
            NamingEnumeration attributeEnumeration = attributes.getAll();
            System.out.println("-- DNS INFORMATION --");
            while (attributeEnumeration.hasMore())
            {
                System.out.println("" + attributeEnumeration.next());
            }
            idc.close();



//            List<String> servers = new ArrayList<String>();
//
//
//            Attributes attrs = idc.getAttributes("www.google.com", CN_ATTRIBS);
//            System.out.println("DNS lookup====>");
//
//            Attribute attr = attrs.get(CN_ATTRIB);

//            System.out.println("DNS==>"+attr.get());

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    @Override
    public void process(Exchange exchange) throws Exception {
        List<String> servers = new ArrayList<String>();


        Attributes attrs = idc.getAttributes("www.google.com", MX_ATTRIBS);

        Attribute attr = attrs.get(MX_ATTRIB);

        System.out.println("DNS==>"+attr.get());

//        System.out.println("Performing lookup");
//        InetAddress inetAddress[] = InetAddress.getAllByName("springdemo-byaa1n-ext1-svc");
//
//        for (InetAddress x : inetAddress) {
//            System.out.println("Found " + x.getCanonicalHostName() + "::::" + x.getHostAddress());
//        }

    }
}
