/**
 * 校验手机号
 */
export function validatePhone(rule, value, callback) {
    const phoneReg = /^1[3-9]\d{9}$/
    if (value && !phoneReg.test(value)) {
        callback(new Error('请输入正确的手机号码'))
    } else {
        callback()
    }
}

/**
 * 校验邮箱
 */
export function validateEmail(rule, value, callback) {
    const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
    if (value && !emailReg.test(value)) {
        callback(new Error('请输入正确的邮箱地址'))
    } else {
        callback()
    }
}