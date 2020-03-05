<%@ page language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<link rel="stylesheet" href="/editormd/css/editormd.css" />
<link rel="stylesheet" href="/editormd/examples/css/style.css" />
<head>
    <title>Java后端WebSocket的Tomcat实现</title>
</head>
<body>
    <div id="layout">
        <header>
            <h1>Whlcome</h1>
            <button onclick="send()">发送消息</button>
            <button onclick="closeWebSocket()">关闭WebSocket连接</button>
        </header>
        <div id="test-editor"></div>
        <p id="output"></p>
    </div>
</body>

<script src="https://cdn.bootcss.com/jquery/1.11.3/jquery.min.js"></script>
<script src="/editormd/editormd.min.js"></script>
<script type="text/javascript">
    var username = getUrlQueryString('tag');
    let isNew = true;
    if (username == null)
    {
        username = randomString(5);
        window.location.href = "?tag="+username;
        exit(0);
    }

    var editor;
    var websocket;
    $(function() {
        $.get('/md/test.md', function (md) {
        });
        editor = editormd("test-editor", {
            width : "90%",
            height : "700",
            // markdown : md,
            syncScrolling : "single",
            path : "/editormd/lib/",
            onchange : function () {
                if (isNew)
                {
                    sendMessage(this.getValue())
                }
                isNew = true;
            }
        });
    });

    //判断当前浏览器是否支持WebSocket

    if ('WebSocket' in window) {
        websocket = new WebSocket('ws://localhost:8080/websocket/' + username);
    }
    else {
        alert('当前浏览器 Not support websocket')
    }
    //连接发生错误的回调方法
    websocket.onerror = function () {
        setMessageLog("WebSocket连接发生错误");
    };

    //连接成功建立的回调方法
    websocket.onopen = function () {
        setMessageLog("WebSocket连接成功");
    };

    //接收到消息的回调方法
    websocket.onmessage = function (event) {
        var obj = JSON.parse(event.data);
        if (obj.type === 0){
            setMessageLog(obj.msg);
        }else{
            setMessageToTextArea(obj.msg);
        }
    };

    //连接关闭的回调方法
    websocket.onclose = function () {
        setMessageLog("WebSocket连接关闭");
    };

    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function () {
        closeWebSocket();
    };

    window.onload = function() {
        reqDoc();
    };

    //将消息显示在网页上
    function setMessageLog(message) {
        $("#output").html(message);
        // document.getElementById('message').innerHTML += innerHTML + '<br/>';
    }

    function setMessageToTextArea(message) {
        // $("#editormd").html(message);
        isNew = false;
        editor.setMarkdown(message);
    }

    //关闭WebSocket连接
    function closeWebSocket() {
        websocket.close();
    }

    //发送消息
    function send() {
        // var message = document.getElementById('text').value;
        var message = editor.getValue();
        websocket.send(message);
    }

    // 发送指定消息
    function sendMessage(msg) {
        sendJson(0, msg);
    }

    function reqDoc() {
        sendJson(1, '');
    }

    function sendJson(type, msg) {
        var params = {};
        params['type'] = type;
        params['msg'] = msg;
        websocket.send(JSON.stringify(params))
    }

    function getUrlQueryString(names, urls) {
        urls = urls || window.location.href;
        urls && urls.indexOf("?") > -1 ? urls = urls
            .substring(urls.indexOf("?") + 1) : "";
        var reg = new RegExp("(^|&)" + names + "=([^&]*)(&|$)", "i");
        var r = urls ? urls.match(reg) : window.location.search.substr(1)
            .match(reg);
        if (r != null && r[2] != "")
            return unescape(r[2]);
        return null;
    }

    function randomString(len) {
        len = len || 32;
        var $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
        var maxPos = $chars.length;
        var pwd = '';
        for (i = 0; i < len; i++) {
            pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
        }
        return pwd;
    }
</script>
</html>
