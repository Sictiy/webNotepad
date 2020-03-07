let tag = getTag();
let domain = window.location.host;
let wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
let isNew = true;
let hasInit = -2;

if (tag == null)
{
    tag = randomString(5);
    window.location.href = "?tag="+tag;
    exit(0);
}

let editor;
let websocket;
$(function() {
    // $.get('/md/test.md', function (md) {
    // });
    editor = editormd("test-editor", {
        width : "90%",
        height : "700",
        watch: false,
        syncScrolling : "single",
        path : "/editormd/lib/",
        onchange : function () {
            if (isNew)
            {
                setMessageLog('Saving...');
                sendMessage(this.getValue())
            }
            isNew = true;
        }
    });
});

//判断当前浏览器是否支持WebSocket

if ('WebSocket' in window) {
    websocket = new WebSocket(wsProtocol + '//' + domain + '/webNotepad/' + tag);
}
else {
    alert('Current Browser Not Support Websocket!')
}
//连接发生错误的回调方法
websocket.onerror = function () {
    setMessageLog("Connect Error!");
};

//连接成功建立的回调方法
websocket.onopen = function () {
    setMessageLog("Connect Successful!");
    hasInit++;
    init();
};

//接收到消息的回调方法
websocket.onmessage = function (event) {
    let obj = JSON.parse(event.data);
    if (obj.type === 0){
        // setMessageLog(obj.msg);
        setMessageLog('Saved!')
    }else{
        setMessageToTextArea(obj.msg);
    }
};

//连接关闭的回调方法
websocket.onclose = function () {
    setMessageLog("Connect Closed!");
};

//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
window.onbeforeunload = function () {
    closeWebSocket();
};

window.onload = function() {
    hasInit++;
    init();
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
    // let message = document.getElementById('text').value;
    let message = editor.getValue();
    websocket.send(message);
}

// 发送指定消息
function sendMessage(msg) {
    sendJson(0, msg);
}

function init() {
    if (hasInit < 0){
        return
    }
    sendJson(1, '');
}

function sendJson(type, msg) {
    let params = {};
    params['type'] = type;
    params['msg'] = msg;
    websocket.send(JSON.stringify(params))
}

function getTag() {
    let tagTemp  = window.location.pathname.substr(1);
    if (tagTemp == null || tagTemp === ''){
        return getUrlQueryString('tag');
    }
    return tagTemp;
}

function getUrlQueryString(names, urls) {
    urls = urls || window.location.href;
    urls && urls.indexOf("?") > -1 ? urls = urls
        .substring(urls.indexOf("?") + 1) : "";
    let reg = new RegExp("(^|&)" + names + "=([^&]*)(&|$)", "i");
    let r = urls ? urls.match(reg) : window.location.search.substr(1)
        .match(reg);
    if (r != null && r[2] != "")
        return unescape(r[2]);
    return null;
}

function randomString(len) {
    len = len || 32;
    let $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
    let maxPos = $chars.length;
    let pwd = '';
    for (i = 0; i < len; i++) {
        pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
    }
    return pwd;
}
