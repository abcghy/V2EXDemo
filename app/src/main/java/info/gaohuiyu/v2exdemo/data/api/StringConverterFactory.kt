package info.gaohuiyu.v2exdemo.data.api

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class StringConverterFactory: Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (String::class.java == type) {
            return ToStringConverter()
        }
        return null
    }

    class ToStringConverter: Converter<ResponseBody, String> {
        override fun convert(value: ResponseBody): String? {
            return value.string()
        }
    }
}