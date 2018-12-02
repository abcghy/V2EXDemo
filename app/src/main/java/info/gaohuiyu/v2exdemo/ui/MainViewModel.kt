package info.gaohuiyu.v2exdemo.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import info.gaohuiyu.v2exdemo.data.model.Topic
import info.gaohuiyu.v2exdemo.data.repository.TopicRepository

class MainViewModel(val topicRepository: TopicRepository): ViewModel() {
    private val _topics = MediatorLiveData<List<Topic>>()
    val topics : LiveData<List<Topic>>
        get() = _topics

    fun loadFirstPage() {
        val apiSource = topicRepository.getHotTopics()
        _topics.addSource(apiSource) {
            _topics.removeSource(apiSource)
            _topics.value = it
        }
    }
}