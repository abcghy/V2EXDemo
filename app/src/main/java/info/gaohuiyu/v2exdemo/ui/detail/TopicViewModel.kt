package info.gaohuiyu.v2exdemo.ui.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import info.gaohuiyu.v2exdemo.data.api.ApiEmptyResponse
import info.gaohuiyu.v2exdemo.data.api.ApiErrorResponse
import info.gaohuiyu.v2exdemo.data.api.ApiSuccessResponse
import info.gaohuiyu.v2exdemo.data.repository.TopicRepository

class TopicViewModel(val topicRepository: TopicRepository, val topicId: Long) : ViewModel() {

    private var currentPage = 1

    private var _topicLiveData = MediatorLiveData<List<Any>>()
    val topicLiveData: LiveData<List<Any>>
        get() = _topicLiveData

    private var _isMore = MutableLiveData<Boolean>()
    val isMore: LiveData<Boolean>
        get() = _isMore

    fun refresh() {
        currentPage = 1
        val apiSource = topicRepository.getTopicDetailResponse(topicId, currentPage)
        _topicLiveData.addSource(apiSource) {
            _topicLiveData.removeSource(apiSource)

            if (it != null) {
                when (it) {
                    is ApiSuccessResponse -> {
                        _topicLiveData.value = it.body.toList()
                        _isMore.value = it.body.commentResponse.run {
                            this.totalPage != this.currentPage
                        }
                    }
                    is ApiErrorResponse -> {
                        // todo
                    }
                    is ApiEmptyResponse -> {
                        // todo 告诉用户，为空
                        _topicLiveData.value = null
                    }
                }
            }
        }
    }

    fun loadMore() {
//        currentPage++
//        val apiSource = topicRepository.getTopicDetail(topicId, currentPage)
//        _topicDetailLiveData.addSource(apiSource) {
//            _topicDetailLiveData.removeSource(apiSource)
//            _topicDetailLiveData.value?.currentPage = it?.currentPage!!
//            _topicDetailLiveData.value?.totalPage = it.totalPage
//            _topicDetailLiveData.value?.commentResponse?.addAll(it.commentResponse!!)
//            _topicDetailLiveData.value = _topicDetailLiveData.value
//        }
    }
}