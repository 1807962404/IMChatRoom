/**
 * 登陆注册js文件
 * @type {Element}
 */

window.onload = () => {
    let verifyCodeToSignIn = document.getElementsByClassName('verifyCode')[1];
    if (verifyCodeToSignIn)
        changeCheckCode(verifyCodeToSignIn, true)   // 加载验证码
}

const container = document.getElementsByClassName('container')[0];
const signIn = document.getElementById('sign-in');
const signUp = document.getElementById('sign-up');
const verifyCode = document.getElementsByClassName('verifyCode');
// 每次切换选项卡都需要更新验证码
if (signUp)
    signUp.addEventListener('click', (evt) => {
        container.classList.add('active');
        changeCheckCode(verifyCode[0], false);
    });
if (signIn)
    signIn.addEventListener('click', (evt) => {
        container.classList.remove('active');
        changeCheckCode(verifyCode[1], true);
    });

//验证码点击切换事件。注册：false；登陆：true
function changeCheckCode(img, isSignIn) {
    const timestamp = new Date().getTime();
    img.src = "entity/verify-code/" + timestamp;
    // console.log(img.src);

    // 用于验证 输入验证码的正确与否
    var identify = document.getElementsByClassName('identify');
    if (isSignIn)
        identify[1].value = timestamp;
    else
        identify[0].value = timestamp;
}

// 校验邮箱【或账号】
function checkEmailVal(emailVal, isSignIn) {

    let email_form = /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/;
    // var email_form = /^[0-9a-zA-Z_.-]+[@][0-9a-zA-Z_.-]+([.][a-zA-Z]+){1,2}$/;
    let error_email = document.getElementsByClassName('error_email');
    if (checkContentIsEmpty(emailVal))
        return false;

    let emailValLength = emailVal.length;

    if (isSignIn) {     // 如果为登陆：检查账号或邮箱
        if (emailValLength < 6) {
            error_email[1].innerText = '·请输入正确的账号或邮箱地址！';
            return false;
        } else {
            error_email[1].innerText = '';
            if (emailValLength !== 6) {
                if (emailValLength !== 8) {     // 排除掉账号
                    if (!email_form.test(emailVal)) {
                        error_email[1].innerText = '·请输入正确的邮箱地址！';
                        return false;
                    }
                }
            }
        }

    } else {        // 否则为注册，只需检查邮箱
        if (!email_form.test(emailVal)) {
            error_email[0].innerText = '·请输入正确的邮箱地址！';
            return false;
        } else {
            error_email[0].innerText = '';
        }
    }

    return true;
}

// 校验密码
function checkPasswordVal(pwdVal, isSignIn) {

    var error_pwd = document.getElementsByClassName('error_pwd');
    if (checkContentIsEmpty(pwdVal) || pwdVal.length < 6) {
        if (isSignIn) {
            error_pwd[1].innerText = '·请检查密码格式！';
            return false;
        } else {
            error_pwd[0].innerText = '·请检查密码格式！';
            return false;
        }
    } else {
        if (isSignIn) {
            error_pwd[1].innerText = '';
        } else {
            error_pwd[0].innerText = '';
        }
    }
    return true;
}

// 校验验证码
function checkVerifyCodeVal(codeVal, isSignIn) {
    var error_code = document.getElementsByClassName('error_code');
    if (checkContentIsEmpty(codeVal) || codeVal.length != 4) {
        if (isSignIn) {
            error_code[1].innerText = '·请检查验证码是否输入正确！';
            return false;
        } else {
            error_code[0].innerText = '·请检查验证码是否输入正确！';
            return false;
        }
    } else {
        if (isSignIn) {
            error_code[1].innerText = '';
        } else {
            error_code[0].innerText = '';
        }
    }
    return true;
}

// 注册用户时，校验昵称
function checkNicknameVal(nicknameVal) {
    var error_nickname = document.getElementsByClassName('error_nickname')[0];
    if (checkContentIsEmpty(nicknameVal) || nicknameVal.length < 3 || nicknameVal.length > 15) {
        error_nickname.innerText = '·请检查昵称格式！';
        return false;
    } else {
        error_nickname.innerText = '';
    }
    return true;
}

// 表单提交的数据校验
function checkVal(isSignIn) {

    let emailVal = isSignIn ?
        document.querySelector('#signin input[name="email"]').value :
        document.querySelector('#signup input[name="email"]').value;
    if (!checkEmailVal(emailVal, isSignIn)) {
        callMessage(1, "请检查邮箱地址的格式！");
        return false;
    }

    let passwordVal = isSignIn ?
        document.querySelector('#signin input[name="password"]').value :
        document.querySelector('#signup input[name="password"]').value;
    if (!checkPasswordVal(passwordVal, isSignIn)) {
        callMessage(1, "请检查密码格式！");
        return false;
    }

    let verifyCodeVal = isSignIn ?
        document.querySelector('#signin input[name="verifyCode"]').value :
        document.querySelector('#signup input[name="verifyCode"]').value;
    if (!checkVerifyCodeVal(verifyCodeVal, isSignIn)) {
        callMessage(1, "请检查验证码是否正确！");
        return false;
    }

    if (!isSignIn) {
        if (!checkNicknameVal(document.querySelector('#signup input[name="nickname"]').value)) {
            callMessage(1, "请检查昵称格式！");
            return false;
        }
    }

    return true;
}

// 获取表单数据
function getFormData(isSignIn) {
    let emailVal = isSignIn ?
        document.querySelector('#signin input[name="email"]').value :
        document.querySelector('#signup input[name="email"]').value;

    let passwordVal = isSignIn ?
        document.querySelector('#signin input[name="password"]').value :
        document.querySelector('#signup input[name="password"]').value;

    let verifyCodeVal = isSignIn ?
        document.querySelector('#signin input[name="verifyCode"]').value :
        document.querySelector('#signup input[name="verifyCode"]').value;

    let identifyVal = isSignIn ?
        document.querySelector('#signin input[name="identify"]').value :
        document.querySelector('#signup input[name="identify"]').value;

    let formData = {
        'email': emailVal,
        'password': passwordVal,
        'verifyCode': verifyCodeVal,
        'identify': identifyVal
    };
    if (!isSignIn) {
        let nicknameVal = document.querySelector('#signup input[name="nickname"]').value;
        formData['nickname'] = nicknameVal;
    };

    return JSON.parse(JSON.stringify(formData));    // 将表单数据转为JSON格式的字符串，然后转为JSON对象
}

// 用户登陆
function doSignIn() {

    // 1、进行数据校验
    if (!checkVal(true))
        return ;

    // 2、封装登陆表单的数据
    let data = getFormData(true);

    $.ajax({
        url: getProjectPath() + '/user/signin',
        type: 'POST',
        data: data,
        success: function (resp) {
            // console.log(resp);
            callMessage(resp.code, resp.msg);

            let verifyCodeElem = document.querySelector('#signin input[name="verifyCode"]');
            if (resp.code === 0) {
                // 登陆成功
                sessionStorage.setItem('signInUser', JSON.stringify(resp.data));
                sleep(sleepTime).then(()=> {window.location.href = getProjectPath() + '/main';});

            } else {
                // 登陆失败需要切换验证码
                changeCheckCode(verifyCode[1], true);
            }
            verifyCodeElem.value = '';
        },
        error: function (resp) {
            console.log(resp)
            // 发生错误时处理逻辑
            callMessage(-1, "***哎呀出错啦，请稍后再试吧！");
            // 登陆失败需要切换验证码
            changeCheckCode(verifyCode[1], true);
        }
    });
}

// 用户注册
function doSignUp() {

    // 1、进行数据校验
    if (!checkVal(false))
        return ;

    // 2、显示弹窗提醒
    let pElem = document.createElement('p');
    pElem.innerHTML = `请确认邮箱地址是否无误，错误的邮箱会可能使您无法找回账户！<br />
                <button class="opt-cancel" onclick="doFinalSignUp(true)">容我再想想？</button>
                <button onclick="doFinalSignUp(false)">确认注册</button>`;
    showModal(pElem);
}
function doFinalSignUp(isCancelled) {
    hideModal();
    if (!isCancelled) {
        // 3、封装登陆表单的数据
        let data = getFormData(false);
        showLoader();
        // 4、发送请求
        $.ajax({
            url: getProjectPath() + '/user/signup',
            type: 'POST',
            data: data,
            success: function (resp) {
                // console.log(resp);
                hiddenLoader();
                callMessage(resp.code, resp.msg);

                changeCheckCode(verifyCode[0], false);  // 更新验证码框
                document.querySelector('#signup input[name="verifyCode"]').value = '';  // 清空验证码框内容
            },
            error: function (resp) {
                hiddenLoader();
                console.log(resp);
                // 发生错误时处理逻辑
                callMessage(-1, "***哎呀出错啦，请稍后再试吧！");
                // 注册失败需要切换验证码
                changeCheckCode(verifyCode[0], false);
                // sleep(sleepTime).then(()=> window.location.href = getProjectPath() + '/error');
            }
        });

    } else {
        callMessage(1, "已取消操作");
        changeCheckCode(verifyCode[0], false);
    }
}

// 解散群聊
function doForgetPasswordBtnListener() {
    let container = document.createElement("div");
    container.innerHTML = `<input id="reset-email" type="email" placeholder="请输入需要重置密码的邮箱地址..." required /><br />`;
    let pElem = document.createElement('p');
    pElem.innerHTML = `请您确认是否需要重置此账户密码？<br/>
                        <button class="opt-cancel" onclick="doFinalForgetPasswordBtnListener(true)">容我想想？</button>
                        <button onclick="doFinalForgetPasswordBtnListener(false)">确认重置</button>`;
    container.appendChild(pElem);
    showModal(container);
}
function doFinalForgetPasswordBtnListener(isCancelld) {

    if (isCancelld) {
        hideModal();
        callMessage(1, "已取消操作！");
        return;
    }

    // 检查邮箱
    let resetEmail = document.querySelector("#customized-modal .custom-modal-content #reset-email");
    let emailVal = resetEmail.value;
    let email_form = /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/;
    if (checkContentIsEmpty(emailVal) || emailVal.length < 10 || !email_form.test(emailVal)) {
        callMessage(1, '请输入正确的邮箱地址！');
        return ;
    }

    if (!isCancelld) {
        showLoader();
        $.ajax({
            url: getProjectPath() + '/user/reset-password',
            type: 'POST',
            data: doSingleDataToJson(emailVal),
            success: (resp) => {
                hiddenLoader();
                callMessage(resp.code, resp.msg);

                if (resp.code === 0) {
                    resetEmail.value = '';
                    hideModal();
                }
            },
            error: (resp) => {
                console.log("Error: " + resp);
                hiddenLoader();
            }
        });
    }
}