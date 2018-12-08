package info.gaohuiyu.v2exdemo.data.api

sealed class ApiResponse<T> {
    companion object {
        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(error.message ?: "Unknown Error")
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