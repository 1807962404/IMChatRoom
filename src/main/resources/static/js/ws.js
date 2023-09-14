/**
 * WebSocket连接js文件
 * @type {null}
 */

// 登陆用户
const signInUser = JSON.parse(sessionStorage.getItem('signInUser'));
var ws = null;

// 创建websocket连接
function createWebSocketClient(uniqueUserCode) {
    if (uniqueUserCode) {
        // 判断当前浏览器是否支持WebSocket
        if ('WebSocket' in window) {
            ws = new WebSocket('ws://127.0.0.1:80' + getProjectPath() + '/api/websocket/' + uniqueUserCode);
            // ws = new WebSocket('ws://8.130.104.52:80' + getProjectPath() + '/api/websocket/' + uniqueUserCode);
        } else {
            callMessage(-1, "当前浏览器 不支持WebSocket服务！");
        }
    } else {
        callMessage(-1, "建立WebSocket连接失败！");
    }
}

// 1、为此用户创建websocket连接（每次刷新页面都会创建WebSocket连接）
let uniqueUserCode = signInUser.uniqueUserCode;
// console.log('uniqueUserCode: ', uniqueUserCode);
createWebSocketClient(uniqueUserCode);

sleep(sleepTime).then(()=> {
    console.log(ws.readyState, WebSocket.OPEN, Number(ws.readyState) === WebSocket.OPEN);
});

/**
 * 检查WebSocket连接是否为空
 * @returns {number}
 */
function checkWSIsNull() {
    let result = ws === null || ws === undefined;
    if (result)
        console.log(('尚未建立WebSocket连接！ws为空：', ws));

    return result;
}

// websocket 连接成功执行的回调函数
ws.onopen = () => {
    console.log('WebSocket服务已成功建立连接！');
    callMessage(0, 'WebSocket服务已成功建立连接！');
}

// websocket 连接过程中发生错误时执行的回调函数
ws.onerror = () => {
    console.log('WebSocket服务连接失败，请稍后重试！');
    callMessage(-1, "WebSocket服务连接失败，请稍后重试！");
}

// websocket 接收到消息时执行的回调函数
ws.onmessage = (evt) => {
    if (checkWSIsNull())
        return;

    // console.log(evt);
    let message = JSON.parse(evt.data);
    // console.log("message: ", message);

    let messageType = message.messageType;
    if (messageType === null || messageType === undefined)
        return;

    if (messageType === 'sign-in-message') {
        // 用户登陆消息（会给该用户所有的好友发送好友上线通知）
        callMessage(0, '您的好友：' + message.content.nickname + ' 已上线！');

    } else if (messageType === 'online-count-message') {
        setMessageToOnlineCountBoard(message);

    } else if (messageType.endsWith('group-message')) {
        // 群消息
        if (messageType === 'enter-group-message') {
            callMessage(0,
                "群通知：用户 " + message.content.member.nickname + " 申请加入您的群组：" + message.content.group.gName + "！");
            setMessageToGroupNotifications(message.content);

        } else if (messageType === 'agree-enter-group-message') {
            callMessage(0,
                "群通知：群组 " + message.content.group.gName + " 已同意您的入群申请！");
            getMyGroups();

        } else if (messageType === 'drop-member-from-group-message') {
            callMessage(0, "群通知：群主 " + message.content.group.hostUser.nickname +
                " 已将您移出群聊：" + message.content.group.gName + " ！");

        } else if (messageType === 'exit-group-message') {
            callMessage(0,
                "群通知：用户 " + message.content.member.nickname + " 已退出您的群组：" + message.content.group.gName + "！");

        } else if (messageType === 'dissolve-group-message') {
            /*let dissolveGroupMembers = message.content.members;
            for (let i = 0; i < dissolveGroupMembers.length; i++) {
                if (dissolveGroupMembers[i].guStatus === '0')

            }*/
            callMessage(0, "群通知：群组 " + message.content.gName + " 已解散，已自动将您移出群聊！");
        }

    } else if (messageType.endsWith('friend-message')) {
        // 朋友 消息
        if (message.content.fsStatus === '0') {
            // 已同意您的好友申请
            callMessage(0, "好友通知：用户 " + message.content.friendUser.nickname + " 已同意您的好友申请！");

        } else if (message.content.fsStatus === '1') {
            // 用户给本人发送过来好友申请
            callMessage(0, "好友通知：用户 " + message.content.hostUser.nickname + " 发送来一条好友申请！");
            findFriendNotifications();
        }

    } else {
        if (messageType === 'private-message') {
            /*if (Number(thisUserId) === Number(message.receiveUser.uId)) {   // 本人是消息接收者
                setMessageToPriMsgBoard(message, message.sendUser.uId);     // 则将消息记录设置到消息接收者id中
                callMessage(0, "好友通知：" + message.sendUser.nickname + " 发来一条消息！");
            }
            else
                setMessageToPriMsgBoard(message, message.receiveUser.uId);     // 则将消息记录设置到发送者id中*/

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

        } else if (messageType === 'public-message') {
            setMessageToPubMsgBoard(message, message.receiveGroup.gCode);
            if (Number(thisUserId) !== Number(message.sendUser.uId)) {
                callMessage(0, "群通知：" + message.sendUser.nickname + " 发来一条消息！");
            }

        } else if (messageType === 'system-message') {
            if (Number(thisUserId) !== Number(message.publisher.uId))
                callMessage(0, "管理员：" + message.publisher.nickname + " 发布了一则系统公告！");
            else
                setContentToOwnPublishedBroadcast(message);

            setMessageToSystemInfoBoard(message);

        } else if (messageType === 'abstract-message') {
            if (Number(thisUserId) !== Number(message.publisher.uId))
                callMessage(0, "管理员：" + message.publisher.nickname + " 发表了一部优文摘要文摘！");
            else
                setContentToOwnPublishedArticle(message);

            setMessageToArticleBoard(message);
        }
    }
}

// 关闭 WebSocket 连接
function closeWebSocket() {
    if (!checkWSIsNull())
        ws.close();
}

// 监听窗口关闭事件，当窗口关闭时，主动去关闭 WebSocket 连接。防止连接还未断开就关闭窗口，从而导致服务端抛异常
window.onbeforeunload = () => {
    closeWebSocket();
    // 同时发请求给服务端，注销此用户的本次会话实例
    // window.location.href = getProjectPath() + '/user/logout';
}

// 设置用户在线人数
function setMessageToOnlineCountBoard(message) {
    document.querySelector('#chat-header #info-show #online-user-count #count').innerText = message.content;
}

// 监听聊天输入框的回车键
var content = document.querySelector('#chat-ipt #ipt-content #content');
if (content) {
    content.addEventListener('keydown', function (evt) {
        let e = evt || window.evt || arguments.callee.caller.arguments[0];
        let key = e.which || e.keyCode || e.charCode;
        if (key && key === 13)
            if (!sendBtn.disabled)      // 发送按钮未被禁用才能够发送消息
                callSendMessage(evt)
    });
}

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
    // console.log(contentVal);

    // 判断当前聊天框是私聊消息还是群聊消息
    let chatTypeElem = document.querySelector('#chat-box #message div[class="exclusive-chat-box exclusive-chat-box-active"]');
    let chatTypeElemId = chatTypeElem.getAttribute('id');
    if (chatTypeElemId === 'private-message') {

        let priFriendId = document.querySelector('#my-friend-list a[class="my-friend content-active"]').dataset.id;
        // console.log(friendId)
        let sendMsgData = doMessageJsonData(contentVal, chatTypeElemId);    // 封装需要发送的消息
        // console.log(sendMsgData);

        if (checkWSIsNull()) {
            callMessage(-1, '系统服务崩溃，请稍后再试！');
            return;
        }

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
    let systemInfoShowElem = document.getElementById('system-message-show');

    let newSystemInfoElement = document.createElement('p');
    newSystemInfoElement.classList.add("info-all", "infos");
    newSystemInfoElement.innerText = msg.content;

    let nonBroadcastsElem = document.querySelector('#system-message .non-broadcasts');
    if (nonBroadcastsElem && !nonBroadcastsElem.classList.contains('hidden-el'))
        nonBroadcastsElem.classList.add('hidden-el');

    let hasBroadcastsElem = systemInfoShowElem.querySelector('.has-broadcasts');
    if (hasBroadcastsElem) {
        let hasFirstElem = hasBroadcastsElem.firstElementChild;
        if (hasFirstElem)
            hasBroadcastsElem.insertBefore(newSystemInfoElement, hasFirstElem);
        else
            hasBroadcastsElem.appendChild(newSystemInfoElement);

    } else {
        let hasNewBroadcastsElem = document.createElement('div');
        hasNewBroadcastsElem.classList.add('has-broadcasts');
        hasNewBroadcastsElem.appendChild(newSystemInfoElement);

        systemInfoShowElem.appendChild(hasNewBroadcastsElem);
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

        if (checkWSIsNull()) {
            callMessage(-1, '系统服务崩溃，请稍后再试！');
            return;
        }

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
    let articleInfoShowElem = document.getElementById('article-message-show');

    let newArticleInfoElem = document.createElement('p');
    newArticleInfoElem.classList.add("default-msg-p");
    newArticleInfoElem.innerText = msg.content;

    let nonAbstractsElem = document.querySelector('#article-list .non-abstracts');
    if (nonAbstractsElem && !nonAbstractsElem.classList.contains('hidden-el'))
        nonAbstractsElem.classList.add('hidden-el');

    let hasAbstractsElem = articleInfoShowElem.querySelector('.has-abstracts');
    if (hasAbstractsElem) {
        let hasFirstElem = hasAbstractsElem.firstElementChild;
        if (hasFirstElem)
            hasAbstractsElem.insertBefore(newArticleInfoElem, hasFirstElem);
        else
            hasAbstractsElem.appendChild(newArticleInfoElem);

    } else {
        let hasNewBroadcastsElem = document.createElement('div');
        hasNewBroadcastsElem.classList.add('has-abstracts');
        hasNewBroadcastsElem.appendChild(newArticleInfoElem);

        articleInfoShowElem.appendChild(hasNewBroadcastsElem);
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
        };

        if (checkWSIsNull()) {
            callMessage(-1, '系统服务崩溃，请稍后再试！');
            return;
        }

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
if (feedbackElem) {
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
}

// 加入群聊
function enterGroup(gCode) {

    if (checkWSIsNull()) {
        callMessage(-1, '系统服务崩溃，请稍后再试！');
        return;
    }

    $.ajax({
        url: getProjectPath() + '/user/enter-group/' + gCode,
        type: 'GET',
        success: (resp) => {        // 意见反馈成功
            callMessage(resp.code, resp.msg);

            if (resp.code === 0) {
                let enterGroupUser = resp.data;     // 用户申请加入的群组 信息
                // 封装发送 用户申请加入的群组 消息
                ws.send(JSON.stringify(doMessageJsonData(enterGroupUser, 'enter-group-message')));
            }
        },
        error: (resp) => {
            console.log(resp);
            callMessage(-1, "***出错啦，请稍后再试！");
        }
    });
}

// 同意用户进入自己的群组
function doAgreeUserToGroup(gId, uId) {

    if (checkWSIsNull()) {
        callMessage(-1, '系统服务崩溃，请稍后再试！');
        return;
    }

    $.ajax({
        url: getProjectPath() + '/user/agree-enter-group/' + gId + '/' + uId,
        type: 'GET',
        success: (resp) => {
            callMessage(resp.code, resp.msg);
            if (resp.code === 0) {
                let agreeEnterGroupUser = resp.data;     // 同意用户入群申请 信息
                // 封装发送 同意用户入群申请 消息
                ws.send(JSON.stringify(doMessageJsonData(agreeEnterGroupUser, 'agree-enter-group-message')));
                findGroupNotifications();
            }
        },
        error: (resp) => {
            console.log(resp);
            callMessage(-1, "***哎呀出错啦，请稍后再试吧！");
        }
    })
}

// 最终将用户移出群聊
function doFinalDropMemberFromGroupBtnListener(isCancelld, gId, uId) {
    hideModal();
    if (!isCancelld) {

        if (checkWSIsNull()) {
            callMessage(-1, '系统服务崩溃，请稍后再试！');
            return;
        }

        $.ajax({
            url: getProjectPath() + '/user/drop-member-from-group/' + gId + '/' + uId,
            type: 'GET',
            success: (resp) => {
                callMessage(resp.code, resp.msg);

                if (resp.code === 0) {
                    let dropMemberFromGroupUser = resp.data;     // 将用户移出群组 信息
                    // 封装发送 将用户移出群组 消息
                    ws.send(JSON.stringify(doMessageJsonData(dropMemberFromGroupUser, 'drop-member-from-group-message')));
                    sleep(sleepTime).then(() => window.location.href= getProjectPath() + '/main');
                    fingMyCreatedGroups();
                }
            },
            error: (resp) => {
                console.log(resp);
                callMessage(-1, "***哎呀出错啦，请稍后再试吧！");
            }
        })
    } else {
        callMessage(1, "已取消操作！");
    }
}

// 最终退出群聊
function doFinalExitGroupBtnListener(isCancelld, gCode) {
    hideModal();
    if (!isCancelld) {

        if (checkWSIsNull()) {
            callMessage(-1, '系统服务崩溃，请稍后再试！');
            return;
        }

        $.ajax({
            url: getProjectPath() + '/user/exit-group/' + gCode,
            type: 'GET',
            success: (resp) => {        // 意见反馈成功
                callMessage(resp.code, resp.msg);

                if (resp.code === 0) {
                    let exitGroupUser = resp.data;     // 用户退出的群组 信息
                    // 封装发送 用户退出的群组 消息
                    ws.send(JSON.stringify(doMessageJsonData(exitGroupUser, 'exit-group-message')));
                    sleep(sleepTime).then(() => window.location.href= getProjectPath() + '/main');
                }
            },
            error: (resp) => {
                console.log(resp);
                callMessage(-1, "***出错啦，请稍后再试！");
            }
        });
    } else {
        callMessage(1, "已取消操作！");
    }
}

// 最终解散群聊
function doFinalDissolveGroupBtnListener(isCancelld, gCode) {
    hideModal();
    if (!isCancelld) {

        if (checkWSIsNull()) {
            callMessage(-1, '系统服务崩溃，请稍后再试！');
            return;
        }

        $.ajax({
            url: getProjectPath() + '/user/dissolve-group/' + gCode,
            type: 'GET',
            success: (resp) => {        // 意见反馈成功
                callMessage(resp.code, resp.msg);

                if (resp.code === 0) {
                    let dissolveGroup = resp.data;     // 解散群组 信息
                    // 封装发送 解散群组 消息
                    ws.send(JSON.stringify(doMessageJsonData(dissolveGroup, 'dissolve-group-message')));
                    sleep(sleepTime).then(() => window.location.href= getProjectPath() + '/main');
                }
            },
            error: (resp) => {
                console.log(resp);
                callMessage(-1, "***出错啦，请稍后再试！");
            }
        });
    } else {
        callMessage(1, "已取消操作！");
    }
}

// 为 点击添加好友 的a(class: add-friend-btn)标签绑上监听事件
function doAddFriendBtnListener(evt, addFriendBtnElem) {
    evt.preventDefault();

    if (checkWSIsNull()) {
        callMessage(-1, '系统服务崩溃，请稍后再试！');
        return;
    }

    // /chatroom/user/add-friend/?
    let addFriendUrl = addFriendBtnElem.querySelector('a[class="add-friend-btn"]').dataset.gohref;
    $.ajax({
        url: addFriendUrl,
        type: 'GET',
        success: (resp) => {
            callMessage(resp.code, resp.msg);

            if (resp.code === 0) {
                let friendShip = resp.data;     // 友情 信息

                if (friendShip.fsStatus === '0') {
                    // 正处于好友状态，说明发送的请求是：同意好友申请 请求
                    ws.send(JSON.stringify(doMessageJsonData(friendShip, 'agree-add-friend-message')));
                    findFriendNotifications();

                } else if (friendShip.fsStatus === '1') {
                    // 好友关系确认中，说明发送的请求是：添加好友 请求
                    ws.send(JSON.stringify(doMessageJsonData(friendShip, 'add-friend-message')));
                }
                // sleep(sleepTime).then(() => window.location.href= getProjectPath() + '/main');
            }
        },
        error: (resp) => {
            console.log(resp);
            callMessage(-1, "***哎呀出错啦，请稍后再试吧！");
        }
    })
}