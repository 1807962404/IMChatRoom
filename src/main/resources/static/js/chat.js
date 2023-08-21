/**
 * 聊天页面js文件
 * @type {Element}
 */

// 滑动私聊聊天版
function scrollBoard() {
    let elems = new Array();
    elems.push(document.querySelector('#chat-bg #chat-box'));
    // elems.push(document.querySelector('#chat-bg #menu-bar #system-message'));
    for (let i = 0; i < elems.length; i++) {
        let elem = elems[i];
        //判断元素是否出现了滚动条
        if(elem.scrollHeight > elem.clientHeight) {
            let chatBoard = document.querySelector('#chat-bg #chat-box #message .exclusive-chat-box-active');
            // 只有聊天板界面是私聊或群聊版才需要滑动至底部
            if (chatBoard.id === 'private-message' || chatBoard.id === 'public-message') {
                //设置滚动条到最底部
                elem.scrollTop = elem.scrollHeight;
                elem.style.opacity = 1;
            }
        }
    }
}

// 获取用户在线数量
function getOnlineUserCount() {
    $.ajax({
        url: getProjectPath() + '/user/online-user-count',
        type: 'GET',
        success: function (resp) {
            console.log(resp)
            // 成功处理逻辑

            if (resp.code === 0) {
                // console.log('当前用户总在线数为' + resp.data);
                // 设置在线用户数量
                document.querySelector('#chat-header #info-show #online-user-count #count').innerText = resp.data;
            }
        },
        error: function (resp) {
            console.log(resp)
        }
    })
};

scrollBoard();
getOnlineUserCount();

setInterval(() => {
    getOnlineUserCount();
}, 60000);   // 1分钟发送一次请求

// 监听发送内容框
const content = document.querySelector('#chat-ipt #ipt-content #content');
// 检查内容狂的长度
function checkContentLength(content, contentLength) {
    let contentVal = content.value
    if (contentVal.length > contentLength)
        callMessage(1, "发送内容字数不得超过" + contentLength + "！");
    content.value = contentVal.substring(0, contentLength);
}
content.addEventListener('change', function () {
    checkContentLength(this, 120);
});
content.addEventListener('keydown', function () {
    checkContentLength(this, 120);
});
content.addEventListener('keyup', function () {
    checkContentLength(this, 120);
});

var sendBtn = document.getElementById('send');
// 禁用发送消息的textarea和发送button
sendBtn.disabled = true;
content.disabled = true;

// 尚不支持的功能 提醒
var unAllowedFunctions = document.querySelectorAll('.unAllowedFunction');
for (let i = 0; i < unAllowedFunctions.length; i++) {
    unAllowedFunctions[i].addEventListener('click', (evt) => {
        evt.preventDefault();
        callMessage(1, "此功能暂未开放，相关功能将在后续更新中陆续推出！");
        return false;
    });
}

function getDateTime() {
    let nowDate = new Date();
    let hour = nowDate.getHours();
    let minutes = nowDate.getMinutes();
    let seconds = nowDate.getSeconds();
    return nowDate.getFullYear() + "-" + (nowDate.getMonth() + 1) + "-" + nowDate.getDate() + " " +
        (hour < 10 ? ('0' + hour) : hour) + ":" +
        (minutes < 10 ? ('0' + minutes) : minutes) + ":" +
        (seconds < 10 ? ('0' + seconds) : seconds);
}

// 格式化日期 封装函数
function FormatDate(date) { //参数是时间
    let myDate = new Date(date);
    let hour = myDate.getHours();
    let minutes = myDate.getMinutes();
    let seconds = myDate.getSeconds();
    return myDate.getFullYear() + "-" + (myDate.getMonth() + 1) + "-" + myDate.getDate() + " " +
        (hour < 10 ? ('0' + hour) : hour) + ":" +
        (minutes < 10 ? ('0' + minutes) : minutes) + ":" +
        (seconds < 10 ? ('0' + seconds) : seconds);
}

/**
 * 导航栏切换：
 * 1、页面加载
 *  左侧消息窗口栏显示 默认框：document.querySelectorAll('#chat-box #message .exclusive-chat-box')[0]
 *  右侧菜单栏显示 默认：document.querySelectorAll("#chat-bg #menu-bar #menu #expand-function .header li")[0]
 *  右侧菜单栏内容显示 默认：document.querySelectorAll("#chat-bg #menu-bar #menu #expand-function .box li")[0];
 *
 * 2、点击右侧菜单栏：
 *  对应菜单栏添加类名 active，其余的需要删除类名 active；
 *  显示对应序号的右侧菜单栏内容：加类名 active，其余的需要删除类名 active；
 *      如果是最后一张选项卡，则设置左侧消息默认窗口 以及 左侧发送消息栏失效，
 */

// 右侧菜单栏 选项卡
var menubars = document.querySelectorAll("#chat-bg #menu-bar #menu #expand-function .header li");
// 右侧菜单栏 选项卡 记录索引
var menubar_index = 0;      // 使用时需要注意该变量会被认作是字符串，所以需要对其进行强制转换：Number(menubar_index)
// 右侧菜单栏下的内容栏 选项卡
var menubar_content_li = document.querySelectorAll("#chat-bg #menu-bar #menu #expand-function .box li");
// 获取具有 class: active 属性的 右侧菜单栏选项卡 下所有的 a标签（需要排除第一个）
var menubar_content_active = document.querySelectorAll("#chat-bg #menu-bar #menu #expand-function .box li[class='active'] a");
// 左侧消息窗口栏 选项卡
var msg_window_bars = document.querySelectorAll('#chat-box #message .exclusive-chat-box');

for (let i = 0; i < menubars.length; i++) {
    // 给右侧菜单栏的每个选项卡 赋上索引
    let menubar = menubars[i];
    menubar.dataset.index = i;

    // 再监听 右侧菜单栏的各个选项卡 的click点击事件
    menubar.addEventListener('click', function (evt) {
        // 记录点击的 右侧菜单栏 选项卡的索引index
        menubar_index = this.dataset.index;
        // console.log(menubar_index)

        for (let j = 0; j < menubars.length; j++) {
            // 利用 排他思想，先将 右侧菜单栏的各个选项卡 中的 class：active属性去掉
            menubars[j].classList.remove('active');
            // 再将 右侧菜单栏下的各个 内容栏选项卡 中的 class：active属性去掉
            menubar_content_li[j].classList.remove('active');
        }

        // 每次点击 右侧菜单栏都会执行
        for (let j = 0; j < menubar_content_active.length; j++) {
            // 移除 右侧菜单栏下所有a标签的 class: content-active 属性
            menubar_content_active[j].classList.remove('content-active');
        }

        for (let j = 0; j < msg_window_bars.length; j++) {
            // 移除各个 左侧消息窗口 的 class: exclusive-chat-box-active 属性
            msg_window_bars[j].classList.remove('exclusive-chat-box-active');
        }

        // 隐藏聊天框上中部位置的内容，即隐藏显示与谁聊天的内容
        let chatBoardHeaderInfo = document.querySelector('#chat-obj');
        chatBoardHeaderInfo.classList.add('hidden-el');

        // 禁用发送消息的textarea和发送button
        sendBtn.disabled = true;
        content.disabled = true;

        // 最后给点击的这个 右侧菜单栏选项卡 添加上 class：active属性
        this.classList.add('active');
        // 给与点击的右侧菜单栏选项卡 对应索引位置上的 右侧菜单栏下的内容栏选项卡 添加 class：active属性
        menubar_content_li[Number(menubar_index)].classList.add('active');
        // 如果是 右侧菜单栏选项卡 的最后一张选项卡
        if (Number(menubar_index) === menubars.length - 1) {
            content.value = '';     // 发送框消息置为空

        } else {
            // 左侧消息窗口 将一直保持选中 默认窗口 状态
            msg_window_bars[0].classList.add('exclusive-chat-box-active');
        }

        // 切换左侧消息窗口选项卡
        changeMsgWindow();
    });
};
// 切换左侧消息窗口选项卡
function changeMsgWindow() {
    menubar_content_active = document.querySelectorAll("#chat-bg #menu-bar #menu #expand-function .box li[class='active'] a");
    // console.log(menubar_content_active)
    for (let i = 0; i < menubar_content_active.length; i++) {
        let menubarContentActiveElement = menubar_content_active[i];
        // 为右侧菜单栏选项卡 下所有的 a标签 建立索引
        menubarContentActiveElement.dataset.index = i;
        // console.log(menubarContentActiveElement)

        // 监听右侧菜单栏选项卡 下所有的 a标签
        menubarContentActiveElement.addEventListener('click', function () {
            // console.log(i, Number(menubar_index))

            // 显示聊天框上中部内容，即显示与谁正在聊天
            let chatBoardHeaderInfo = document.querySelector('#chat-obj');
            chatBoardHeaderInfo.classList.remove('hidden-el');

            // 切换左侧消息窗口
            if (Number(menubar_index) !== 0) {  // 如果当前选项卡不是第一个默认右侧菜单栏选项卡
                for (let j = 0; j < menubar_content_active.length; j++) {
                    // 移除 右侧菜单栏下所有a标签的 class: content-active 属性
                    menubar_content_active[j].classList.remove('content-active');
                }

                for (let j = 0; j < msg_window_bars.length; j++) {
                    // 移除各个 左侧消息窗口 的 class: exclusive-chat-box-active 属性
                    msg_window_bars[j].classList.remove('exclusive-chat-box-active');
                }

                // 给 右侧菜单栏下点击的a标签 赋上 class: content-active 属性
                this.classList.add('content-active');
                // 再为右侧菜单栏 所对应索引位置的 左侧消息窗口 添加 class: exclusive-chat-box-active 属性
                msg_window_bars[Number(menubar_index)].classList.add('exclusive-chat-box-active');

                // 如果是最后一张选项卡：更多功能，则需要切换左侧消息窗口为 功能选项卡
                if (Number(menubar_index) === menubars.length - 1) {
                    // 获取 功能消息窗口 列表
                    let entity_li = document.querySelectorAll('#chat-box #message .exclusive-chat-box .entity-li');
                    // console.log(entity_li)
                    for (let j = 0; j < entity_li.length; j++) {
                        // 移除 功能消息窗口 列表中各个功能消息的 class: entity-li-active属性
                        entity_li[j].classList.remove('entity-li-active')
                    }
                    // 给 右侧导航栏下点击的a标签 对应索引位置上的 功能消息窗口 赋上 class: entity-li-active属性
                    entity_li[i].classList.add('entity-li-active');

                    // 有无结果都要清空
                    document.querySelector('.non-search-result').classList.add('hidden-el');
                    document.querySelector('.has-search-result').classList.add('hidden-el');

                } else {
                    // 左侧消息窗口 的 默认窗口 状态 将被移除
                    msg_window_bars[0].classList.remove('exclusive-chat-box-active');

                    // 启用发送消息的textarea和发送button
                    sendBtn.disabled = false;
                    content.disabled = false;
                }
                scrollBoard();   // 需要下拉至最底部
            }
        })
    }
};

/**
 * 更多功能栏：
 * @type {Element}
 */
// 监听 更多功能栏 的textarea
var more_entities_textarea = document.querySelectorAll('#chat-box #message #entity .entity-li textarea');
for (let i = 0; i < more_entities_textarea.length; i++) {
    let entities_textarea = more_entities_textarea[i];
    entities_textarea.addEventListener('change', function () {
        checkContentLength(this, 60);
    });
    entities_textarea.addEventListener('keydown', function () {
        checkContentLength(this, 60);
    });
    entities_textarea.addEventListener('keyup', function () {
        checkContentLength(this, 60);
    });
}

// 监听 更多功能栏 的input[type="text"]
var more_entities_input = document.querySelectorAll('#chat-box #message #entity .entity-li input[type="text"]');
for (let i = 0; i < more_entities_input.length; i++) {
    let entities_input = more_entities_input[i];
    entities_input.addEventListener('change', function () {
        checkContentLength(this, 30);
    });
    entities_input.addEventListener('keydown', function () {
        checkContentLength(this, 30);
    });
    entities_input.addEventListener('keyup', function () {
        checkContentLength(this, 30);
    });
};

// 监听 添加好友（搜索好友） 的button
var searchFriendBtn = document.querySelector('#add-friend button');
if (searchFriendBtn) {
    searchFriendBtn.addEventListener('click', function (evt) {
        let textElem = document.querySelector('#add-friend input[type="text"]');
        let msgVal = textElem.value;
        if (msgVal === '' | msgVal.length === 0) {
            callMessage(1, "搜索内容不能为空！");
            return;
        }

        textElem.value = '';        // 内容框置为空
        $.ajax({
            url: getProjectPath() + '/user/find-friend',
            type: 'POST',
            data: doSingleDataToJson(msgVal),
            success: function (resp) {
                console.log(resp)
                // 成功处理逻辑

                let rootElem = document.querySelector('#add-friend');
                let hasResultElem = rootElem.querySelector('.has-search-result');
                let hasNonResultElem = rootElem.querySelector('.non-search-result');
                if (resp.code === 0) {
                    callMessage(resp.code, resp.msg);

                    let friendInfoTempElems = hasResultElem.querySelectorAll('.friend-info');
                    if (friendInfoTempElems)
                        for (let i = 0; i < friendInfoTempElems.length; i++)
                            friendInfoTempElems[i].remove();

                    // 可能会存在多个用户
                    let findFriends = resp.data;
                    if (findFriends.length > 0) {
                        hasNonResultElem.classList.add('hidden-el');
                        hasResultElem.classList.remove('hidden-el');

                        for (let i = 0; i < findFriends.length; i++) {
                            let findFriend = findFriends[i];

                            let friendInfoElem = document.createElement('div');
                            friendInfoElem.classList.add('friend-info');

                            let avatarElem = document.createElement('div');
                            avatarElem.classList.add('avatar');
                            avatarElem.style.background = 'url(' + getProjectPath() + '/images' + findFriend.avatarUrl + ') no-repeat';

                            let friendNameElem = document.createElement('div');
                            friendNameElem.classList.add('friend-name', 'high-light');
                            friendNameElem.innerText = findFriend.nickname + ", " + findFriend.account;

                            let emailElem = document.createElement('div');
                            emailElem.classList.add('app-content');
                            // emailElem.innerText = resp.data.account + ' & ' + resp.data.email;
                            emailElem.innerText = findFriend.email;

                            let addBtnElem = document.createElement('div');
                            addBtnElem.classList.add('add-btn');
                            let aElem = document.createElement('a');
                            // 为a标签设置自定义属性 gohref：发送的请求
                            aElem.dataset.gohref = getProjectPath() + '/user/add-friend/' + findFriend.uId;
                            aElem.classList.add('add-friend-btn');
                            aElem.innerText = '点击添加好友';
                            addBtnElem.appendChild(aElem);
                            addBtnElem.addEventListener('click',  () => {
                                doAddFriendBtnListener(evt, addBtnElem);    // 绑上监听点击事件（添加好友）
                            });

                            friendInfoElem.append(avatarElem, friendNameElem, emailElem, addBtnElem);
                            hasResultElem.appendChild(friendInfoElem);
                        }

                    } else {
                        hasNonResultElem.classList.remove('hidden-el');
                        hasResultElem.classList.add('hidden-el');
                        rootElem.querySelector('p').innerText = resp.msg;
                    }

                } else {
                    callMessage(resp.code, resp.msg);
                    hasNonResultElem.classList.remove('hidden-el');
                    hasResultElem.classList.add('hidden-el');
                    rootElem.querySelector('p').innerText = resp.msg;
                }
            },
            error: function (resp) {
                console.log(resp)
                // 发生错误时处理逻辑
                callMessage(-1, "***哎呀出错啦，请稍后再试吧！");
                // sleep(sleepTime).then(()=> window.location.href = getProjectPath() + '/error');
            }
        });
    });
}
// 为 点击添加好友 的a(class: add-friend-btn)标签绑上监听事件
function doAddFriendBtnListener(evt, addFriendBtnElem) {
    evt.preventDefault();
    let addFriendUrl = addFriendBtnElem.querySelector('a[class="add-friend-btn"]').dataset.gohref;   // /chatroom/user/add-friend/?
    // console.log(addFriendUrl)
    sendUrl(addFriendUrl, 'POST', null, getProjectPath() + '/main');
}

// 监听 加入群聊（搜索群聊） 的button
var enterGroupBtn = document.querySelector('#enter-group button');
if (enterGroupBtn) {
    enterGroupBtn.addEventListener('click', function (evt) {
        let textElem = document.querySelector('#add-friend input[type="text"]');
        // sendMessageToUrl(textElem);
        // sendMessageToUrl(textElem, getProjectPath() + '/user/enterGroup', '/main');
        let msgVal = textElem.value;
        if (msgVal === '' | msgVal.length === 0) {
            callMessage(1, "搜索内容不能为空！");
            return;
        }

        textElem.value = '';        // 内容框置为空
        $.ajax({
            url: getProjectPath() + '/user/findGroup',
            type: 'POST',
            data: doSingleDataToJson(msgVal),
            success: function (resp) {
                console.log(resp)
                // 成功处理逻辑

                let rootElem = document.querySelector('#enter-group');
                if (resp.code === 0) {
                    callMessage(0, resp.msg);
                    rootElem.querySelector('.non-search-result').classList.add('hidden-el');
                    rootElem.querySelector('.has-search-result').classList.remove('hidden-el');

                    // 可能查询结果有多个群组信息
                    // 先移除group-info节点
                    if (rootElem.querySelector('.has-search-result').querySelector('.group-info'))
                        rootElem.removeChild(rootElem.querySelector('.has-search-result').querySelector('.group-info'));

                    // 再创建节点
                    let groupInfo = document.createElement('div');
                    groupInfo.classList.add('groupInfo');

                    for (let i = 0; i < resp.data.length; i++) {
                        let groupNameElem =  document.createElement('div');
                        groupNameElem.classList.add('group-name', 'high-light', 'ellipsis');
                        groupNameElem.innerText = resp.data.groupName;
                        groupInfo.appendChild(groupNameElem);

                        let createTimeElem = document.createElement('div');
                        createTimeElem.classList.add('app-content');
                        createTimeElem.innerText = resp.data.createTime;
                        groupInfo.appendChild(createTimeElem);

                        let groupCodeElem = document.createElement('div');
                        groupCodeElem.classList.add('group-code');
                        groupCodeElem.innerText = resp.data.gCode;
                        groupInfo.appendChild(groupCodeElem);

                        let addBtnElem = document.createElement('div');
                        addBtnElem.classList.add('add-btn');
                        let aElem = document.createElement('a');
                        aElem.href = '/user/enterGroup/' + resp.data.gId;
                        aElem.innerText = '点击加入群聊';
                        addBtnElem.appendChild(aElem);
                        groupInfo.appendChild(addBtnElem);

                        // 最后再成为此节点的子节点
                        rootElem.appendChild(groupInfo);
                    }

                    if (typeof (successCallbackUrl) != "undefined" && successCallbackUrl)
                        sleep(sleepTime).then(() => window.location.href = successCallbackUrl);

                } else {
                    callMessage(resp.code, resp.msg);
                    rootElem.querySelector('.non-search-result').classList.remove('hidden-el');
                    rootElem.querySelector('.has-search-result').classList.add('hidden-el');

                    rootElem.querySelector('p').innerText = resp.msg;
                    if (typeof (failedCallbackUrl) != "undefined" && failedCallbackUrl)
                        sleep(sleepTime).then(() => window.location.href = failedCallbackUrl);
                }
            },
            error: function (resp) {
                console.log(resp)
                // 发生错误时处理逻辑
                callMessage(-1, "***哎呀出错啦，请稍后再试！");
                // sleep(sleepTime).then(()=> window.location.href = getProjectPath() + '/error');
            }
        });
    });
}

// 监听 创建群聊 的button
var createGroupBtn = document.querySelector('#add-group button');
if (createGroupBtn) {
    createGroupBtn.addEventListener('click', function (evt) {
        let textElem = document.querySelector('#add-group input[type="text"]');
        sendMessageToUrl(textElem);
    });
}

// 封装单个 data 为Json数据
function doSingleDataToJson(data) {
    let formData = {
        'data': data
    };
    return JSON.parse(JSON.stringify(formData));
}

// 功能栏的 提交材料信息 封装函数
function sendMessageToUrl(elem, url, successUrl, failedUrl) {
    let msgVal = elem.value;
    if (msgVal === '' | msgVal.length === 0) {
        callMessage(1, "发送内容不能为空！");
        return;
    }

    elem.value = '';        // 内容框置为空
    // console.log(typeof doSingleDataToJson(msgVal))
    // 发送url，调用：function sendUrl(url, type, data, successCallbackUrl, failedCallbackUrl)
    sendUrl(url, 'POST', doSingleDataToJson(msgVal), successUrl, failedUrl);
};

// 监听 我的好友列表（获取本人与对方的历史私聊消息）
var myFriendListElem = document.querySelector('#menu-bar #my-friend-list');
if (myFriendListElem) {
    let myFriends = myFriendListElem.querySelectorAll('a[class="my-friend"]');
    for (let i = 0; i < myFriends.length; i++) {
        let myFriend = myFriends[i];
        myFriend.addEventListener('click', (evt) => {
            // console.log(myFriend)
            evt.preventDefault();

            // 设置 聊天框 上中部位置显示的名称：显示私聊消息对方的名称
            let chatBoardHeaderElem = document.querySelector('#chat-obj');
            // chatBoardHeaderElem.classList.remove('hidden-el');
            chatBoardHeaderElem.innerHTML = `正在与 <span class="show-name">` + myFriend.innerText + `</span> 聊天`;

            // 获取点击好友的 data-id 属性（即好友的用户id）
            let friendId = myFriend.dataset.id;
            // console.log(friendId);
            // console.log(myFriend.dataset.hasGetted);

            // 若尚未获取过本人与对方的历史私聊消息
            if (typeof (myFriend.dataset.hasGetted) === 'undefined') {
                let priMsgElem = document.querySelector('#chat-box #message #private-message');
                // 发送ajax请求，获取本人与好友的私聊消息列表
                $.ajax({
                    url: getProjectPath() + '/user/chat/private-history-msg/' + friendId,
                    type: 'GET',
                    success: function (resp) {
                        // console.log(resp)
                        // 成功处理逻辑

                        if (resp.code === 0) {
                            // 渲染数据
                            let priMsgList = resp.data;
                            for (let i = 0; i < priMsgList.length; i++) {
                                let priMsg = priMsgList[i];
                                // console.log(priMsg);
                                setMessageToPriMsgBoard(priMsg, friendId);
                            }

                            // console.log(priMsgElem);
                            // 设置一个自定义属性用于：记录已经获取过本人与该好友的历史消息了
                            myFriend.dataset.hasGetted = true;
                        }
                    },
                    error: function (resp) {
                        console.log('Error: ' + resp)
                    }
                });
            }

            // 设置一个自定义属性用于：记录已经获取过本人与该好友的历史消息了
            // myFriend.dataset.hasGetted = true;
        });
    }
}

// 将私聊消息 添加至私聊板块上
function setMessageToPriMsgBoard(priMsg, friendId) {
    let priMsgElem = document.querySelector('#chat-box #message #private-message');
    let msgElem = document.createElement('div');
    msgElem.classList.add('msg');

    let uIdElem = document.createElement('div');
    uIdElem.classList.add('hidden-el');

    let avatarElem = document.createElement('div');
    avatarElem.classList.add('avatar');

    let nameElem = document.createElement('span');
    nameElem.classList.add('name', 'ellipsis');

    let sentenceElem = document.createElement('div');
    sentenceElem.classList.add('sentence');
    let textElem = document.createElement('p');
    textElem.classList.add('text');
    let triangleElem = document.createElement('div');
    triangleElem.classList.add('triangle');
    let timeElem = document.createElement('p');
    timeElem.classList.add('time');

    // 如果消息的发送者是对方（好友），渲染数据为 rece_msg（本人接收对方的消息）
    // console.log(priMsg.sendUser.uId, friendId, Number(priMsg.sendUser.uId === Number(friendId));
    if (Number(priMsg.sendUser.uId) === Number(friendId)) {
        msgElem.classList.add('rece_msg');
        uIdElem.classList.add('rece_uid');
        avatarElem.classList.add('rece_avatar');

    } else {
        // 消息发送者是本人，则渲染数据为 send_msg（对方接收本人发送的消息）
        msgElem.classList.add('send_msg');
        uIdElem.classList.add('send_uid');
        avatarElem.classList.add('send_avatar');
    }

    uIdElem.innerText = priMsg.sendUser.uId;

    avatarElem.style.background = 'url(' + getProjectPath() + '/images' +  priMsg.sendUser.avatarUrl +') no-repeat';

    nameElem.innerText = priMsg.sendUser.nickname;

    textElem.innerText = priMsg.content;
    let sendTime = new Date(priMsg.sendTime);
    timeElem.innerText = sendTime.getFullYear() + "年" + (sendTime.getMonth() + 1) + "月" + sendTime.getDate() + "日 "
        + sendTime.getHours() + "时" + sendTime.getMinutes() + "分" + sendTime.getSeconds() + "秒";

    sentenceElem.append(textElem, triangleElem, timeElem);
    msgElem.append(uIdElem, avatarElem, nameElem, sentenceElem);
    priMsgElem.appendChild(msgElem);
    scrollBoard();
}

// 删除好友
function delFriend(friendId, nickname) {
    // 显示弹窗提醒
    let pElem = document.createElement('p');
    pElem.innerHTML = `请您确认是否需要删除uId为：` + friendId + `，昵称为：` + nickname + ` 的好友信息？<br />
                <button class="opt-cancel" onclick="doFinalDelFriend(true)">容我再想想？</button>
                <button onclick="doFinalDelFriend(false, ` + friendId + `)">确认删除</button>`;
    showModal(pElem);
}

function doFinalDelFriend(isCancelled, friendId) {
    hideModal();
    if (!isCancelled) {
        sendUrl(getProjectPath() + '/user/del-friend/' + friendId, 'POST', null, getProjectPath() + "/main");
    } else {
        callMessage(1, "已取消操作");
    }
}

// 监听 用户会话注销按钮
var userExitBtn = document.querySelector('#user-exit button');
if (userExitBtn) {
    userExitBtn.addEventListener('click', () => {
        sendUrl(getProjectPath() + '/user/logout', 'GET',
            null, getProjectPath() + '/login', getProjectPath() + '/main')
    })
}

// 用户账号注销
function logoutAccount() {
    // 显示弹窗提醒
    let pElem = document.createElement('p');
    pElem.innerHTML = `请确认是否需要注销账号！<br />
                <button class="opt-cancel" onclick="doFinalLogoutAccount(true)">容我再想想？</button>
                <button onclick="doFinalLogoutAccount(false)">确认注销</button>`;
    showModal(pElem);
}
function doFinalLogoutAccount(isCancelled) {
    hideModal();
    if (!isCancelled) {
        sendUrl(getProjectPath() + '/user/logout-account', 'GET',
            null, getProjectPath() + '/login', getProjectPath() + '/main');

    } else {
        callMessage(1, "操作已取消！");
    }
}

// 监听右侧菜单栏下的 发布系统广播 标签（获取该管理员用户发布过的所有的系统广播信息，然后将其展示在左侧消息窗口中）
// document.getElementById('all-broadcasts').addEventListener('click', (evt) => {
function doGetAllBroadcasts() {
    $.ajax({
        url: getProjectPath() + '/entity/admin-published-broadcasts',
        type: 'GET',
        success: (resp) => {
            console.log(resp);
            let hasBroadcastElem = document.querySelector('#publish-system-broadcast .has-search-result');
            let hasNonBroadcastElem = document.querySelector('#publish-system-broadcast .non-search-result');

            if (resp.code === 0) {
                let publishedBroadcasts = resp.data;
                console.log(publishedBroadcasts);
                let myPubBroadcastPreviewElem = document.querySelector('#publish-system-broadcast #broadcast-preview');
                if (publishedBroadcasts.length === 0) {
                    hasBroadcastElem.classList.add('hidden-el');
                    hasNonBroadcastElem.classList.remove('hidden-el');
                    hasNonBroadcastElem.firstElementChild.innerText = '您暂未发布任何系统公告！';

                } else {
                    hasBroadcastElem.classList.remove('hidden-el');
                    hasNonBroadcastElem.classList.add('hidden-el');
                    if (typeof (myPubBroadcastPreviewElem.dataset.hasGetted) === 'undefined') {
                        for (let i = 0; i < publishedBroadcasts.length; i++) {
                            // 展示到自己的广播内容列表下
                            setContentToOwnPublishedBroadcast(publishedBroadcasts[i]);
                        }
                        myPubBroadcastPreviewElem.dataset.hasGetted = true;
                    }
                }
            } else {
                callMessage(resp.code, resp.msg)
                hasBroadcastElem.classList.add('hidden-el');
                hasNonBroadcastElem.classList.remove('hidden-el');
                hasNonBroadcastElem.querySelector('#publish-system-broadcast .non-search-result p').innerText = `您暂未发布任何系统公告！`;
            }
        },
        error: (resp) => {
            console.log("Error: " + resp)
        }
    })
};
// 将用户发表的广播内容展示出来
function setContentToOwnPublishedBroadcast(publishedBroadcast) {
    let myPublishedBroadcastElem = document.createElement('div');
    myPublishedBroadcastElem.classList.add('my-pub-broadcast');

    let avatarElem = document.createElement('div');
    avatarElem.classList.add('avatar');
    avatarElem.style.background = 'url(' + getProjectPath() + '/images' + publishedBroadcast.user.avatarUrl + ') no-repeat';

    let friendNameElem = document.createElement('div')
    friendNameElem.classList.add('friend-name', 'high-light', 'ellipsis');
    friendNameElem.innerText = publishedBroadcast.user.nickname;

    let timeElem = document.createElement('div');
    timeElem.classList.add('apply-time');
    timeElem.innerText = FormatDate(publishedBroadcast.sendTime);

    let broadcastContentElem = document.createElement('div');
    broadcastContentElem.classList.add('broadcast-content');
    broadcastContentElem.innerText = publishedBroadcast.content;

    myPublishedBroadcastElem.append(avatarElem, friendNameElem, timeElem, broadcastContentElem);
    let broadcastPreviewElem = document.querySelector('#publish-system-broadcast #broadcast-preview');
    let hasFristElem = broadcastPreviewElem.firstElementChild;
    if (hasFristElem) {
        broadcastPreviewElem.insertBefore(myPublishedBroadcastElem, hasFristElem);
    } else {
        broadcastPreviewElem.appendChild(myPublishedBroadcastElem);
    }
}

// 监听右侧菜单栏下的 意见反馈 标签（获取所有的意见反馈信息，然后将其展示在左侧消息窗口中）
document.getElementById('all-feedbacks').addEventListener('click', (evt) => {
    evt.preventDefault();
    $.ajax({
        url: getProjectPath() + '/entity/all-feedbacks',
        type: 'GET',
        success: (resp) => {
            console.log(resp);
            let feedbackList = resp;
            let hasFeedback = document.querySelector('#feedback .has-search-result');
            let hasNonFeedback = document.querySelector('#feedback .non-search-result');
            let feedbackPreviewElem = document.querySelector('#all-feedback-list .feedback-preview');
            if (feedbackList.length === 0) {
                hasFeedback.classList.add('hidden-el');
                hasNonFeedback.classList.remove('hidden-el');
                hasNonFeedback.querySelector('#feedback .non-search-result p').innerText = `暂无更多反馈信息！`;

            } else {
                hasFeedback.classList.remove('hidden-el');
                hasNonFeedback.classList.add('hidden-el');
                if (typeof (feedbackPreviewElem.dataset.hasGetted) === 'undefined') {
                    for (let i = 0; i < feedbackList.length; i++) {
                        setContentToFeedbackList(feedbackList[i]);
                    }
                    feedbackPreviewElem.dataset.hasGetted = true;
                }
            }
        },
        error: (resp) => {
            console.log("Error: " + resp)
        }
    })
});
// 插入内容至意见反馈栏（默认插入到行首）
function setContentToFeedbackList(feedbackContent) {
    let feedbackPreviewContentElem = document.createElement('div');
    feedbackPreviewContentElem.classList.add('feedback-preview-content');

    let avatarElem = document.createElement('div');
    avatarElem.classList.add('avatar');
    avatarElem.style.background = 'url(' + getProjectPath() + '/images' + feedbackContent.user.avatarUrl + ') no-repeat';

    let friendNameElem = document.createElement('div')
    friendNameElem.classList.add('friend-name', 'high-light', 'ellipsis');
    friendNameElem.innerText = feedbackContent.user.nickname;

    let timeElem = document.createElement('div');
    timeElem.classList.add('apply-time');
    timeElem.innerText = FormatDate(feedbackContent.publishTime);

    let feedbackContentElem = document.createElement('div');
    feedbackContentElem.classList.add('feedback-content');
    feedbackContentElem.innerText = feedbackContent.fbContent;

    feedbackPreviewContentElem.append(avatarElem, friendNameElem, timeElem, feedbackContentElem);
    let feedbackPreviewElem = document.querySelector('#all-feedback-list .feedback-preview');
    let hasFristElem = feedbackPreviewElem.firstElementChild;
    if (hasFristElem) {
        feedbackPreviewElem.insertBefore(feedbackPreviewContentElem, hasFristElem);
    } else {
        feedbackPreviewElem.appendChild(feedbackPreviewContentElem);
    }
}
// 监听 意见反馈 栏的提交按钮
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
                // 写入至意见反馈中
                setContentToFeedbackList(resp.data);
            } else {
                callMessage(resp.code, resp.msg);
            }
        },
        error: (resp) => {
            console.log(resp);
            callMessage(-1, "***出错啦，请稍后再试！");
        }
    });
    /*sendUrl(getProjectPath() + "/entity/send-feedback", 'POST', JSON.parse(JSON.stringify(data)),
        getProjectPath() + "/main");
    feedbackContent.value = '';*/
});

// 消息的右键菜单栏
/*
var interMsg = document.querySelector('#chat-box #message .inter-msg');
var more_opt_submenu = document.querySelector('#chat-box #message .inter-msg .more_opt_submenu ul');
interMsg.addEventListener("contextmenu", function (evt) {
    evt.preventDefault(); // 阻止事件传播
    more_opt_submenu.style.display = "block";   // 显示

    var x = evt.clientX;
    var y = evt.clientY;

    var diffX = document.documentElement.clientWidth - more_opt_submenu.offsetWidth;
    var diffY = document.documentElement.clientHeight - more_opt_submenu.offsetHeight;
    // 以免出边界
    x = x >= diffX ? diffX : x;
    y = y >= diffY ? diffY : y;

    list.style.left = x + "px";
    list.style.top = y + "px";
});
document.addEventListener("click", () => more_opt_submenu.style.display = "none");*/
