/**
 * WebSocket连接js文件
 * @type {null}
 */

var ws = null;

//获取当前项目的名称
/*function getProjectPath() {
    //获取主机地址之后的目录，如： cloudlibrary/admin/books.jsp
    var pathName = window.document.location.pathname;
    //获取带"/"的项目名，如：/cloudlibrary
    var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
    return projectName;
}*/

// 创建WebSocket连接
function createWebSocketClient(uniqueUserCode) {

    // console.log(uniqueUserCode)
    if (uniqueUserCode) {
        // 判断当前浏览器是否支持WebSocket
        if ('WebSocket' in window) {
            ws = new WebSocket('ws://127.0.0.1:8080' + getProjectPath() + '/api/websocket/' + uniqueUserCode);
            // ws = new WebSocket('ws://127.0.0.1:8080/chatroom/api/websocket/' + data);
        } else {
            callMessage(-1, "当前浏览器 不支持WebSocket服务！");
        }
    } else {
        callMessage(-1, "建立WebSocket连接失败！");
    }
}

window.onload = (evt) => {
    // setMessageToSystemInfoBoard('请注意！现在是广播事件：啊是多久啊圣诞啊实打实大苏打梵蒂冈的节阿松大是南大四年冬季阿三大苏打爱上你的')
    // console.log(document.getElementById('signined-user'))
}

const user = document.getElementById('unique-user-code').value;   // onlineUser
createWebSocketClient(user);

/**
 * 检查WebSocket连接是否为空
 * @returns {number}
 */
function checkWSIsNull() {
    return ws === undefined | ws === null;
}

// websocket 连接成功执行的回调函数
ws.onopen = (evt) => {
    if (checkWSIsNull()) {
        console.log("尚未建立WebSocket连接！")
        return false;
    }
    console.log('连接成功！');
    callMessage(0, 'WebSocket服务已成功建立连接！');
}

// websocket 连接过程中发生错误时执行的回调函数
ws.onerror = (evt) => {
    // setMessageToSystemInfoBoard('请注意！现在是广播事件：啊是多久啊圣诞啊实打实大苏打梵蒂冈的节阿松大是南大四年冬季阿三大苏打爱上你的')
    callMessage(-1, "WebSocket服务连接失败，请稍后重试！");
    console.log('WebSocket服务连接失败，请稍后重试！');
    console.log(evt)
}

// websocket 接收到消息时执行的回调函数
ws.onmessage = (evt) => {
    if (checkWSIsNull()) {
        console.log("尚未建立WebSocket连接！")
        return false;
    }

    // console.log(evt);

}

// 关闭 WebSocket 连接
function closeWebSocket() {
    if (!checkWSIsNull())
        ws.close();
}

// 监听窗口关闭事件，当窗口关闭时，主动去关闭 WebSocket 连接。防止连接还未断开就关闭窗口，从而导致服务端抛异常
// window.onunload = () => {
window.onbeforeunload = () => {
    closeWebSocket();
    // 同时发请求给服务端，注销此用户的本次会话实例
    // window.location.href = getProjectPath() + '/user/logout';
}

// 监听聊天输入框的回车键
content.addEventListener('keydown', function (evt) {
    let e = evt || window.evt || arguments.callee.caller.arguments[0];
    let key = e.which || e.keyCode || e.charCode;
    if (key && key === 13)
        if (!sendBtn.disabled)      // 发送按钮未被禁用才能够发送消息
            callSendMessage(evt)
});

// 监听 私发和群聊 两种类型的消息
var chatElem = document.querySelector('#chat-ipt #ipt-content');
if (chatElem) {
    let sendBtn = chatElem.querySelector('button[id="send"]');
    sendBtn.addEventListener('click', (evt) => {
        callSendMessage(evt);
    });
}

// 处理发送消息转为Json对象
function doMessageJsonData(content, messageType) {
    let sendMsg = {
        "content": content,
        "messageType": messageType,
        "sendTime": getDateTime()
    };

    return JSON.parse(JSON.stringify(sendMsg));
}

function callSendMessage(evt) {

    evt.preventDefault();
    let content = chatElem.querySelector('textarea[id="content"]');
    let contentVal = content.value;     // 发送内容
    if (contentVal === '' | contentVal.length === 0) {
        callMessage(1, "发送内容不能为空！");
        return ;
    }
    console.log(contentVal);

    // 判断当前聊天框是私聊消息还是群聊消息
    let chatTypeElem = document.querySelector('#chat-box #message div[class="exclusive-chat-box exclusive-chat-box-active"]');
    let chatTypeElemId = chatTypeElem.getAttribute('id');
    if (chatTypeElemId === 'private-message') {
        // console.log('private');

        /*let msgElem = document.createElement('div');
        msgElem.classList.add('msg', 'send_msg');

        let avatarElem = document.createElement('div');
        avatarElem.classList.add('avatar', 'send_avatar');
        avatarElem.style.background = 'url(../images/avatar/Member005) no-repeat';

        let nameElem = document.createElement('span');
        nameElem.classList.add('name', 'ellipsis');
        nameElem.innerText = 'Hello';

        let sentenceElem = document.createElement('div');
        sentenceElem.classList.add('sentence');

        let textElem = document.createElement('p');
        textElem.classList.add('text');
        textElem.innerText = contentVal;

        let triangleElem = document.createElement('div');
        triangleElem.classList.add('triangle');

        let timeElem = document.createElement('p');
        timeElem.classList.add('time');
        let sendTime = new Date();
        timeElem.innerText = sendTime.getFullYear() + "年" + (sendTime.getMonth() + 1) + "月" + sendTime.getDate() + "日 "
            + sendTime.getHours() + "时" + sendTime.getMinutes() + "分";

        sentenceElem.append(textElem, triangleElem, timeElem);
        msgElem.append(avatarElem, nameElem, sentenceElem);
        chatTypeElem.appendChild(msgElem);*/

        let friendId = document.querySelector('#my-friend-list a[class="my-friend content-active"]').dataset.id;
        // console.log(friendId)
        let sendMsgData = doMessageJsonData(contentVal, chatTypeElemId);    // 封装需要发送的消息
        // console.log(sendMsgData);

        $.ajax({
            url: getProjectPath() + '/user/chat/let-chat/' + friendId,
            type: 'POST',
            data: sendMsgData,
            success: (resp) => {    // 消息发送成功
                // console.log(resp);
                callMessage(0, resp.msg);

                // 封装发送
                let priMsg = resp.data;
                // console.log(priMsg)
                ws.send(JSON.stringify(priMsg));
                setMessageToPriMsgBoard(priMsg, friendId);
            },
            error: (resp) => {    // 消息发送失败
                console.log(resp);
                callMessage(-1, resp.msg);
            },
        })
        // sendUrl(getProjectPath() + '/user/chat/to-personal-chat/' + friendId, doSingleDataToJson(contentVal));

    } else if (chatTypeElemId === 'public-message') {
        // console.log('public');

    } else {
        alert('错误的消息框面板！');
        return ;
    }

    // 每次发送一条消息后消息栏置为空
    chatElem.querySelector('textarea[id="content"]').value = '';
}

// 发送消息
function sendMsg(msg) {
    const data = {
        // "messageType":
        "msg": msg
    };
    ws.send(JSON.parse(JSON.stringify(data)));
    // 同时将此消息插入至相应的消息框中（私聊消息、群聊消息、系统公告通知消息）
}

// 将系统公告消息添加至 系统广播板块上
function setMessageToSystemInfoBoard(msg) {
    let systemInfoShow = document.getElementById('system-message-show');
    for (let i = 0; i < 3; i++) {
        let newSystemInfoElement = document.createElement('p');
        // <p className="info-all infos"></p>
        newSystemInfoElement.classList.add("info-all");
        newSystemInfoElement.classList.add("infos");
        newSystemInfoElement.innerText = msg;
        // console.log(newSystemInfoElement)
        systemInfoShow.appendChild(newSystemInfoElement);
    }
}
setMessageToSystemInfoBoard("现在是广播事件：啊是多久啊圣诞啊实打实大苏打梵蒂冈的节阿松大是南大四年冬季阿三大苏打爱上你的");