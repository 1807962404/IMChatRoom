/**
 * WebSocket连接js文件
 * @type {null}
 */

var ws = null;

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

var priFriendId = 0;

// websocket 接收到消息时执行的回调函数
ws.onmessage = (evt) => {
    if (checkWSIsNull()) {
        console.log("尚未建立WebSocket连接！")
        return ;
    }

    // console.log(evt);
    let message = JSON.parse(evt.data);
    if (message.messageType === 'private-message') {
        // let friendId = document.querySelector('#my-friend-list a[class="my-friend content-active"]').dataset.id;
        /*console.log('friendId: ' + friendId);
        console.log(message.sendUser, message.sendUser.uId);
        console.log(message.receiveUser, message.receiveUser.uId);*/
        if (Number(priFriendId) != Number(message.receiveUser.uId)) {   // 若好友Id不等于消息接收者id
            setMessageToPriMsgBoard(message, message.sendUser.uId);     // 则将消息记录设置到发送者id中
        } else {
            setMessageToPriMsgBoard(message, priFriendId);
        }

    } else if (message.messageType === 'public-message') {


    } else if (message.messageType === 'public-message') {
        setMessageToSystemInfoBoard(message);
        setContentToOwnPublishedBroadcast(message);
    }
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

        priFriendId = document.querySelector('#my-friend-list a[class="my-friend content-active"]').dataset.id;
        // console.log(friendId)
        let sendMsgData = doMessageJsonData(contentVal, chatTypeElemId);    // 封装需要发送的消息
        // console.log(sendMsgData);

        $.ajax({
            url: getProjectPath() + '/user/chat/let-chat/' + priFriendId,
            type: 'POST',
            data: sendMsgData,
            success: (resp) => {    // 消息发送成功
                // console.log(resp);
                if (0 == resp.code) {
                    callMessage(0, resp.msg);

                    // 封装发送
                    let priMsg = resp.data;
                    // console.log(priMsg)
                    ws.send(JSON.stringify(priMsg));    // 封装私聊消息

                } else {    // 消息发送失败
                    callMessage(resp.code, resp.msg);
                }
            },
            error: (resp) => {
                console.log(resp);
                callMessage(-1, "***出错啦，请稍后再试！");
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

/*// 发送消息
function sendMsg(msg) {
    const data = {
        // "messageType":
        "msg": msg
    };
    ws.send(JSON.parse(JSON.stringify(data)));
    // 同时将此消息插入至相应的消息框中（私聊消息、群聊消息、系统公告通知消息）
}*/


// 将系统公告消息添加至 系统广播板块上
function setMessageToSystemInfoBoard(msg) {
    let systemInfoShow = document.getElementById('system-message-show');
    let newSystemInfoElement = document.createElement('p');
    // <p className="info-all infos"></p>
    newSystemInfoElement.classList.add("info-all");
    newSystemInfoElement.classList.add("infos");
    newSystemInfoElement.innerText = msg;
    // console.log(newSystemInfoElement)

    let hasFirstElem = systemInfoShow.firstElementChild;
    if (hasFirstElem) {
        systemInfoShow.insertBefore(newSystemInfoElement, hasFirstElem);
    } else {
        systemInfoShow.appendChild(newSystemInfoElement);
    }
}
// 监听 发布系统广播 的button
var broadcastBtn = document.querySelector('#publish-system-broadcast button[type="button"]');
if (broadcastBtn) {
    broadcastBtn.addEventListener('click', function (evt) {
        let publishBroadcastElem = document.getElementById('publish-broadcast');
        let publishBroadcastVal = publishBroadcastElem.value;
        if (publishBroadcastVal === '' | publishBroadcastVal.length === 0) {
            callMessage(1, "发布内容不能为空！");
            return;
        };

        $.ajax({
            url: getProjectPath() + '/chat/let-chat/0',
            type: 'POST',
            data: doMessageJsonData(publishBroadcastVal, 'system-message'),
            success: (resp) => {
                callMessage(resp.code, resp.msg);
                if (resp.code === 0) {
                    let systemMsg = resp.data;
                    ws.send(JSON.stringify(systemMsg));     // 封装系统公告消息
                }
            },
            error: (resp) => {
                callMessage(-1, "***出错啦，请稍后再试！");
            }
        })
    });
}

/*// 监听 意见反馈 栏的提交按钮
var feedbackElem = document.querySelector('#feedback .feedback-opt');
feedbackElem.querySelector('button[type="button"]').addEventListener('click', (evt) => {
    let feedbackContent = feedbackElem.querySelector('textarea[id="feedback-content"]');
    if (feedbackContent.value === '' || feedbackContent.value.length === 0) {
        callMessage(1, "反馈内容不能为空！");
        return ;
    }

    let data = {
        'fbContent': feedbackContent.value,
        'publishTime': getDateTime()
    }
    $.ajax({
        url: getProjectPath() + "/entity/send-feedback",
        type: 'POST',
        data: JSON.parse(JSON.stringify(data)),
        success: (resp) => {        // 意见反馈成功
            if (resp.code === 0) {
                callMessage(0, resp.msg);
                feedbackContent.value = '';

                let newFeedbackContent = resp.data;
                ws.send(doSingleDataToJson(newFeedbackContent));
                // 写入至意见反馈中
                setContentToFeedbackList(resp.data);
            } else {
                callMessage(resp.code, resp.msg);
            }
        },
        error: (resp) => {
            console.log(resp);
            callMessage(-1, "出错啦，请稍后再试！");
        }
    });
    /!*sendUrl(getProjectPath() + "/entity/send-feedback", 'POST', JSON.parse(JSON.stringify(data)),
        getProjectPath() + "/main");
    feedbackContent.value = '';*!/
});*/
