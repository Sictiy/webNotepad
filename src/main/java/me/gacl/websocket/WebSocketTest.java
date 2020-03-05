package me.gacl.websocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import com.alibaba.fastjson.JSONObject;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
@ServerEndpoint("/websocket/{tag}")
public class WebSocketTest {
	private static Map<String, Set<WebSocketTest>> groupSockets = new ConcurrentHashMap<String, Set<WebSocketTest>>();

	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;

	//当前客户端标签
	private String tag;

	private static final int SELF = 0;

	private static final int OTHERS = 1;

	/**
	 * 连接建立成功调用的方法
	 * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
	 */
	@OnOpen
	public void onOpen(@PathParam("tag") String tag, Session session){
		this.session = session;
		this.tag = tag;
		var socketTestSet = groupSockets.computeIfAbsent(tag, k-> new CopyOnWriteArraySet<>());
		socketTestSet.add(this);     //加入set中
		System.out.println("new connect, tag:" + tag + ", current online:" + getOnlineCount(tag));
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose(){
	    var socketSet = groupSockets.get(tag);
	    if (socketSet != null)
        {
            socketSet.remove(this);  //从set中删除
            System.out.println("a closed, current online:" + getOnlineCount(tag));
        }
	    else
        {
            System.out.println("tag do not exist! tag:" + tag);
        }
	}

	/**
	 * 收到客户端消息后调用的方法
	 * @param message 客户端发送过来的消息
	 * @param session 可选的参数
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println("message from client:" + message);
		//群发消息
        var webSocketSet = groupSockets.getOrDefault(tag, new HashSet<>());
		for(WebSocketTest item: webSocketSet){
			try {
			    if (item == this)
                {
                    item.sendMessage(message, SELF);
                }
			    else
                {
                    item.sendMessage(message, OTHERS);
                }
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	/**
	 * 发生错误时调用
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error){
		System.out.println("catch error");
		error.printStackTrace();
	}

	/**
	 * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(String message, int type) throws IOException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", message);
        jsonObject.put("type", type);
		this.session.getBasicRemote().sendText(jsonObject.toJSONString());
		//this.session.getAsyncRemote().sendText(message);
	}

	public static synchronized int getOnlineCount(String  tag) {
	    if (groupSockets.containsKey(tag))
        {
            return groupSockets.get(tag).size();
        }
		return 0;
	}
}
