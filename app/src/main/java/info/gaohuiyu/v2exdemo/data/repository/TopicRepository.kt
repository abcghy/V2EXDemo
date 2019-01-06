package info.gaohuiyu.v2exdemo.data.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.support.annotation.WorkerThread
import info.gaohuiyu.v2exdemo.AppExecutors
import info.gaohuiyu.v2exdemo.data.api.ApiErrorResponse
import info.gaohuiyu.v2exdemo.data.api.ApiResponse
import info.gaohuiyu.v2exdemo.data.api.ApiSuccessResponse
import info.gaohuiyu.v2exdemo.data.api.V2EXApi
import info.gaohuiyu.v2exdemo.data.db.AppDatabase
import info.gaohuiyu.v2exdemo.data.model.CommentResponse
import info.gaohuiyu.v2exdemo.data.model.Tab
import info.gaohuiyu.v2exdemo.data.model.Topic
import info.gaohuiyu.v2exdemo.data.model.TopicDetailResponse
import info.gaohuiyu.v2exdemo.domain.Resource

class TopicRepository(val v2EXApi: V2EXApi, val db: AppDatabase) {
    fun getTopicsByTab(tab: Tab): LiveData<Resource<List<Topic>>> {
        val liveData = MediatorLiveData<Resource<List<Topic>>>()

        val dbSource = db.topicDao().getTopicByTab()
        liveData.addSource(dbSource) {
            liveData.removeSource(dbSource)

            liveData.value = Resource.success(it)

            val apiSource = v2EXApi.getTopicsByTab(tab)
            liveData.addSource(apiSource) {
                liveData.removeSource(apiSource)

                when (it) {
                    is ApiSuccessResponse -> {
                        liveData.value = Resource.success(it.body)

                        AppExecutors.diskIO().submit {
                            setMainTopics(it.body)
                        }
                    }
                    is ApiErrorResponse -> {
                        liveData.value = Resource.error(it.errorMessage, null)
                    }
                }
            }
        }
        return liveData
    }

    fun getTopicDetailResponse(topicId: Long, page: Int): LiveData<ApiResponse<TopicDetailResponse>> {
        return v2EXApi.getTopicDetailResponse(topicId)
    }

    fun getTopicDetailComments(topicId: Long, page: Int): LiveData<ApiResponse<CommentResponse>> {
        return v2EXApi.getTopicComments(topicId, page)
    }

    fun setMainTopics(topics: List<Topic>) {
        db.topicDao().deleteAllTopic()
        db.topicDao().insertAllByNode(topics)
    }
}