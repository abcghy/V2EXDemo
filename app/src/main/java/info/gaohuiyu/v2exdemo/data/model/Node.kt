package info.gaohuiyu.v2exdemo.data.model

import org.jsoup.Jsoup

data class Topic(val title: String?, val id: Long, val publisher: Member?,
                 val node: Node?, val lastReplyTime: String?, val lastReplyMember: Member?,
                 val replyCount: Int)
data class Member(val avatar: String?, val name: String?)
data class Node(val name: String?)

data class Comment(val content: String, val commenter: Member, val loveCount: String?, val time: String)
data class Subtitle(val content: String, val time: String)
data class TopicDetail(val title: String, val content: String?, val lastReplyTime: String?,
                       val replyCount: String?, val node: String, val publisher: Member?,
                       val publishTime: String?, val clickCount: String?,
                       val comments: MutableList<Comment>?, val subtitles: List<Subtitle>?,
                       var currentPage: Int, var totalPage: Int)