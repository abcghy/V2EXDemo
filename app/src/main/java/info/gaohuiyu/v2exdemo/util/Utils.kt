package info.gaohuiyu.v2exdemo.util

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils


fun getValueByRegex(input: CharSequence, pattern: String): String {
    return Regex(pattern)
        .find(input)?.groups?.get(1)?.value!!
}

fun <T> SPUtils.getObj(key: String, klass: Class<T>): T? {
    val value = getString(key, null)
    return if (value == null) {
        null
    } else {
        GsonUtils.fromJson(value, klass)
    }
}

fun <T> SPUtils.putObj(key: String, obj: T) {
    put(key, GsonUtils.toJson(obj))
}