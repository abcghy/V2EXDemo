package info.gaohuiyu.v2exdemo.data.model


data class Topic(
    val title: String?, val id: Long, val publisher: Member?,
    val node: Node?, val lastReplyTime: String?, val lastReplyMember: Member?,
    val replyCount: Int
)

data class Member(val avatar: String?, val name: String?)
data class Node(val name: String?)

data class Comment(val content: String, val commenter: Member, val loveCount: String?, val time: String, val order: String)
data class Subtitle(val content: String, val time: String)
data class TopicDetail(
    val title: String, val content: String?, val lastReplyTime: String?,
    val replyCount: String?, val node: String, val publisher: Member?,
    val publishTime: String?, val clickCount: String?
)
data class CommentHeader(val replyCount: String?, val filter: Boolean)
data class CommentResponse(val comments: List<Comment>, val currentPage: Int, val totalPage: Int)

data class TopicDetailResponse(
    val topicDetail: TopicDetail,
    val subtitles: List<Subtitle>,
    val commentResponse: CommentResponse
) {
    fun toList(): List<Any> {
        val list = ArrayList<Any>()
        list.add(topicDetail)
        list.addAll(subtitles)
        list.add(CommentHeader(topicDetail.replyCount, true))
        list.addAll(commentResponse.comments)
        return list
    }
}