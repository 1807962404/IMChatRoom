/**
 * 通用js文件
 */


//获取当前项目的名称
function getProjectPath() {
    //获取主机地址之后的目录，如： cloudlibrary/admin/books.jsp
    var pathName = window.document.location.pathname;
    //获取带"/"的项目名，如：/cloudlibrary
    var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
    return projectName;
}

const sleepTime = 700;
// 睡眠指定时间后执行程序
function sleep (time) {
    return new Promise((resolve) => setTimeout(resolve, time));
}

const modalElem = document.getElementById("customized-modal");
const modalContentElem = modalElem.firstElementChild;             // modal-content element
// 弹窗显示
function showModal(elem) {
    modalElem.style.display = "block";
    // 自定义弹窗内容
    let customModalContentElem = modalContentElem.lastElementChild;
    customModalContentElem.classList.remove('hidden-el');
    customModalContentElem.appendChild(elem);
}
// 弹窗隐藏
function hideModal() {
    modalElem.style.display = "none";
    let customModalContentElem = modalContentElem.lastElementChild;
    customModalContentElem.classList.add('hidden-el');
    let children = customModalContentElem.children;
    // console.log(children);
    for (let i = 0; i < children.length; i++) {
        // 每次关闭弹窗都需删除自定义模块下的所有内容
        let child = children[i];
        customModalContentElem.removeChild(child);
    }
}
// 弹窗关闭
function closeModal() {
    hideModal();
    callMessage(1, '已取消操作！');
}

// 封装发送ajax请求的函数
function sendUrl(url, type, data, successCallbackUrl, failedCallbackUrl) {
    // console.log(typeof data);
    $.ajax({
        url: url,
        type: type,
        data: data,
        success: function (resp) {
            console.log(resp);
            if (resp)
                callMessage(resp.code, resp.msg);
            // 成功处理逻辑

            if (resp.code === 0) {
                if (typeof (successCallbackUrl) != "undefined" && successCallbackUrl)
                    sleep(sleepTime).then(()=> window.location.href = successCallbackUrl);

            } else {
                if (typeof (failedCallbackUrl) != "undefined" && failedCallbackUrl)
                    sleep(sleepTime).then(() => window.location.href = failedCallbackUrl);
            }
        },
        error: function (resp) {
            console.log(resp)
            // 发生错误时处理逻辑
            callMessage(-1, "***哎呀出错啦，请稍后再试吧！");
            // sleep(sleepTime).then(()=> window.location.href = getProjectPath() + '/error');
        }
    });
};

// 封装单个 data 为Json数据
function doSingleDataToJson(data) {
    let formData = {
        'data': data
    };
    return JSON.parse(JSON.stringify(formData));
}

// 格式化日期时间
function formatDateTime(date) {
    let month = (date.getMonth() + 1);
    let curDay = date.getDate();
    let hour = date.getHours();
    let minutes = date.getMinutes();
    let seconds = date.getSeconds();
    return date.getFullYear() + "-" +
        (month < 10 ? ('0' + month) : month) + "-" +
        (curDay < 10 ? ('0' + curDay) : curDay) + " " +
        (hour < 10 ? ('0' + hour) : hour) + ":" +
        (minutes < 10 ? ('0' + minutes) : minutes) + ":" +
        (seconds < 10 ? ('0' + seconds) : seconds);
}
function getDateTime() {
    let nowDate = new Date();
    return formatDateTime(nowDate);
}
// 格式化日期 封装函数
function FormatDate(date) { //参数是时间
    let myDate = new Date(date);
    return formatDateTime(myDate);
}