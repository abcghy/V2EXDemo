package info.gaohuiyu.v2exdemo.data.repository

import android.arch.lifecycle.LiveData
import info.gaohuiyu.v2exdemo.data.api.ApiResponse
import info.gaohuiyu.v2exdemo.data.api.V2EXApi
import info.gaohuiyu.v2exdemo.data.model.CommentResponse
import info.gaohuiyu.v2exdemo.data.model.Topic
import info.gaohuiyu.v2exdemo.data.model.TopicDetailResponse

class TopicRepository(val v2EXApi: V2EXApi) {
    fun getHotTopics(): LiveData<List<Topic>> {
        return v2EXApi.getHotTopics()
    }

    fun getTopicDetailResponse(topicId: Long, page: Int): LiveData<ApiResponse<TopicDetailResponse>> {
        return v2EXApi.getTopicDetailResponse(topicId)
    }

    fun getTopicDetailComments(topicId: Long, page: Int): LiveData<ApiResponse<CommentResponse>> {
        return v2EXApi.getTopicComments(topicId, page)
    }
}