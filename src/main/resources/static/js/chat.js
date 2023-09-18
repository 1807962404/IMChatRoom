/**
 * 聊天页面js文件
 * @type {string}
 */

window.onbeforeunload = evt => {
    evt.preventDefault();
    console.log('正在更新资源中...');
    sendUrl(getProjectPath() + '/entity/update-session-resources', 'GET', null);
}

// 获取本人id
const thisUserId = signInUser.uId;

// 滑动私聊聊天板块，会根据内容自适应
function scrollBoard() {
    let elems = new Array();
    elems.push(document.querySelector('#chat-bg #chat-box'));
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
scrollBoard();

// 监听内容发送框
var content = document.querySelector('#chat-ipt #ipt-content #content');
// 检查内容框中输入内容的长度
function checkContentLength(contentElem, contentLength) {
    let contentVal = getRealContent(contentElem.value);
    if (null != contentVal) {
        if (contentVal.length > contentLength)
            callMessage(1, "发送内容字数不得超过" + contentLength + "！");

        contentElem.value = contentVal.substring(0, contentLength);
    }
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

// 时间提醒
var curTimeEvt = setInterval(() => {
    document.getElementById('cur-time').firstElementChild.innerText = getDateTime();
}, 1000);

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
                    chatBoardHeaderInfo.classList.add('hidden-el');
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
                        if (nonResults[i].parentElement.id === 'my-friend-list' || hasResults[i].parentElement.id === 'my-group-list')
                            continue;
                        nonResults[i].classList.add('hidden-el');
                    };
                    for (let i = 0; i < hasResults.length; i++) {
                        if (nonResults[i].parentElement.id === 'my-friend-list' || hasResults[i].parentElement.id === 'my-group-list')
                            continue;
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

// 监听 添加好友栏（搜索好友） 的button
var searchFriendBtn = document.querySelector('#add-friend button');
if (searchFriendBtn) {
    searchFriendBtn.addEventListener('click', function (evt) {
        let textElem = document.querySelector('#add-friend input[type="text"]');
        let msgVal = textElem.value;
        if (checkContentIsEmpty(msgVal)) {
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

// 监听 加入群聊栏（搜索群聊） 的button
var enterGroupBtn = document.querySelector('#enter-group button');
if (enterGroupBtn) {
    enterGroupBtn.addEventListener('click', function (evt) {
        let textElem = document.querySelector('#enter-group input[type="text"]');
        let msgVal = textElem.value;
        if (checkContentIsEmpty(msgVal)) {
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
                                enterGroup(findGroup.gCode);
                                // sendUrl(getProjectPath() + '/user/enter-group/' + findGroup.gCode, 'GET');
                            });

                            groupInfo.appendChild(addBtnElem);
                            // 最后再成为此节点的子节点
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

// 查找我的新好友通知
function findFriendNotifications() {
    $.ajax({
        url: getProjectPath() + '/user/find-new-friends',
        type: 'GET',
        success: (resp) => {
            let newFriendShips = resp;

            let rootElem = document.querySelector('#new-friend');
            let hasResultElem = rootElem.querySelector('.has-search-result');
            let hasNonResultElem = rootElem.querySelector('.non-search-result');

            if (newFriendShips !== null && newFriendShips.length > 0) {
                hasResultElem.classList.remove('hidden-el');
                let applyFriendInfoElems = hasResultElem.querySelectorAll('.apply-friend');
                if (applyFriendInfoElems)
                    for (let i = 0; i < applyFriendInfoElems.length; i++)
                        applyFriendInfoElems[i].remove();

                hasNonResultElem.classList.add('hidden-el');

                for (let i = 0; i < newFriendShips.length; i++) {
                    let friendShip = newFriendShips[i];
                    // console.log(newFriendShip);

                    let friendInfoElem = document.createElement('div');
                    friendInfoElem.classList.add('apply-friend');

                    let avatarElem = document.createElement('div');
                    avatarElem.classList.add('avatar');

                    let friendNameElem = document.createElement('div');
                    friendNameElem.classList.add('friend-name', 'high-light');

                    let applyTimeElem = document.createElement('div');
                    applyTimeElem.classList.add('apply-time');

                    let appContentElem = document.createElement('div');
                    appContentElem.classList.add('app-content');

                    if (friendShip && friendShip !== null && friendShip !== undefined) {

                        if (Number(thisUserId) === Number(friendShip.hostUser.uId)) {
                            // 如果本人是好友关系申请者
                            avatarElem.style.background = 'url(' + getProjectPath() + '/images' + friendShip.friendUser.avatarUrl + ') no-repeat';
                            friendNameElem.innerText = friendShip.friendUser.nickname;

                            if (friendShip.fsStatus === '0') {
                                appContentElem.innerText = '对方已同意您的好友申请！';

                            } else if (friendShip.fsStatus === '1') {   // 正处于好友关系确认中
                                appContentElem.innerText = `您已向对方发送好友申请！`;
                            }

                        } else if (Number(thisUserId) === Number(friendShip.friendUser.uId)) {
                            // 如果本人是好友关系被申请者，便可以同意好友申请
                            avatarElem.style.background = 'url(' + getProjectPath() + '/images' + friendShip.hostUser.avatarUrl + ') no-repeat';
                            friendNameElem.innerText = friendShip.hostUser.nickname;

                            if (friendShip.fsStatus === '0') {
                                appContentElem.innerText = '您已同意对方的好友申请！';

                            } else if (friendShip.fsStatus === '1') {   // 正处于好友关系确认中

                                let aElem = document.createElement('a');
                                // 为a标签设置自定义属性 gohref：发送的请求
                                aElem.dataset.gohref = getProjectPath() + '/user/add-friend/' + friendShip.hostUser.uId;
                                aElem.classList.add('add-friend-btn');
                                aElem.innerText = '点击同意好友申请！'

                                let addBtnElem = document.createElement('span');
                                addBtnElem.appendChild(aElem);
                                addBtnElem.classList.add('agree');
                                addBtnElem.addEventListener('click',  (evt) => {
                                    doAddFriendBtnListener(evt, addBtnElem);    // 绑上监听点击事件（同意好友申请）
                                });

                                appContentElem.innerText = `请求添加好友，`;
                                appContentElem.appendChild(addBtnElem);
                            }
                        }

                        if (friendShip.fsStatus === '0') {
                            applyTimeElem.innerText = '结交时间：' + FormatDate(friendShip.applyTime);

                        } else if (friendShip.fsStatus === '1') {
                            applyTimeElem.innerText = '申请时间：' + FormatDate(friendShip.applyTime);
                        }

                    } else {
                        friendNameElem.innerText = '此账号已注销';
                        appContentElem.innerText = `已无法操作！`;
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

// 监听 用户会话注销按钮
var userExitBtn = document.querySelector('#user-exit button');
if (userExitBtn) {
    userExitBtn.addEventListener('click', () => {
        sendUrl(getProjectPath() + '/user/sign-out', 'GET',
            null, getProjectPath() + '/login');
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
            null, getProjectPath() + '/login');

    } else {
        callMessage(1, "操作已取消！");
    }
}

// 监听右侧菜单栏下的 发布系统广播 标签（获取该管理员用户发布过的所有的系统广播信息，然后将其展示在左侧消息窗口中）
// document.getElementById('all-broadcasts').addEventListener('click', (evt) => {
function doGetMyBroadcasts() {
    $.ajax({
        url: getProjectPath() + '/user/chat/admin-published-broadcasts',
        type: 'GET',
        success: (resp) => {
            console.log(resp);
            let hasBroadcastElem = document.querySelector('#publish-system-broadcast .has-search-result');
            let hasNonBroadcastElem = document.querySelector('#publish-system-broadcast .non-search-result');

            if (resp.code === 0) {
                let publishedBroadcasts = resp.data;
                // console.log(publishedBroadcasts);
                let myPubBroadcastPreviewElem = document.querySelector('#publish-system-broadcast #broadcast-preview');
                if (publishedBroadcasts && publishedBroadcasts.length > 0) {
                    hasBroadcastElem.classList.remove('hidden-el');
                    hasNonBroadcastElem.classList.add('hidden-el');

                    if (typeof (myPubBroadcastPreviewElem.dataset.hasGetted) === 'undefined') {
                        for (let i = 0; i < publishedBroadcasts.length; i++) {
                            // 展示到自己的广播内容列表下
                            setContentToOwnPublishedBroadcast(publishedBroadcasts[i]);
                        }
                        myPubBroadcastPreviewElem.dataset.hasGetted = true;
                    }
                } else {
                    hasBroadcastElem.classList.add('hidden-el');
                    hasNonBroadcastElem.classList.remove('hidden-el');
                    hasNonBroadcastElem.firstElementChild.innerText = '您暂未发布任何系统公告！';
                }
            }
        },
        error: (resp) => {
            console.log("Error: " + resp)
        }
    })
};
// 将用户发表的广播内容展示出来
function setContentToOwnPublishedBroadcast(publishedBroadcast) {

    let hasNonBroadcastElem = document.querySelector('#publish-system-broadcast .non-search-result');
    if (!hasNonBroadcastElem.classList.contains('hidden-el'))
        hasNonBroadcastElem.classList.add('hidden-el');

    let hasBroadcastElem = document.querySelector('#publish-system-broadcast .has-search-result');
    if (hasBroadcastElem.classList.contains('hidden-el'))
        hasBroadcastElem.classList.remove('hidden-el');

    let myPublishedBroadcastElem = document.createElement('div');
    myPublishedBroadcastElem.classList.add('my-pub-broadcast');

    let avatarElem = document.createElement('div');
    avatarElem.classList.add('avatar');
    avatarElem.style.background = 'url(' + getProjectPath() + '/images' + publishedBroadcast.publisher.avatarUrl + ') no-repeat';

    let friendNameElem = document.createElement('div')
    friendNameElem.classList.add('friend-name', 'high-light', 'ellipsis');
    friendNameElem.innerText = publishedBroadcast.publisher.nickname;

    let timeElem = document.createElement('div');
    timeElem.classList.add('apply-time');
    timeElem.innerText = FormatDate(publishedBroadcast.sendTime);

    let broadcastContentElem = document.createElement('div');
    broadcastContentElem.classList.add('broadcast-content');
    broadcastContentElem.innerText = publishedBroadcast.content;

    myPublishedBroadcastElem.append(avatarElem, friendNameElem, timeElem, broadcastContentElem);
    let broadcastPreviewElem = document.querySelector('#publish-system-broadcast #broadcast-preview');
    let hasFirstElem = broadcastPreviewElem.firstElementChild;
    if (hasFirstElem) {
        broadcastPreviewElem.insertBefore(myPublishedBroadcastElem, hasFirstElem);
    } else {
        broadcastPreviewElem.appendChild(myPublishedBroadcastElem);
    }
}

// 获取本人（管理员用户）发布的所有优文摘要
function doGetMyArticles() {
    $.ajax({
        url: getProjectPath() + '/user/chat/admin-published-articles',
        type: 'GET',
        success: (resp) => {
            console.log(resp);
            let hasArticleElem = document.querySelector('#publish-excellent-abstract .has-search-result');
            let hasNonArticleElem = document.querySelector('#publish-excellent-abstract .non-search-result');

            if (resp.code === 0) {
                let publishedArticles = resp.data;
                // console.log(publishedArticles);
                let myPubArticlePreviewElem = document.querySelector('#publish-excellent-abstract #article-preview');
                if (publishedArticles && publishedArticles.length > 0) {
                    hasArticleElem.classList.remove('hidden-el');
                    hasNonArticleElem.classList.add('hidden-el');
                    if (typeof (myPubArticlePreviewElem.dataset.hasGetted) === 'undefined') {
                        for (let i = 0; i < publishedArticles.length; i++) {
                            // 展示到自己的广播内容列表下
                            setContentToOwnPublishedArticle(publishedArticles[i]);
                        }
                        myPubArticlePreviewElem.dataset.hasGetted = true;
                    }
                } else {
                    hasArticleElem.classList.add('hidden-el');
                    hasNonArticleElem.classList.remove('hidden-el');
                    hasNonArticleElem.firstElementChild.innerText = '您暂未发表任意优文摘要！';
                }
            }
        },
        error: (resp) => {
            console.log("Error: " + resp);
        }
    })
}
function setContentToOwnPublishedArticle(publishedArticle) {

    let hasNonAbstractsElem = document.querySelector('#publish-excellent-abstract .non-search-result');
    if (!hasNonAbstractsElem.classList.contains('hidden-el'))
        hasNonAbstractsElem.classList.add('hidden-el');

    let hasAbstractsElem = document.querySelector('#publish-excellent-abstract .has-search-result');
    if (hasAbstractsElem.classList.contains('hidden-el'))
        hasAbstractsElem.classList.remove('hidden-el');

    let myPublishedArticleElem = document.createElement('div');
    myPublishedArticleElem.classList.add('my-pub-article');

    let avatarElem = document.createElement('div');
    avatarElem.classList.add('avatar');
    avatarElem.style.background = 'url(' + getProjectPath() + '/images' + publishedArticle.publisher.avatarUrl + ') no-repeat';

    let friendNameElem = document.createElement('div')
    friendNameElem.classList.add('friend-name', 'high-light', 'ellipsis');
    friendNameElem.innerText = publishedArticle.publisher.nickname;

    let timeElem = document.createElement('div');
    timeElem.classList.add('apply-time');
    timeElem.innerText = FormatDate(publishedArticle.sendTime);

    let articleContentElem = document.createElement('div');
    articleContentElem.classList.add('article-content');
    articleContentElem.innerText = publishedArticle.content;

    myPublishedArticleElem.append(avatarElem, friendNameElem, timeElem, articleContentElem);
    let articlePreviewElem = document.querySelector('#publish-excellent-abstract #article-preview');
    let hasFristElem = articlePreviewElem.firstElementChild;
    if (hasFristElem) {
        articlePreviewElem.insertBefore(myPublishedArticleElem, hasFristElem);
    } else {
        articlePreviewElem.appendChild(myPublishedArticleElem);
    }
}

// 监听右侧菜单栏下的 意见反馈 标签（获取所有的意见反馈信息，然后将其展示在左侧消息窗口中）
let allFeedbackElem = document.getElementById('all-feedbacks');
if (allFeedbackElem) {
    allFeedbackElem.addEventListener('click', (evt) => {
        evt.preventDefault();
        $.ajax({
            url: getProjectPath() + '/user/chat/feedback-history-msg',
            type: 'GET',
            success: (resp) => {
                // console.log(resp);
                let feedbackList = resp;

                let hasFeedbackElem = document.querySelector('#feedback .has-search-result');
                let hasNonFeedbackElem = document.querySelector('#feedback .non-search-result');
                let feedbackPreviewElems = document.querySelectorAll('#all-feedback-list .feedback-preview .feedback-preview-content');
                for (let i = 0; i < feedbackPreviewElems.length; i++) {
                    feedbackPreviewElems[i].remove();
                }

                if (feedbackList && feedbackList.length > 0) {
                    hasNonFeedbackElem.classList.remove('hidden-el');
                    hasFeedbackElem.classList.add('hidden-el');
                    for (let i = 0; i < feedbackList.length; i++) {
                        setContentToFeedbackList(feedbackList[i]);
                    }
                } else {
                    hasFeedbackElem.classList.add('hidden-el');
                    hasNonFeedbackElem.classList.remove('hidden-el');
                    hasNonFeedbackElem.querySelector('#feedback .non-search-result p').innerText = `暂无更多反馈信息！`;
                }
            },
            error: (resp) => {
                console.log("Error: " + resp);
            }
        })
    });
}

// 插入内容至意见反馈栏（默认插入到行首）
function setContentToFeedbackList(feedbackContent) {

    let hasNonFeedbackElem = document.querySelector('#feedback .non-search-result');
    if (!hasNonFeedbackElem.classList.contains('hidden-el'))
        hasNonFeedbackElem.classList.add('hidden-el');

    let hasFeedbackElem = document.querySelector('#feedback .has-search-result');
    if (hasFeedbackElem.classList.contains('hidden-el'))
        hasFeedbackElem.classList.remove('hidden-el');

    let feedbackPreviewContentElem = document.createElement('div');
    feedbackPreviewContentElem.classList.add('feedback-preview-content');

    let avatarElem = document.createElement('div');
    avatarElem.classList.add('avatar');

    let friendNameElem = document.createElement('div')
    friendNameElem.classList.add('friend-name', 'high-light', 'ellipsis');

    let timeElem = document.createElement('div');
    timeElem.classList.add('apply-time');
    timeElem.innerText = FormatDate(feedbackContent.sendTime);

    let feedbackContentElem = document.createElement('div');
    feedbackContentElem.classList.add('feedback-content');
    feedbackContentElem.innerText = feedbackContent.content;

    if (feedbackContent.publisher && feedbackContent.publisher !== null && feedbackContent.publisher !== undefined) {
        avatarElem.style.background = 'url(' + getProjectPath() + '/images' + feedbackContent.publisher.avatarUrl + ') no-repeat';
        friendNameElem.innerText = feedbackContent.publisher.nickname;

    } else {
        friendNameElem.innerText = '此账号已注销';
    }

    feedbackPreviewContentElem.append(avatarElem, friendNameElem, timeElem, feedbackContentElem);
    let feedbackPreviewElem = document.querySelector('#all-feedback-list .feedback-preview');
    let hasFristElem = feedbackPreviewElem.firstElementChild;
    if (hasFristElem) {
        feedbackPreviewElem.insertBefore(feedbackPreviewContentElem, hasFristElem);
    } else {
        feedbackPreviewElem.appendChild(feedbackPreviewContentElem);
    }
}

// 查询我创建的所有群组、以及这些群组中所有的用户信息
function fingMyCreatedGroups() {
    $.ajax({
        url: getProjectPath() + '/user/get-my-created-groups',
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

    let nonResultElem = document.querySelector('#my-created-groups .non-search-result');
    if (!nonResultElem.classList.contains('hidden-el'))
        nonResultElem.classList.add('hidden-el');

    let hasResultElem = document.querySelector('#my-created-groups .has-search-result');
    if (hasResultElem.classList.contains('hidden-el'))
        hasResultElem.classList.remove('hidden-el');

    let createdGroupElem = hasResultElem.querySelector('#created-group');

    let groupElem = document.createElement('div');
    groupElem.classList.add('group');

    let groupNameElem = document.createElement('p');
    groupNameElem.classList.add('group-name');
    groupNameElem.innerText = myGroup.gName;

    let myGroupUsersElem = document.createElement('div');
    myGroupUsersElem.classList.add('my-group-members');
    for (let i = 0; i < myGroup.members.length; i++) {
        let groupUser = myGroup.members[i];   // 群成员(GroupUser类型)
        if (groupUser.guStatus == '0') {
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

                aElem.innerText = '点击移出群聊';

                friendNameElem.innerText = member.nickname;

                dropBtnElem.addEventListener('click',  () => {
                    // console.log('移出群聊', myGroup.gCode, member.uId);
                    // 绑上监听点击事件（移出群聊）
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
}
// 将用户移出群聊
function doDropMemberFromGroupBtnListener(myGroup, member) {
    let pElem = document.createElement('p');
    pElem.innerHTML = `请您确认是否需要将用户'` + member.nickname + `'从您的群组'` +  myGroup.gName + `'中移出？<br/>
                        <button class="opt-cancel" onclick="doFinalDropMemberFromGroupBtnListener(true)">容我想想？</button>
                        <button onclick="doFinalDropMemberFromGroupBtnListener(false, ` + myGroup.gId + `, ` + member.uId + `)">确认移出</button>`;
    showModal(pElem);
}

// 退出群聊
function doExitGroupBtnListener(gName, gCode) {
    let pElem = document.createElement('p');
    pElem.innerHTML = `请您确认是否需要退出群组'` + gName + `'？<br/>
                        <button class="opt-cancel" onclick="doFinalExitGroupBtnListener(true)">容我想想？</button>
                        <button onclick="doFinalExitGroupBtnListener(false, '` + gCode + `')">确认退出</button>`;
    showModal(pElem);
}

// 解散群聊
function doDissolveGroupBtnListener(gName, gCode) {
    let pElem = document.createElement('p');
    pElem.innerHTML = `请您确认是否需要解散群组'` + gName + `'？<br/>
                        <button class="opt-cancel" onclick="doFinalDissolveGroupBtnListener(true)">容我想想？</button>
                        <button onclick="doFinalDissolveGroupBtnListener(false, '` + gCode + `')">确认解散</button>`;
    showModal(pElem);
}

// 监听 创建群聊 的button
var createGroupBtn = document.querySelector('#add-group button');
if (createGroupBtn) {
    createGroupBtn.addEventListener('click', function (evt) {
        let textElem = document.querySelector('#add-group .add-group-opt input[type="text"]');
        let textElemVal = textElem.value;
        if (checkContentIsEmpty(textElemVal)) {
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
                    // console.log(group);
                    setGroupUsersInfo(group);
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
            // console.log(resp);

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
                        setMessageToGroupNotifications(groupUser);
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
function setMessageToGroupNotifications(groupUser) {

    let nonResultElem = document.querySelector('#group-notifications #group-notification .non-search-result');
    if (!nonResultElem.classList.contains('hidden-el'))
        nonResultElem.classList.add('hidden-el');

    let hasResultElem = document.querySelector('#group-notifications #group-notification .has-search-result');
    if (hasResultElem.classList.contains('hidden-el'))
        hasResultElem.classList.remove('hidden-el');

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
    let aElem = document.createElement('a');
    aElem.classList.add('agree');

    let hasResult = false;
    if (Number(thisUserId) === Number(groupUser.group.hostUser.uId)) {
        // 如果本人为群主

        if (groupUser.guStatus === '0')
            aElem.innerText = '已同意用户：' + groupUser.member.nickname + ' 的入群申请！';

        else if (groupUser.guStatus === '1') {
            appContentElem.innerHTML = `申请加入<span class="high-light">` + groupUser.group.gName + `</span> 群组`;
            aElem.innerText = '点击同意！';
            aElem.addEventListener('click', () => {
                doAgreeUserToGroup(groupUser.group.gId, groupUser.member.uId);
            });
        }
        hasResult = true;

    } else if (Number(thisUserId) === Number(groupUser.member.uId)) {
        // 否则为群成员
        if (groupUser.guStatus === '0')
            aElem.innerText = '您已加入：' + groupUser.group.gName + ' 群组！';

        else if (groupUser.guStatus === '1')
            aElem.innerText = '您已发送：' + groupUser.group.gName + ' 的入群申请！';

        hasResult = true;
    }

    if (hasResult) {
        if (groupUser.guStatus === '0')
            applyTimeElem.innerHTML = `入群时间：` + FormatDate(groupUser.joinTime);

        else if (groupUser.guStatus === '1')
            applyTimeElem.innerHTML = `申请时间：` + FormatDate(groupUser.applyTime);

        appContentElem.appendChild(aElem);
        friendInfoElem.append(avatarElem, friendNameElem, applyTimeElem, appContentElem);

        let firstElem = hasResultElem.firstElementChild;
        if (firstElem) {
            hasResultElem.insertBefore(friendInfoElem, firstElem);
        } else {
            hasResultElem.appendChild(friendInfoElem);
        }
    }
}

/**
 * 获取我加入的群组列表
 */
function getMyGroups() {
    let myGroupListElem = document.getElementById('my-group-list');
    let hasResultElem= myGroupListElem.querySelector('.has-search-result');
    let nonResultElem = myGroupListElem.querySelector('.non-search-result');
    let myGroupsElem = hasResultElem.querySelector('#my-groups');

    if (myGroupsElem.dataset.hasGetted) {
        // console.log('已获取过群聊列表！');
        return;
    }

    $.ajax({
        url: getProjectPath() + '/user/get-my-entered-groups',
        type: 'GET',
        success: (resp) => {
            let myEnteredGroupList = resp;
            // console.log(myEnteredGroupList);
            if (myEnteredGroupList && myEnteredGroupList.length > 0) {

                hasResultElem.classList.remove('hidden-el');
                nonResultElem.classList.add('hidden-el');

                for (let i = 0; i < myEnteredGroupList.length; i++) {
                    let myEnteredGroup = myEnteredGroupList[i];

                    let pElem = document.createElement('p');

                    let aElem = document.createElement('a');
                    aElem.dataset.code = myEnteredGroup.gCode;
                    // 设置群成员人数
                    if (myEnteredGroup.members && myEnteredGroup.members.length > 0) {
                        let memberSize = 0;
                        for (let j = 0; j < myEnteredGroup.members.length; j++) {
                            let thisMember = myEnteredGroup.members[j];
                            if (thisMember.guStatus === '0')
                                memberSize += 1;
                        }
                        aElem.dataset.memberLen = memberSize;
                    }
                    aElem.classList.add('my-entered-group');
                    aElem.innerText = myEnteredGroup.gName;

                    let btnElem = document.createElement('button');
                    btnElem.dataset.code = myEnteredGroup.gCode;
                    if (Number(thisUserId) === Number(myEnteredGroup.hostUser.uId)) {
                        // 如果该用户是群主，则可以操作：解散群聊
                        btnElem.classList.add('dissolve-group');
                        btnElem.innerText = '解散群聊';
                        btnElem.addEventListener('click', () => {
                            doDissolveGroupBtnListener(myEnteredGroup.gName, myEnteredGroup.gCode);
                        })

                    } else {
                        // 如果该用户不是群主，则只能操作：退出群聊
                        btnElem.classList.add('exit-group');
                        btnElem.innerText = '退出群聊';
                        btnElem.addEventListener('click', () => {
                            doExitGroupBtnListener(myEnteredGroup.gName, myEnteredGroup.gCode);
                        })
                    }

                    aElem.addEventListener('click', () => {
                        seePubMsg(aElem);
                    })

                    pElem.append(aElem, btnElem);
                    myGroupsElem.appendChild(pElem);

                    // 为左侧消息窗口设置右侧菜单栏下群组列表所对应的群聊消息窗口
                    let correspondingElem = document.createElement('div');
                    correspondingElem.dataset.code = myEnteredGroup.gCode;         // 设置群组gCode，以分辨不同的群组
                    correspondingElem.classList.add('hidden-el', 'corresponding');
                    document.querySelector('#chat-box #message #public-message').appendChild(correspondingElem);      // 将其添加至群聊板中
                }
                myGroupsElem.dataset.hasGetted = true;

            } else {
                hasResultElem.classList.add('hidden-el');
                nonResultElem.classList.remove('hidden-el');
                nonResultElem.querySelector('p').innerText = '暂未加入任意群聊！';
            }
        },
        error: (resp) => {
            console.log("Error: " + resp);
        }
    })
}
function seePubMsg(myEnteredGroupElem) {
    let correspondingElems = document.querySelectorAll('#chat-box #message #public-message .corresponding');
    for (let i = 0; i < correspondingElems.length; i++) {
        let correspondingElem = correspondingElems[i];
        if (correspondingElem.dataset.code === myEnteredGroupElem.dataset.code) {
            correspondingElem.classList.remove('hidden-el');
        } else {
            correspondingElem.classList.add('hidden-el');
        }
    }

    // 设置 聊天框 上中部位置显示的名称：显示群聊消息 群名称（群聊成员人数）
    let chatBoardHeaderElem = document.querySelector('#chat-obj');
    chatBoardHeaderElem.innerHTML = `<span class="show-name">` + myEnteredGroupElem.innerText + ` (` +
        myEnteredGroupElem.dataset.memberLen + `)</span>`;
    // chatBoardHeaderElem.innerHTML = `<span class="show-name">` + myEnteredGroupElem.innerText + `</span>`;

    // 获取点击好友的 data-code 属性（即群唯一码gCode）
    let gCode = myEnteredGroupElem.dataset.code;
    // console.log(gCode, myEnteredGroup.dataset.hasGetted);

    // 若尚未获取过本人与该群的历史群聊消息
    if (typeof (myEnteredGroupElem.dataset.hasGetted) === 'undefined') {

        let pubMsgElem = document.querySelector('#chat-box #message #public-message');
        // 发送ajax请求，获取本人与该群的历史群聊消息列表
        $.ajax({
            url: getProjectPath() + '/user/chat/public-history-msg/' + gCode,
            type: 'GET',
            success: function (resp) {
                // console.log(resp)
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
                    myEnteredGroupElem.dataset.hasGetted = true;
                }
            },
            error: function (resp) {
                console.log('Error: ' + resp)
            }
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

            // 如果消息的发送者是群友，渲染数据为 rece_msg（本人接收群友的消息）
            // console.log(priMsg.sendUser.uId, friendId, Number(priMsg.sendUser.uId === Number(friendId));
            if (Number(pubMsg.sendUser.uId) !== Number(thisUserId)) {
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

/**
 * 获取我的好友列表
 */
function getMyFriends() {
    let myFriendListElem = document.getElementById('my-friend-list');
    let hasResultElem = myFriendListElem.querySelector('.has-search-result');
    let nonResultElem = myFriendListElem.querySelector('.non-search-result');
    let myFriendsElem = hasResultElem.querySelector('#my-friends');

    if (myFriendsElem.dataset.hasGetted) {
        // console.log('已获取过好友列表！');
        return;
    }

    $.ajax({
        url: getProjectPath() + '/user/get-my-friends',
        type: 'GET',
        success: (resp) => {
            let myFriendList = resp;
            // console.log(myFriendList);
            if (myFriendList && myFriendList.length > 0) {

                hasResultElem.classList.remove('hidden-el');
                nonResultElem.classList.add('hidden-el');

                for (let i = 0; i < myFriendList.length; i++) {
                    let friend = myFriendList[i];

                    if (friend.friendShip.hostUser === undefined || friend.friendShip.friendUser === undefined ||
                        friend.friendShip.hostUser === null || friend.friendShip.friendUser === null ||
                        friend.friendShip.hostUser.accountStatus !== '0' || friend.friendShip.friendUser.accountStatus !== '0')
                        continue;

                    let pElem = document.createElement('p');

                    let aElem = document.createElement('a');
                    aElem.classList.add('my-friend');

                    let btnElem = document.createElement('button');
                    btnElem.classList.add('del-user');
                    btnElem.innerText = '删除好友';

                    let friendId = 0;
                    let friendName = '';
                    // console.log('thisUserId: ', thisUserId)
                    if (Number(thisUserId) === Number(friend.friendShip.hostUser.uId)) {
                        // 如果我是好友关系的申请人，则显示对方 好友的id和名称
                        friendId = friend.friendShip.friendUser.uId;
                        friendName = friend.friendShip.remark;
                        // aElem.innerText = friend.friendShip.friendUser.nickname;

                    } else if (Number(thisUserId) === Number(friend.friendShip.friendUser.uId)){
                        // 如果我是好友关系的被申请人，则显示申请人（即对方 好友）的id和名称
                        friendId = friend.friendShip.hostUser.uId;
                        friendName = friend.friendShip.hostUser.nickname;
                    }

                    aElem.dataset.id = friendId;
                    aElem.innerText = friendName;
                    aElem.addEventListener('click', () => {
                        seePriMsg(aElem);
                    });

                    btnElem.addEventListener('click', () => {
                        doDelFriendBtnListener(friendId, friendName);
                    })

                    pElem.append(aElem, btnElem);
                    myFriendsElem.appendChild(pElem);

                    // 为左侧消息窗口设置右侧菜单栏下好友列表所对应的消息窗口
                    let correspondingElem = document.createElement('div');
                    correspondingElem.dataset.id = aElem.dataset.id;         // 设置用户好友id，以分辨不同的好友
                    correspondingElem.classList.add('hidden-el', 'corresponding');
                    document.querySelector('#chat-box #message #private-message').appendChild(correspondingElem);      // 将其添加至群聊板中
                }
                myFriendsElem.dataset.hasGetted = true;

            } else {
                hasResultElem.classList.add('hidden-el');
                nonResultElem.classList.remove('hidden-el');
                nonResultElem.querySelector('p').innerText = '暂无任何好友信息！';
            }
        },
        error: (resp) => {v`                                                                               `
            console.log("Error: " + resp);
        }
    })
}
// 获取本人与对方的历史私聊消息
function seePriMsg(myFriendElem) {
    // 获取点击好友的 data-id 属性（即好友的用户id）
    let friendId = Number(myFriendElem.dataset.id);
    // console.log('friendId: ', friendId);

    // 设置左侧消息窗口展示的 对应好友私聊消息
    let correspondingElems = document.querySelectorAll('#chat-box #message #private-message .corresponding');
    for (let i = 0; i < correspondingElems.length; i++) {
        let correspondingElem = correspondingElems[i];
        // 只有是此好友对应id的私聊消息窗口，才会展示出来
        if (Number(correspondingElem.dataset.id) === friendId) {
            correspondingElem.classList.remove('hidden-el');
        } else {
            correspondingElem.classList.add('hidden-el');
        }
    }

    // 设置 聊天框 上中部位置显示的名称：显示私聊消息对方的名称
    let chatBoardHeaderElem = document.querySelector('#chat-obj');
    // chatBoardHeaderElem.classList.remove('hidden-el');
    chatBoardHeaderElem.innerHTML = `正在与 <span class="show-name">` + myFriendElem.innerText + `</span> 聊天`;

    // console.log(myFriend.dataset.hasGetted);
    // 若尚未获取过本人与对方的历史私聊消息
    if (typeof (myFriendElem.dataset.hasGetted) === 'undefined') {

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
                    myFriendElem.dataset.hasGetted = true;
                }
            },
            error: function (resp) {
                console.log('Error: ' + resp)
            }
        });
    }
}

// 将私聊消息 添加至私聊板块上
function setMessageToPriMsgBoard(priMsg, friendId) {
    // 查找群聊聊天板下是否已经有过其它的群聊组信息
    let correspondingElems = document.querySelectorAll('#chat-box #message #private-message .corresponding');
    for (let i = 0; i < correspondingElems.length; i++) {

        let correspondingElem = correspondingElems[i];
        if (Number(correspondingElem.dataset.id) === Number(friendId)) {
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
            correspondingElem.appendChild(msgElem);
            scrollBoard();
        }
    }
}

// 删除好友
function doDelFriendBtnListener(friendId, nickname) {
    // 显示弹窗提醒
    let pElem = document.createElement('p');
    pElem.innerHTML = `请您确认是否需要删除uId为：` + friendId + `，昵称为：` + nickname + ` 的好友信息？<br />
                <button class="opt-cancel" onclick="doFinalDelFriendBtnListener(true)">容我想想？</button>
                <button onclick="doFinalDelFriendBtnListener(false, ` + friendId + `)">确认删除</button>`;
    showModal(pElem);
}

function doFinalDelFriendBtnListener(isCancelled, friendId) {
    hideModal();
    if (!isCancelled) {
        sendUrl(getProjectPath() + '/user/del-friend/' + friendId, 'GET', null, getProjectPath() + "/main");
    } else {
        callMessage(1, "已取消操作");
    }
}

/**
 * 修改用户信息
 */
function editProfile() {
    let newNicknameVal = document.querySelector('#profile-introduce input[type="text"]').value;
    let newPasswordVal = document.querySelector('#profile-introduce input[type="password"]').value;

    // 1、检查昵称是否合规
    if (checkContentIsEmpty(newNicknameVal) || newNicknameVal.length < 3 || newNicknameVal.length > 15) {
        callMessage(1, "请检查输入的昵称格式！");
        return;
    }

    // 封装数据
    let data = {
        'nickname': newNicknameVal,
    };

    // 2、检查密码是否合规
    if (newPasswordVal && '' != newPasswordVal) {
        if (checkContentIsEmpty(newPasswordVal) || newPasswordVal.length < 6 | newPasswordVal.length > 20) {
            callMessage(1, "请检查输入的密码格式！");
            return;
        }
        data['password'] = newPasswordVal;
    }

    // 3、发送请求修改个人信息
    sendUrl(getProjectPath() + '/user/edit-profile', 'POST', JSON.parse(JSON.stringify(data)), getProjectPath() + '/main')
}

// 头像上传
var avatarElem = document.querySelector('#profile #profile-avatar #avatar-upload');
if (avatarElem) {
    avatarElem.addEventListener('change', (evt) => {
        let file = avatarElem.files[0];
        // console.log(file);
        uploadAvatar(file);
    })
}
function uploadAvatar(file) {

    if (file === null || file === undefined)
        return;

    // 检查是否支持上传的头像文件
    let isSupport = false;
    switch (file.type) {
        case 'image/jpg':
        case 'image/png':
        case 'image/jpeg':
            isSupport = true;
    }

    if (!isSupport) {
        callMessage(1, "不支持的文件类型，请上传jpg、png或jpeg格式的头像文件！");
        return ;
    }

    file.width

    let fileData = new FormData();
    fileData.append('avatar', file);
    console.log(fileData);

    $.ajax({
        url: getProjectPath() + '/user/upload-avatar',
        type: 'POST',
        data: fileData,
        processData: false, // 告诉jQuery不要去处理发送的数据
        contentType: false, // 告诉jQuery不要去设置Content-Type请求头
        success: (resp) => {
            callMessage(resp.code, resp.msg);
            if (resp.code === 0)
                sleep(sleepTime).then(()=> window.location.href = getProjectPath() + '/main');
        },
        error: (resp) => {
            console.log("Error: " + resp);
        }
    });
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
