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

var getOnlineCounts = setInterval(() => {
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
                    let nonResults = document.querySelectorAll('.non-search-result');
                    let hasResults = document.querySelectorAll('.has-search-result');
                    for (let i = 0; i < nonResults.length; i++) {
                        nonResults[i].classList.add('hidden-el');
                    };
                    for (let i = 0; i < hasResults.length; i++) {
                        hasResults[i].classList.add('hidden-el');
                    };

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
                console.log(resp);
                callMessage(resp.code, resp.msg);
                // 成功处理逻辑

                let rootElem = document.querySelector('#add-friend');
                let hasResultElem = rootElem.querySelector('.has-search-result');
                let hasNonResultElem = rootElem.querySelector('.non-search-result');
                let isEmptyResult = true;

                if (resp.code === 0) {

                    let friendInfoTempElems = hasResultElem.querySelectorAll('.friend-info');
                    if (friendInfoTempElems)
                        for (let i = 0; i < friendInfoTempElems.length; i++)
                            friendInfoTempElems[i].remove();

                    // 可能会存在多个用户
                    let findFriends = resp.data;
                    if (findFriends && findFriends.length > 0) {
                        hasNonResultElem.classList.add('hidden-el');
                        hasResultElem.classList.remove('hidden-el');
                        isEmptyResult = false;

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

                    }
                }
                if (isEmptyResult){
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
        let textElem = document.querySelector('#enter-group input[type="text"]');
        let msgVal = textElem.value;
        if (msgVal === '' | msgVal.length === 0) {
            callMessage(1, "搜索内容不能为空！");
            return;
        }

        $.ajax({
            url: getProjectPath() + '/user/find-group',
            type: 'POST',
            data: doSingleDataToJson(msgVal),
            success: function (resp) {
                console.log(resp)
                // 成功处理逻辑
                callMessage(resp.code, resp.msg);

                let rootElem = document.querySelector('#enter-group');
                let hasResultElem = rootElem.querySelector('.has-search-result');
                let nonResultElem = rootElem.querySelector('.non-search-result');
                let isEmptyResult = true;

                if (resp.code === 0) {
                    let findGroups = resp.data;
                    if (findGroups && findGroups.length > 0) {
                        isEmptyResult = false;

                        nonResultElem.classList.add('hidden-el');
                        hasResultElem.classList.remove('hidden-el');
                        let groupInfoElems = rootElem.querySelectorAll('#groups-info .group-info');
                        for (let i = 0; i < groupInfoElems.length; i++)
                            groupInfoElems[i].remove(); // 先移除所有的group-info节点

                        let groupsInfoElem = rootElem.querySelector('.has-search-result #groups-info');
                        // 可能查询结果有多个群组信息
                        for (let i = 0; i < findGroups.length; i++) {
                            let findGroup = findGroups[i];

                            let groupInfo = document.createElement('div');
                            groupInfo.classList.add('group-info');

                            let groupNameElem =  document.createElement('div');
                            groupNameElem.classList.add('group-name');
                            groupNameElem.innerText = findGroup.gName;
                            groupInfo.appendChild(groupNameElem);

                            let createTimeElem = document.createElement('div');
                            createTimeElem.classList.add('create-time');
                            createTimeElem.innerText = FormatDate(findGroup.createTime);
                            groupInfo.appendChild(createTimeElem);

                            let groupCodeElem = document.createElement('div');
                            groupCodeElem.classList.add('group-code');
                            groupCodeElem.innerText = findGroup.gCode;
                            groupInfo.appendChild(groupCodeElem);

                            let addBtnElem = document.createElement('div');
                            addBtnElem.classList.add('add-btn');
                            let aElem = document.createElement('a');
                            // aElem.href = '/user/enterGroup/' + findGroup.gCode;
                            aElem.innerText = '点击加入群聊';
                            addBtnElem.appendChild(aElem);
                            // addBtnElem.addEventListener('click', doEnterGroupEvtListener(findGroup.gCode));
                            addBtnElem.addEventListener('click', (evt) => {
                                evt.preventDefault();
                                // 监听点击加入群聊的按钮
                                sendUrl(getProjectPath() + '/user/enter-group/' + findGroup.gCode, 'GET');
                            });

                            groupInfo.appendChild(addBtnElem);
                            // 最后再成为此节点的子节点
                            /*let firstElem = groupsInfoElem.firstElementChild;
                            if (firstElem) {
                                groupsInfoElem.insertBefore(groupInfo, firstElem);
                            } else {
                                groupsInfoElem.appendChild(groupInfo);
                            }*/
                            groupsInfoElem.appendChild(groupInfo);
                        }
                    }
                }

                if (isEmptyResult) {
                    nonResultElem.classList.remove('hidden-el');
                    nonResultElem.querySelector('p').innerText = resp.msg;
                    hasResultElem.classList.add('hidden-el');
                }
            },
            error: function (resp) {
                console.log(resp)
                // 发生错误时处理逻辑
                callMessage(-1, "***哎呀出错啦，请稍后再试！");
                // sleep(sleepTime).then(()=> window.location.href = getProjectPath() + '/error');
            }
        });
        textElem.value = '';        // 内容框置为空
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

        // 为左侧消息窗口设置右侧菜单栏下对应的消息窗口
        let correspondingElem = document.createElement('div');
        correspondingElem.dataset.id = myFriend.dataset.id;         // 设置用户好友id，以分辨不同的好友
        correspondingElem.classList.add('hidden-el', 'corresponding');

        document.querySelector('#chat-box #message #private-message').appendChild(correspondingElem);      // 将其添加至群聊板中
    }

    for (let i = 0; i < myFriends.length; i++) {
        let myFriend = myFriends[i];

        myFriend.addEventListener('click', (evt) => {
            // console.log(myFriend)
            evt.preventDefault();

            let correspondingElems = document.querySelectorAll('#chat-box #message #private-message .corresponding');
            for (let i = 0; i < correspondingElems.length; i++) {
                let correspondingElem = correspondingElems[i];
                if (Number(correspondingElem.dataset.id) === Number(correspondingElem.dataset.id)) {
                    correspondingElem.classList.remove('hidden-el');
                } else {
                    correspondingElem.classList.add('hidden-el');
                }
            }

            // 设置 聊天框 上中部位置显示的名称：显示私聊消息对方的名称
            let chatBoardHeaderElem = document.querySelector('#chat-obj');
            // chatBoardHeaderElem.classList.remove('hidden-el');
            chatBoardHeaderElem.innerHTML = `正在与 <span class="show-name">` + myFriend.innerText + `</span> 聊天`;

            // 获取点击好友的 data-id 属性（即好友的用户id）
            let friendId = myFriend.dataset.id;
            console.log('friendId: ', friendId);
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
        })
    }
}

// 将私聊消息 添加至私聊板块上
function setMessageToPriMsgBoard(priMsg, friendId) {
    // 查找群聊聊天板下是否已经有过其它的群聊组信息
    let correspondingElems = document.querySelectorAll('#chat-box #message #private-message .corresponding');
    for (let i = 0; i < correspondingElems.length; i++) {

        let correspondingElem = correspondingElems[i];
        if (Number(correspondingElem.dataset.id) === Number(friendId)) {
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
    }
}

// 查找我的新好友通知
function findMyNewFriends() {
    $.ajax({
        url: getProjectPath() + '/user/find-new-friends',
        type: 'GET',
        success: (resp) => {

            let rootElem = document.querySelector('#new-friend');
            let hasResultElem = rootElem.querySelector('.has-search-result');
            let hasNonResultElem = rootElem.querySelector('.non-search-result');
            let newFriends = resp;
            if (newFriends !== undefined && newFriends.length > 0) {
                hasResultElem.classList.remove('hidden-el');
                let applyFriendInfoElems = hasResultElem.querySelectorAll('.apply-friend');
                if (applyFriendInfoElems)
                    for (let i = 0; i < applyFriendInfoElems.length; i++)
                        applyFriendInfoElems[i].remove();

                hasNonResultElem.classList.add('hidden-el');

                for (let i = 0; i < newFriends.length; i++) {
                    let newFriend = newFriends[i].hostUser;
                    // console.log(newFriend);

                    let friendInfoElem = document.createElement('div');
                    friendInfoElem.classList.add('apply-friend');

                    let avatarElem = document.createElement('div');
                    avatarElem.classList.add('avatar');
                    avatarElem.style.background = 'url(' + getProjectPath() + '/images' + newFriend.avatarUrl + ') no-repeat';

                    let friendNameElem = document.createElement('div');
                    friendNameElem.classList.add('friend-name', 'high-light');
                    friendNameElem.innerText = newFriend.nickname;

                    let applyTimeElem = document.createElement('div');
                    applyTimeElem.classList.add('apply-time');

                    let appContentElem = document.createElement('div');
                    appContentElem.classList.add('app-content');

                    if (newFriends[i].fsStatus === '0') {
                        applyTimeElem.innerText = '结交时间：' + FormatDate(newFriends[i].applyTime);
                        appContentElem.innerText = '已同意好友申请！';

                    } else if (newFriends[i].fsStatus === '1') {   // 正处于好友关系确认中
                        applyTimeElem.innerText = '申请时间：' + FormatDate(newFriends[i].applyTime);
                        let aElem = document.createElement('a');
                        // 为a标签设置自定义属性 gohref：发送的请求
                        aElem.dataset.gohref = getProjectPath() + '/user/add-friend/' + newFriend.uId;
                        aElem.classList.add('add-friend-btn');
                        aElem.innerText = '点击同意好友申请！'

                        let addBtnElem = document.createElement('span');
                        addBtnElem.appendChild(aElem);
                        addBtnElem.classList.add('agree');
                        addBtnElem.addEventListener('click',  (evt) => {
                            doAddFriendBtnListener(evt, addBtnElem);    // 绑上监听点击事件（添加好友）
                        });

                        appContentElem.innerText = `请求添加好友，`;
                        appContentElem.appendChild(addBtnElem);
                    }

                    friendInfoElem.append(avatarElem, friendNameElem, applyTimeElem, appContentElem);
                    let hasFirstResultElem = hasResultElem.firstElementChild;
                    if (hasFirstResultElem) {
                        hasResultElem.insertBefore(friendInfoElem, hasFirstResultElem);

                    } else {
                        hasResultElem.appendChild(friendInfoElem);
                    }
                }
            } else {
                hasResultElem.classList.add('hidden-el');
                hasNonResultElem.classList.remove('hidden-el');
                hasNonResultElem.firstElementChild.innerText = '暂无更多好友请求信息';
            }
        }
    })
}

// 删除好友
function delFriend(friendId, nickname) {
    // 显示弹窗提醒
    let pElem = document.createElement('p');
    pElem.innerHTML = `请您确认是否需要删除uId为：` + friendId + `，昵称为：` + nickname + ` 的好友信息？<br />
                <button class="opt-cancel" onclick="doFinalDelFriend(true)">容我想想？</button>
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
                <button class="opt-cancel" onclick="doFinalLogoutAccount(true)">容我想想？</button>
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
            let feedbackPreviewElems = document.querySelectorAll('#all-feedback-list .feedback-preview .feedback-preview-content');
            for (let i = 0; i < feedbackPreviewElems.length; i++) {
                feedbackPreviewElems[i].remove();
            }

            if (feedbackList.length === 0) {
                hasFeedback.classList.add('hidden-el');
                hasNonFeedback.classList.remove('hidden-el');
                hasNonFeedback.querySelector('#feedback .non-search-result p').innerText = `暂无更多反馈信息！`;

            } else {
                hasFeedback.classList.remove('hidden-el');
                hasNonFeedback.classList.add('hidden-el');
                /*if (typeof (feedbackPreviewElem.dataset.hasGetted) === 'undefined') {
                    for (let i = 0; i < feedbackList.length; i++) {
                        setContentToFeedbackList(feedbackList[i]);
                    }
                    feedbackPreviewElem.dataset.hasGetted = true;
                }*/
                for (let i = 0; i < feedbackList.length; i++) {
                    setContentToFeedbackList(feedbackList[i]);
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

// 查询我创建的所有群组、以及这些群组中所有的用户信息
function fingMyCreatedGroups() {
    $.ajax({
        url: getProjectPath() + '/user/find-my-groups',
        type: 'GET',
        success: (resp) => {
            let rootElem = document.getElementById('my-created-groups');
            let hasResultElem = rootElem.querySelector('.has-search-result');
            let nonResultElem = rootElem.querySelector('.non-search-result');

            let myGroups = resp;
            // console.log(myGroups);
            if (myGroups && myGroups.length > 0) {
                hasResultElem.classList.remove('hidden-el');
                let groupInfoElems = hasResultElem.querySelectorAll('.group');
                if (groupInfoElems)
                    for (let i = 0; i < groupInfoElems.length; i++)
                        groupInfoElems[i].remove();
                nonResultElem.classList.add('hidden-el');

                for (let i = 0; i < myGroups.length; i++) {
                    let myGroup = myGroups[i];  // 群组
                    setGroupUsersInfo(myGroup);
                }

            } else {
                hasResultElem.classList.add('hidden-el');
                nonResultElem.classList.remove('hidden-el');
            }
        },
        error: (resp) => {
            console.log("Error: " + resp);
        }
    })
}
// 设置群组以及该群组下所有成员的信息（是否为群组新增）
function setGroupUsersInfo(myGroup) {

    let createdGroupElem = document.querySelector('#my-created-groups .has-search-result #created-group');

    let groupElem = document.createElement('div');
    groupElem.classList.add('group');

    let groupNameElem = document.createElement('p');
    groupNameElem.classList.add('group-name');
    groupNameElem.innerText = myGroup.gName;

    let myGroupUsersElem = document.createElement('div');
    myGroupUsersElem.classList.add('my-group-members');
    for (let i = 0; i < myGroup.members.length; i++) {
        let groupUser = myGroup.members[i];   // 群成员(GroupUser类型)
        let member = groupUser.member;        // 群成员（User类型）

        let friendInfoElem = document.createElement('div');
        friendInfoElem.classList.add('apply-friend');

        let avatarElem = document.createElement('div');
        avatarElem.classList.add('avatar');
        avatarElem.style.background = 'url(' + getProjectPath() + '/images' + member.avatarUrl + ') no-repeat';

        let friendNameElem = document.createElement('div');
        friendNameElem.classList.add('friend-name', 'high-light');

        let appContentElem = document.createElement('div');
        appContentElem.classList.add('app-content');
        appContentElem.innerText = member.email;

        let applyTimeElem = document.createElement('div');
        applyTimeElem.classList.add('apply-time');

        let dropBtnElem = document.createElement('div');
        dropBtnElem.classList.add('drop-btn');
        let aElem = document.createElement('a');
        dropBtnElem.appendChild(aElem);

        if (Number(member.uId) === Number(myGroup.hostUser.uId)) {
            // 该成员是群主
            applyTimeElem.innerHTML = '群聊创建时间：<br />' + FormatDate(myGroup.createTime);

            friendNameElem.innerText = '（群主）';

            aElem.innerText = '解散群聊';

            dropBtnElem.addEventListener('click',  () => {
                // console.log('解散群聊', myGroup.gCode);
                // 绑上监听点击事件（解散群聊）
                doDissolveGroupBtnListener(myGroup.gName, myGroup.gCode);
            });

        } else {
            // 仅是群成员
            applyTimeElem.innerHTML = '入群时间：<br />' + FormatDate(groupUser.joinTime);

            aElem.innerText = '点击踢出群聊';

            friendNameElem.innerText = member.nickname;

            dropBtnElem.addEventListener('click',  () => {
                // console.log('踢出群聊', myGroup.gCode, member.uId);
                // 绑上监听点击事件（踢出群聊）
                doDropMemberFromGroupBtnListener(myGroup, member);
            });
        }

        friendInfoElem.append(avatarElem, friendNameElem, appContentElem, applyTimeElem, dropBtnElem);
        myGroupUsersElem.appendChild(friendInfoElem);
        groupElem.append(groupNameElem, myGroupUsersElem);
        let firstGroupElem = createdGroupElem.firstElementChild;
        if (firstGroupElem) {
            createdGroupElem.insertBefore(groupElem, firstGroupElem);

        } else {
            createdGroupElem.appendChild(groupElem);
        }
    }
}
// 将用户踢出群聊
function doDropMemberFromGroupBtnListener(myGroup, member) {
    let pElem = document.createElement('p');
    pElem.innerHTML = `请您确认是否需要将用户'` + member.nickname + `'从您的群组'` +  myGroup.gName + `'中踢出？<br/>
                        <button class="opt-cancel" onclick="doFinalDropMemberFromGroupBtnListener(true)">容我想想？</button>
                        <button onclick="doFinalDropMemberFromGroupBtnListener(false, ` + myGroup.gId + `, ` + member.uId + `)">确认踢出</button>`;
    showModal(pElem);
}
function doFinalDropMemberFromGroupBtnListener(isCancelld, gId, uId) {
    hideModal();
    if (!isCancelld) {
        $.ajax({
            url: getProjectPath() + '/user/drop-member-from-group/' + gId + '/' + uId,
            type: 'GET',
            success: (resp) => {
                callMessage(resp.code, resp.msg);
                if (resp.code === 0) {
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

// 解散群聊
function doDissolveGroupBtnListener(gName, gCode) {
    let pElem = document.createElement('p');
    pElem.innerHTML = `请您确认是否需要解散群组'` + gName + `'？<br/>
                        <button class="opt-cancel" onclick="doFinalDissolveGroupBtnListener(true)">容我想想？</button>
                        <button onclick="doFinalDissolveGroupBtnListener(false, '` + gCode + `')">确认解散</button>`;
    showModal(pElem);
}
function doFinalDissolveGroupBtnListener(isCancelld, gCode) {
    hideModal();
    if (!isCancelld) {
        $.ajax({
            url: getProjectPath() + '/user/dissolve-group/' + gCode,
            type: 'GET',
            success: (resp) => {
                callMessage(resp.code, resp.msg);
                if (resp.code === 0) {
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

// 退出群聊
function doExitGroupBtnListener(gName, gCode) {
    let pElem = document.createElement('p');
    pElem.innerHTML = `请您确认是否需要退出群组'` + gName + `'？<br/>
                        <button class="opt-cancel" onclick="doFinalExitGroupBtnListener(true)">容我想想？</button>
                        <button onclick="doFinalExitGroupBtnListener(false, '` + gCode + `')">确认退出</button>`;
    showModal(pElem);
}
function doFinalExitGroupBtnListener(isCancelld, gCode) {
    hideModal();
    if (!isCancelld) {
        sendUrl(getProjectPath() + '/user/exit-group/' + gCode, 'GET', null, getProjectPath() + '/main');
    } else {
        callMessage(1, "已取消操作！");
    }
}

// 监听 创建群聊 的button
var createGroupBtn = document.querySelector('#add-group button');
if (createGroupBtn) {
    createGroupBtn.addEventListener('click', function (evt) {
        let textElem = document.querySelector('#add-group .add-group-opt input[type="text"]');
        let textElemVal = textElem.value;
        if (textElemVal === undefined || textElemVal === '') {
            callMessage(1, "请填写群聊名称！");
            return ;
        }
        if (textElemVal.length > 20) {
            callMessage(1, "群聊名称不能大于20个字符！");
            return ;
        }

        $.ajax({
            url: getProjectPath() + '/user/add-group',
            type: 'POST',
            data: doSingleDataToJson(textElemVal),
            success: (resp) => {
                callMessage(resp.code, resp.msg);
                if (resp.code === 0) {
                    let group = resp.data;
                    console.log(group);
                    setGroupUsersInfo(group);
                    // fingMyCreatedGroups();
                }
            },
            error: (resp) => {
                console.log(resp);
                callMessage(-1, "***哎呀出错啦，请稍后再试吧！");
            }
        });
        textElem.value = '';
    });
}

// 查看群通知（有无用户申请加入您管理的群组）
function findGroupNotifications() {
    $.ajax({
        url: getProjectPath() + '/user/find-group-notifications',
        type: 'GET',
        success: (resp) => {
            console.log(resp);

            let rootElem = document.getElementById('group-notifications');
            let groupNotificationElem = rootElem.querySelector('#group-notification');
            let hasResultElem = groupNotificationElem.querySelector('.has-search-result');
            let nonResultElem = groupNotificationElem.querySelector('.non-search-result');
            let contentElems = hasResultElem.querySelectorAll('.apply-friend');
            if (contentElems)
                for (let i = 0; i < contentElems.length; i++) {
                    contentElems[i].remove();
                }

            if (resp.code === 0) {
                let groupsUsers = resp.data;

                if (groupsUsers && groupsUsers.length > 0) {
                    hasResultElem.classList.remove('hidden-el');
                    nonResultElem.classList.add('hidden-el');

                    for (let i = 0; i < groupsUsers.length; i++) {
                        let groupUser = groupsUsers[i];     // 拿到一一对应的用户和群组信息
                        if (Number(groupUser.group.hostUser.uId) === Number(groupUser.member.uId)) {
                            // 如果为群主则跳过
                            continue;
                        }

                        let friendInfoElem = document.createElement('div');
                        friendInfoElem.classList.add('apply-friend');

                        let avatarElem = document.createElement('div');
                        avatarElem.classList.add('avatar');
                        avatarElem.style.background = 'url(' + getProjectPath() + '/images' + groupUser.member.avatarUrl + ') no-repeat';

                        let friendNameElem = document.createElement('div');
                        friendNameElem.classList.add('friend-name', 'high-light');
                        friendNameElem.innerText = groupUser.member.nickname;

                        let applyTimeElem = document.createElement('div');
                        applyTimeElem.classList.add('apply-time');

                        let appContentElem = document.createElement('div');
                        appContentElem.classList.add('app-content');
                        appContentElem.innerHTML = `申请加入<span class="high-light">` + groupUser.group.gName + `</span> 群组`;
                        let aElem = document.createElement('a');
                        aElem.classList.add('agree');

                        if (groupUser.guStatus === '0') {
                            applyTimeElem.innerHTML = `入群时间：` + FormatDate(groupUser.joinTime);
                            aElem.innerText = '已同意此请求！';

                        } else if (groupUser.guStatus === '1') {
                            applyTimeElem.innerHTML = `申请时间：` + FormatDate(groupUser.applyTime);
                            aElem.innerText = '点击同意！';
                            aElem.addEventListener('click', () => {
                                doAgreeUserToGroup(groupUser.group.gId, groupUser.member.uId);
                            });
                        }

                        appContentElem.appendChild(aElem);
                        friendInfoElem.append(avatarElem, friendNameElem, applyTimeElem, appContentElem);
                        let firstElem = hasResultElem.firstElementChild;
                        if (firstElem) {
                            hasResultElem.insertBefore(friendInfoElem, firstElem);
                        } else {
                            hasResultElem.appendChild(friendInfoElem);
                        }
                    }

                } else {
                    hasResultElem.classList.add('hidden-el');
                    nonResultElem.classList.remove('hidden-el');
                    nonResultElem.querySelector('p').innerText = '暂无更多群通知！'
                }
            }
        },
        error: (resp) => {
            console.log(resp);
            callMessage(-1, "***哎呀出错啦，请稍后再试吧！");
        }
    })
}
// 同意用户进入自己的群组
function doAgreeUserToGroup(gId, uId) {
    $.ajax({
        url: getProjectPath() + '/user/agree-enter-group/' + gId + '/' + uId,
        type: 'GET',
        success: (resp) => {
            callMessage(resp.code, resp.msg);
            if (resp.code === 0)
                findGroupNotifications();
        },
        error: (resp) => {
            console.log(resp);
            callMessage(-1, "***哎呀出错啦，请稍后再试吧！");
        }
    })
}

// 监听 我加入的群组列表（获取群组的历史群聊消息）
var myGroupListElem = document.querySelector('#menu-bar #my-group-list');
if (myGroupListElem) {
    let myEnteredGroups = myGroupListElem.querySelectorAll('a[class="my-entered-group"]');
    for (let i = 0; i < myEnteredGroups.length; i++) {
        let myEnteredGroup = myEnteredGroups[i];

        // 为左侧消息窗口设置右侧菜单栏下对应的消息窗口
        let correspondingElem = document.createElement('div');
        correspondingElem.dataset.code = myEnteredGroup.dataset.code;         // 设置群唯一码gCode，以分辨不同的群组
        correspondingElem.classList.add('hidden-el', 'corresponding');
        // console.log(':::', myEnteredGroup.dataset.code);

        document.querySelector('#chat-box #message #public-message').appendChild(correspondingElem);      // 将其添加至群聊板中
    }

    for (let i = 0; i < myEnteredGroups.length; i++) {
        let myEnteredGroup = myEnteredGroups[i];

        myEnteredGroup.addEventListener('click', (evt) => {
            // console.log(myEnteredGroup)
            evt.preventDefault();

            let correspondingElems = document.querySelectorAll('#chat-box #message #public-message .corresponding');
            for (let i = 0; i < correspondingElems.length; i++) {
                let correspondingElem = correspondingElems[i];
                if (correspondingElem.dataset.code === myEnteredGroup.dataset.code) {
                    correspondingElem.classList.remove('hidden-el');
                } else {
                    correspondingElem.classList.add('hidden-el');
                }
            }

            // 设置 聊天框 上中部位置显示的名称：显示群聊消息 群名称
            let chatBoardHeaderElem = document.querySelector('#chat-obj');
            // chatBoardHeaderElem.classList.remove('hidden-el');
            /*chatBoardHeaderElem.innerText = `<span class="show-name">` + myEnteredGroup.innerText + ` (` +
                myEnteredGroup.members.length + `)</span>`;*/
            chatBoardHeaderElem.innerHTML = `<span class="show-name">` + myEnteredGroup.innerText + `</span>`;

            // 获取点击好友的 data-code 属性（即群唯一码gCode）
            let gCode = myEnteredGroup.dataset.code;
            // console.log(gCode, myEnteredGroup.dataset.hasGetted);

            // 若尚未获取过本人与该群的历史群聊消息
            if (typeof (myEnteredGroup.dataset.hasGetted) === 'undefined') {
                let pubMsgElem = document.querySelector('#chat-box #message #public-message');
                // 发送ajax请求，获取本人与该群的历史群聊消息列表
                $.ajax({
                    url: getProjectPath() + '/user/chat/public-history-msg/' + gCode,
                    type: 'GET',
                    success: function (resp) {
                        // console.log(resp)
                        // 成功处理逻辑

                        if (resp.code === 0) {
                            // 渲染数据
                            let pubMsgList = resp.data;
                            for (let i = 0; i < pubMsgList.length; i++) {
                                let pubMsg = pubMsgList[i];
                                // console.log(pubMsg);
                                setMessageToPubMsgBoard(pubMsg, gCode);
                            }

                            // console.log(pubMsgElem);
                            // 设置一个自定义属性用于：记录已经获取过本人与该好友的历史消息了
                            myEnteredGroup.dataset.hasGetted = true;
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
// 将 群聊消息 添加至群聊板块上
function setMessageToPubMsgBoard(pubMsg, gCode) {
    // 查找群聊聊天板下是否已经有过其它的群聊组信息
    let correspondingElems = document.querySelectorAll('#chat-box #message #public-message .corresponding');
    for (let i = 0; i < correspondingElems.length; i++) {

        let correspondingElem = correspondingElems[i];
        if (correspondingElem.dataset.code === gCode) {

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

            // 获取本人id
            let meId = document.querySelector('#user-info span').dataset.id;

            // 如果消息的发送者是群友，渲染数据为 rece_msg（本人接收群友的消息）
            // console.log(priMsg.sendUser.uId, friendId, Number(priMsg.sendUser.uId === Number(friendId));
            if (Number(pubMsg.sendUser.uId) !== Number(meId)) {
                msgElem.classList.add('rece_msg');
                uIdElem.classList.add('rece_uid');
                avatarElem.classList.add('rece_avatar');

            } else {
                // 消息发送者是本人，则渲染数据为 send_msg（对方接收本人发送的消息）
                msgElem.classList.add('send_msg');
                uIdElem.classList.add('send_uid');
                avatarElem.classList.add('send_avatar');
            }

            uIdElem.innerText = pubMsg.sendUser.uId;

            avatarElem.style.background = 'url(' + getProjectPath() + '/images' +  pubMsg.sendUser.avatarUrl +') no-repeat';

            nameElem.innerText = pubMsg.sendUser.nickname;

            textElem.innerText = pubMsg.content;
            let sendTime = new Date(pubMsg.sendTime);
            timeElem.innerText = sendTime.getFullYear() + "年" + (sendTime.getMonth() + 1) + "月" + sendTime.getDate() + "日 "
                + sendTime.getHours() + "时" + sendTime.getMinutes() + "分" + sendTime.getSeconds() + "秒";

            sentenceElem.append(textElem, triangleElem, timeElem);
            msgElem.append(uIdElem, avatarElem, nameElem, sentenceElem);
            correspondingElem.appendChild(msgElem);
            scrollBoard();
        }
    }
}


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
