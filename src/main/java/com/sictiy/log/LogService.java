package com.sictiy.log;

/**
 * 程序日志服务
 *
 * @author sictiy.xu
 * @version 2019/12/24 14:44
 **/
public interface LogService
{
    void info(String string, Throwable throwable);

    void info(String string, Object... objects);

    void warn(String string, Throwable throwable);

    void warn(String string, Object... objects);

    void error(String string, Throwable throwable);

    void error(String string, Object... objects);
}
