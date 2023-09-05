/**
 * WebSocket连接js文件
 * @type {null}
 */

var ws = null;

/**
 * 检查WebSocket连接是否为空
 * @returns {number}
 */
function checkWSIsNull() {
    return ws === undefined | ws === null;
}

// 创建WebSocket连接
function createWebSocketClient(uniqueUserCode) {

    if (uniqueUserCode) {
        // 判断当前浏览器是否支持WebSocket
        if ('WebSocket' in window) {
            ws = new WebSocket('ws://127.0.0.1:8080' + getProjectPath() + '/api/websocket/' + uniqueUserCode);
        } else {
            callMessage(-1, "当前浏览器 不支持WebSocket服务！");
        }
    } else {
        callMessage(-1, "建立WebSocket连接失败！");
    }
}

const user = document.getElementById('unique-user-code').value;   // onlineUser
createWebSocketClient(user);

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
    console.log(evt);
}

// websocket 接收到消息时执行的回调函数
ws.onmessage = (evt) => {
    if (checkWSIsNull()) {
        console.log("尚未建立WebSocket连接！")
        return;
    }

    // console.log(evt);
    let message = JSON.parse(evt.data);
    console.log("message: ", message);
    if (message.messageType === 'private-message') {
        let myFriends = document.querySelectorAll('#my-friend-list a');
        for (let i = 0; i < myFriends.length; i++) {
            let myFriend = myFriends[i];
            let myFriendId = Number(myFriend.dataset.id);
            // 判断我的好友列表中，哪个好友是属于这条消息的发送者或接收者
            if (myFriendId === Number(message.receiveUser.uId) || myFriendId === Number(message.sendUser.uId)) {
                if (myFriendId !== Number(message.receiveUser.uId)) {   // 若好友Id不等于消息接收者id
                    setMessageToPriMsgBoard(message, message.sendUser.uId);     // 则将消息记录设置到发送者id中
                    callMessage(0, "好友通知：" + message.sendUser.nickname + " 发来一条消息！");
                } else {
                    setMessageToPriMsgBoard(message, myFriendId);
                }
            }
        }

    } else if (message.messageType === 'public-message') {
        setMessageToPubMsgBoard(message, message.receiveGroup.gCode);
        if (Number(thisUserId) !== Number(message.sendUser.uId)) {
            callMessage(0, "群通知：" + message.sendUser.nickname + " 发来一条消息！");
        }

    } else if (message.messageType === 'system-message') {
        callMessage(0, "管理员：" + message.publisher.nickname + " 发布了一则系统公告！");
        setMessageToSystemInfoBoard(message);
        setContentToOwnPublishedBroadcast(message);

    } else if (message.messageType === 'abstract-message') {
        callMessage(0, "管理员：" + message.publisher.nickname + " 发表了一部优文摘要文摘！");
        setMessageToArticleBoard(message);
        setContentToOwnPublishedArticle(message);

    } else if (message.messageType === 'online-count-message') {
        // callMessage(0, "有一用户登陆！");
        setMessageToOnlineCountBoard(message);
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

window.onunload = () => {
    clearInterval(curTimeEvt);     // 清除获取当前时间的间隔定时器
}

window.onload = () => {
    realTimeUpdateOnlineCount();
}

function realTimeUpdateOnlineCount() {
    $.ajax({
        url: getProjectPath() + '/user/chat/online-user-count',
        type: 'GET',
        success: (resp) => {
            // console.log(resp);

            if (resp.code === 0) {
                // console.log('当前用户总在线数为' + resp.data);
                // 设置在线用户数量
                let onlineNumberMsg = resp.data;
                ws.send(JSON.stringify(onlineNumberMsg));
            }
        },
        error: function (resp) {
            console.log("Error: " + resp);
        }
    })
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
        "sendTime": getDateTime(),
        "displayStatus": "0"
    };

    return JSON.parse(JSON.stringify(sendMsg));
}

function callSendMessage(evt) {

    evt.preventDefault();
    let content = chatElem.querySelector('textarea[id="content"]');
    let contentVal = content.value;     // 发送内容
    if (contentVal === '' | contentVal.length === 0) {
        callMessage(1, "发送内容不能为空！");
        return;
    }
    console.log(contentVal);

    // 判断当前聊天框是私聊消息还是群聊消息
    let chatTypeElem = document.querySelector('#chat-box #message div[class="exclusive-chat-box exclusive-chat-box-active"]');
    let chatTypeElemId = chatTypeElem.getAttribute('id');
    if (chatTypeElemId === 'private-message') {
        // console.log('private');

        let priFriendId = document.querySelector('#my-friend-list a[class="my-friend content-active"]').dataset.id;
        // console.log(friendId)
        let sendMsgData = doMessageJsonData(contentVal, chatTypeElemId);    // 封装需要发送的消息
        // console.log(sendMsgData);

        $.ajax({
            url: getProjectPath() + '/user/chat/communicate/' + priFriendId,
            type: 'POST',
            data: sendMsgData,
            success: (resp) => {    // 消息发送成功
                // console.log(resp);
                callMessage(resp.code, resp.msg);

                if (0 == resp.code) {
                    // 封装发送
                    let priMsg = resp.data;
                    // console.log(priMsg)
                    ws.send(JSON.stringify(priMsg));    // 封装私聊消息
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
        let gCode = document.querySelector('#my-group-list a[class="my-entered-group content-active"]').dataset.code;
        // console.log(gCode)
        let sendMsgData = doMessageJsonData(contentVal, chatTypeElemId);    // 封装需要发送的消息
        // console.log(sendMsgData);

        $.ajax({
            url: getProjectPath() + '/user/chat/communicate/' + gCode,
            type: 'POST',
            data: sendMsgData,
            success: (resp) => {    // 消息发送成功
                // console.log(resp);
                callMessage(resp.code, resp.msg);

                if (0 == resp.code) {
                    // 封装发送
                    let pubMsg = resp.data;
                    // console.log(priMsg)
                    ws.send(JSON.stringify(pubMsg));    // 封装私聊消息
                }
            },
            error: (resp) => {
                console.log(resp);
                callMessage(-1, "***出错啦，请稍后再试！");
            },
        })

    } else {
        alert('错误的消息框面板！');
        return;
    }

    // 每次发送一条消息后消息栏置为空
    content.value = '';
}

// 将系统公告消息添加至 系统广播板块上
function setMessageToSystemInfoBoard(msg) {
    let systemInfoShow = document.getElementById('system-message-show');
    let newSystemInfoElement = document.createElement('p');
    newSystemInfoElement.classList.add("info-all", "infos");
    newSystemInfoElement.innerText = msg.content;
    // console.log(newSystemInfoElement);

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
        }
        ;

        // console.log(doMessageJsonData(publishBroadcastVal, 'system-message'))
        $.ajax({
            url: getProjectPath() + '/user/chat/communicate/0',
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
                console.log(resp);
                callMessage(-1, "***出错啦，请稍后再试！");
            }
        });
        publishBroadcastElem.value = '';
    });
}

// 将优文摘要消息添加至 优文摘要板块上
function setMessageToArticleBoard(msg) {
    let articleInfoShow = document.getElementById('article-message-show');
    let newArticleInfoElem = document.createElement('p');
    newArticleInfoElem.classList.add("default-msg-p");
    newArticleInfoElem.innerText = msg.content;
    // console.log(newArticleInfoElem)

    let hasFirstElem = articleInfoShow.firstElementChild;
    if (hasFirstElem) {
        articleInfoShow.insertBefore(newArticleInfoElem, hasFirstElem);
    } else {
        articleInfoShow.appendChild(newArticleInfoElem);
    }
}

// 监听 发布优文摘要 的button
var articleBtn = document.querySelector('#publish-excellent-abstract button[type="button"]');
if (articleBtn) {
    articleBtn.addEventListener('click', function (evt) {
        let publishArticleElem = document.getElementById('publish-abstract');
        let publishArticleVal = publishArticleElem.value;
        if (publishArticleVal === '' | publishArticleVal.length === 0) {
            callMessage(1, "发表优文摘要文章内容不能为空！");
            return;
        }
        ;

        $.ajax({
            url: getProjectPath() + '/user/chat/communicate/0',
            type: 'POST',
            data: doMessageJsonData(publishArticleVal, 'abstract-message'),
            success: (resp) => {
                callMessage(resp.code, resp.msg);
                if (resp.code === 0) {
                    let articleMsg = resp.data;
                    ws.send(JSON.stringify(articleMsg));     // 封装优文摘要消息
                }
            },
            error: (resp) => {
                console.log(resp);
                callMessage(-1, "***出错啦，请稍后再试！");
            }
        });
        publishArticleElem.value = '';
    });
}

// 监听 意见反馈 栏的提交按钮
var feedbackElem = document.querySelector('#feedback .feedback-opt');
feedbackElem.querySelector('button[type="button"]').addEventListener('click', (evt) => {
    let feedbackContent = feedbackElem.querySelector('textarea[id="feedback-content"]');
    if (feedbackContent.value === '' || feedbackContent.value.length === 0) {
        callMessage(1, "反馈内容不能为空！");
        return ;
    }

    let feedbackContentVal = feedbackContent.value;
    $.ajax({
        url: getProjectPath() + "/user/chat/communicate/0",
        type: 'POST',
        data: doMessageJsonData(feedbackContentVal, 'feedback-message'),
        success: (resp) => {        // 意见反馈成功
            callMessage(resp.code, resp.msg);
            if (resp.code === 0) {
                feedbackContent.value = '';
                // 写入至意见反馈中
                setContentToFeedbackList(resp.data);
            }
        },
        error: (resp) => {
            console.log(resp);
            callMessage(-1, "***出错啦，请稍后再试！");
        }
    });
});