package info.gaohuiyu.v2exdemo.util


fun getValueByRegex(input: CharSequence, pattern: String): String {
    return Regex(pattern)
        .find(input)?.groups?.get(1)?.value!!
}