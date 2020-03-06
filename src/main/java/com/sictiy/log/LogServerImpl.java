package com.sictiy.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sictiy.xu
 * @version 2019/12/24 14:47
 **/
public class LogServerImpl implements LogService
{
    public static Logger log = LoggerFactory.getLogger(LogServerImpl.class);

    @Override
    public void info(String string, Throwable throwable)
    {
        log.info(string, throwable);
    }

    @Override
    public void info(String string, Object... objects)
    {
        log.info(string, objects);
    }

    @Override
    public void warn(String string, Throwable throwable)
    {
        log.warn(string, throwable);
    }

    @Override
    public void warn(String string, Object... objects)
    {
        log.warn(string, objects);
    }

    @Override
    public void error(String string, Throwable throwable)
    {
        log.error(string, throwable);
    }

    @Override
    public void error(String string, Object... objects)
    {
        log.error(string, objects);
    }
}
