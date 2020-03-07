package com.sictiy.webNotepad;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.alibaba.fastjson.JSONObject;
import com.sictiy.log.LogUtil;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
@ServerEndpoint("/webNotepad/{tag}")
public class NotepadSocket
{
    private static Map<String, Set<NotepadSocket>> groupSockets = new ConcurrentHashMap<>();

    private static Map<String, StringBuilder> groupString = new ConcurrentHashMap<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //当前客户端标签
    private String tag;

    private static final int SELF = 0;

    private static final int OTHERS = 1;

    private static String MD_PATH = null;

    private static String getMdPath()
    {
        if (MD_PATH == null)
        {
            MD_PATH = getWebPath() + "/md/";
        }
        return MD_PATH;
    }

    public static String getWebPath()
    {
        String resource = NotepadSocket.class.getResource("/").getPath();
        int lastFirst;
        for (int i = 0; i < 3; i++)
        {
            lastFirst = resource.lastIndexOf('/');
            resource = resource.substring(0, lastFirst);
        }
        return resource;
    }

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(@PathParam("tag") String tag, Session session)
    {
        this.session = session;
        this.tag = tag;
        var socketTestSet = groupSockets.computeIfAbsent(tag, k -> new CopyOnWriteArraySet<>());
        socketTestSet.add(this);     //加入set中
        LogUtil.info("new connect, tag:" + tag + ", current online:" + getOnlineCount(tag));
    }

    private void checkMarkDownFile()
    {
        LogUtil.info(tag + " req markdown!");
        String content;
        if (groupString.containsKey(tag))
        {
            content = groupString.get(tag).toString();
        }
        else
        {
            content = getFileString(getMdPath() + tag);
            groupString.put(tag, new StringBuilder(content));
        }
        if (!content.isEmpty())
        {
            try
            {
                sendMessage(content, OTHERS);
            }
            catch (IOException e)
            {
                LogUtil.exception(e);
            }
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose()
    {
        var socketSet = groupSockets.get(tag);
        if (socketSet != null)
        {
            socketSet.remove(this);  //从set中删除
            LogUtil.info("a closed, current online:" + getOnlineCount(tag));
        }
        else
        {
            LogUtil.info("tag do not exist! tag:" + tag);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session)
    {
        JSONObject jsonObject = JSONObject.parseObject(message);
        int type = (int) jsonObject.get("type");
        if (type == 0)
        {
            onRecvMsg((String) jsonObject.get("msg"), session);
        }
        else
        {
            checkMarkDownFile();
        }
    }

    private void onRecvMsg(String message, Session session)
    {
        LogUtil.info("message from client:" + message);
        //群发消息
        var webSocketSet = groupSockets.getOrDefault(tag, new HashSet<>());
        for (NotepadSocket item : webSocketSet)
        {
            try
            {
                if (item == this)
                {
                    item.sendMessage(message, SELF);
                }
                else
                {
                    item.sendMessage(message, OTHERS);
                }
            }
            catch (IOException e)
            {
                LogUtil.exception(e);
            }
        }
        // 保存消息
        if (groupString.containsKey(tag))
        {
            // 假设是追加
            //            groupString.get(tag).append(message);
            groupString.put(tag, new StringBuilder(message));
        }
        else
        {
            groupString.put(tag, new StringBuilder(message));
        }
        if(!writeFileString(getMdPath() + tag, message, false))
        {
            LogUtil.error("write to file failed! tag:{}, message:{}, path:{}", tag, message, getMdPath());
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error)
    {
        LogUtil.error("catch error", error);
    }

    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message, int type) throws IOException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", message);
        jsonObject.put("type", type);
        this.session.getBasicRemote().sendText(jsonObject.toJSONString());
        //this.session.getAsyncRemote().sendText(message);
    }

    public static synchronized int getOnlineCount(String tag)
    {
        if (groupSockets.containsKey(tag))
        {
            return groupSockets.get(tag).size();
        }
        return 0;
    }

    /**
     * @param fileName fileName
     * @param content  content
     * @param isAppend 是否追加
     * @return boolean
     **/
    public static boolean writeFileString(String fileName, String content, boolean isAppend)
    {
        try
        {
            File file = new File(fileName);
            if (!file.getParentFile().exists())
            {
                if(!file.getParentFile().mkdirs())
                {
                    return false;
                }
            }
            RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
            accessFile.seek(isAppend ? file.length() : 0);
            accessFile.write(content.getBytes(StandardCharsets.UTF_8));
            accessFile.close();
            LogUtil.info("write to file: " + fileName);
            return true;
        }
        catch (IOException e)
        {
            LogUtil.exception(e);
        }
        return false;
    }

    public static String getFileString(String fileName)
    {
        try
        {
            File file = new File(fileName);
            if (!file.exists())
            {
                // 文件不存在
                return "";
            }
            long fileLong = file.length();
            byte[] fileContent = new byte[(int) fileLong];
            FileInputStream inputStream = new FileInputStream(file);
            if (inputStream.read(fileContent) <= 0)
            {
                return "";
            }
            inputStream.close();
            return new String(fileContent, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            LogUtil.exception(e);
        }
        return "";
    }
}
