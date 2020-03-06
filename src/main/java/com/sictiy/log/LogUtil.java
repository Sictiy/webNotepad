package com.sictiy.log;

import java.util.function.BiConsumer;

import com.sictiy.log.LogServerImpl;
import com.sictiy.log.LogService;


/**
 * @author sictiy.xu
 * @version 2019/09/24 15:38
 **/
public class LogUtil
{
    private static class ServiceHolder
    {
        private static LogService logService = new LogServerImpl();
    }

    public static LogService getLog()
    {
        return ServiceHolder.logService;
    }

    public static void error(Object... objects)
    {
        for (Object o : objects)
        {
            log((s, ob) -> getLog().error(s, ob), "{}", objects);
        }
    }

    public static void warn(Object... objects)
    {
        for (Object o : objects)
        {
            log((s, ob) -> getLog().warn(s, ob), "{}", objects);
        }
    }

    public static void info(Object... objects)
    {
        for (Object o : objects)
        {
            log((s, ob) -> getLog().info(s, ob), "{}", objects);
        }
    }

    public static void info(String string, Object... objects)
    {
        log((s, ob) -> getLog().info(s, ob), string, objects);
    }

    public static void info(String string, Throwable throwable)
    {
        log((s, ob) -> getLog().info(s, ob), string, throwable);
    }

    public static void warn(String string, Object... objects)
    {
        log((s, ob) -> getLog().warn(s, ob), string, objects);
    }

    public static void warn(String string, Throwable throwable)
    {
        log((s, ob) -> getLog().warn(s, ob), string, throwable);
    }

    public static void error(String string, Object... objects)
    {
        log((s, ob) -> getLog().error(s, ob), string, objects);
    }

    public static void error(String string, Throwable throwable)
    {
        log((s, ob) -> getLog().error(s, ob), string, throwable);
    }

    public static void exception(Throwable throwable)
    {
        error("", throwable);
    }

    public static void log(BiConsumer<String, Object[]> biConsumer, String string, Object... objects)
    {
        biConsumer.accept(getCaller() + " " + string, objects);
    }

    public static String getCaller()
    {
        return Thread.currentThread().getStackTrace()[4].toString();
    }
}
