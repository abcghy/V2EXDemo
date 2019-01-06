package info.gaohuiyu.v2exdemo.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import info.gaohuiyu.v2exdemo.AppExecutors
import info.gaohuiyu.v2exdemo.data.api.ApiErrorResponse
import info.gaohuiyu.v2exdemo.data.api.ApiSuccessResponse
import info.gaohuiyu.v2exdemo.data.model.Topic
import info.gaohuiyu.v2exdemo.data.repository.TopicRepository
import info.gaohuiyu.v2exdemo.domain.Resource

class MainViewModel(val topicRepository: TopicRepository): ViewModel() {
    private val _topics = MediatorLiveData<Resource<List<Topic>>>()
    val topics : LiveData<Resource<List<Topic>>>
        get() = _topics

    fun getLastData() {
        val dbSource = topicRepository.loadHotTopicsFromDb()
        _topics.addSource(dbSource) {
            _topics.removeSource(dbSource)
            _topics.value = Resource.success(it)
        }
    }

    fun refresh() {
        val apiSource = topicRepository.getHotTopics()

        _topics.addSource(apiSource) {
            _topics.removeSource(apiSource)

            when (it) {
                is ApiSuccessResponse -> {
                    _topics.value = Resource.success(it.body)

                    AppExecutors.diskIO().submit {
                        topicRepository.setMainTopics(it.body)
                    }
                }
                is ApiErrorResponse -> {
                    _topics.value = Resource.error(it.errorMessage, null)
                }
            }
        }
    }
}