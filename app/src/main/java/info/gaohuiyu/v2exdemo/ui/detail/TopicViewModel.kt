package info.gaohuiyu.v2exdemo.ui.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import info.gaohuiyu.v2exdemo.data.model.TopicDetail
import info.gaohuiyu.v2exdemo.data.repository.TopicRepository

class TopicViewModel(val topicRepository: TopicRepository, val topicId: Long) : ViewModel() {

    private var currentPage = 1

    private var _topicDetailLiveData = MediatorLiveData<TopicDetail>()
    val topicDetailLiveData: LiveData<TopicDetail>
        get() = _topicDetailLiveData

    fun refresh() {
        currentPage = 1
        val apiSource = topicRepository.getTopicDetail(topicId, currentPage)
        _topicDetailLiveData.addSource(apiSource) {
            _topicDetailLiveData.removeSource(apiSource)
            _topicDetailLiveData.value = it
        }
    }

    fun loadMore() {
        currentPage++
        val apiSource = topicRepository.getTopicDetail(topicId, currentPage)
        _topicDetailLiveData.addSource(apiSource) {
            _topicDetailLiveData.removeSource(apiSource)
            _topicDetailLiveData.value?.currentPage = it?.currentPage!!
            _topicDetailLiveData.value?.totalPage = it.totalPage
            _topicDetailLiveData.value?.comments?.addAll(it.comments!!)
            _topicDetailLiveData.value = _topicDetailLiveData.value
        }
    }
}