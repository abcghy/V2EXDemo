package info.gaohuiyu.v2exdemo.data.api

import java.net.UnknownHostException

sealed class ApiResponse<T> {
    companion object {
        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            var message: String = when (error) {
                is UnknownHostException -> "网络连接失败"
                else -> error.message ?: "Unknown Error"
            }
            return ApiErrorResponse(message)
        }

        fun <T> create(body: T?): ApiResponse<T> {
            if (body == null) {
                return ApiEmptyResponse()
            }
            return ApiSuccessResponse(body)
        }
    }
}

class ApiEmptyResponse<T> : ApiResponse<T>()

class ApiErrorResponse<T>(val errorMessage: String) : ApiResponse<T>()

class ApiSuccessResponse<T>(val body: T): ApiResponse<T>()