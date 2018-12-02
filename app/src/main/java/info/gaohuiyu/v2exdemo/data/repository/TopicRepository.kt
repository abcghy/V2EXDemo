package info.gaohuiyu.v2exdemo.data.repository

import android.arch.lifecycle.LiveData
import info.gaohuiyu.v2exdemo.data.api.V2EXApi
import info.gaohuiyu.v2exdemo.data.model.Topic
import info.gaohuiyu.v2exdemo.data.model.TopicDetail

class TopicRepository(val v2EXApi: V2EXApi) {
    fun getHotTopics(): LiveData<List<Topic>> {
        return v2EXApi.getHotTopics()
    }

    fun getTopicDetail(topicId: Long, page: Int): LiveData<TopicDetail> {
        return v2EXApi.getTopicDetail(topicId, page)
    }
}