package info.gaohuiyu.v2exdemo.ui

import android.arch.lifecycle.*
import com.blankj.utilcode.util.SPUtils
import info.gaohuiyu.v2exdemo.data.model.AbsentLiveData
import info.gaohuiyu.v2exdemo.data.model.Tab
import info.gaohuiyu.v2exdemo.data.model.Topic
import info.gaohuiyu.v2exdemo.data.repository.TopicRepository
import info.gaohuiyu.v2exdemo.domain.Resource
import info.gaohuiyu.v2exdemo.util.getObj
import info.gaohuiyu.v2exdemo.util.putObj

class MainViewModel: ViewModel {
    var topicRepository: TopicRepository? = null

    private val _currentTab = MutableLiveData<Tab>()
    val currentTab: LiveData<Tab>
        get() = _currentTab

    val topics: LiveData<Resource<List<Topic>>> = Transformations.switchMap(_currentTab) { tab ->
        if (tab == null) {
            AbsentLiveData.create()
        } else {
            topicRepository?.getTopicsByTab(tab)
        }
    }

    constructor(topicRepository: TopicRepository) : super() {
        this.topicRepository = topicRepository
        _currentTab.value = SPUtils.getInstance().getObj("lastTab", Tab::class.java)

        _currentTab.observeForever {
            if (it != null) {
                SPUtils.getInstance().putObj("lastTab", it)
            }
        }
    }

    val tabs = listOf(Tab("技术", "tech"),
            Tab("创意", "creative"),
            Tab("好玩", "play"),
            Tab("Apple", "apple"),
            Tab("酷工作", "jobs"),
            Tab("交易", "deals"),
            Tab("城市", "city"),
            Tab("问与答", "qna"),
            Tab("最热", "hot"),
            Tab("全部", "all"),
            Tab("R2", "r2"))

    fun selectTag(pos: Int) {
        val tab = tabs[pos]
        _currentTab.value = tab
    }

    fun refresh() {
        if (_currentTab.value == null) {
            _currentTab.value = tabs[8]
        } else {
            _currentTab.value = _currentTab.value
        }
    }
}