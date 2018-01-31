package com.savdev.io.string;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;

public class Collections2String {

    public static String viaJava8String(
            final Iterable<? extends CharSequence> values){
        return String.join("; ", values);
    }

    //do not work with Iterable, only with Collection:
    public static String viaApacheCommons(
            final Collection<String> values){
        return StringUtils.join(values, "; ");
    }

    public static String viaGuava(
            Iterable<? extends CharSequence> values){
        return Joiner.on("; ").skipNulls().join(values);
    }
}
