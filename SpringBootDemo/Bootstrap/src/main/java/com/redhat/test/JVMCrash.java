package com.redhat.test;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import org.springframework.stereotype.Component;

/** This is a deliberate attempt to crash the JVM **/

@Component
public class JVMCrash {

    public void doCrash() throws Exception{
        getUnsafe().getByte(0);
    }

    private  static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return (Unsafe) theUnsafe.get(null);
    }

}
