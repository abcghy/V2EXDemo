package info.gaohuiyu.v2exdemo.data.model
import com.google.gson.annotations.SerializedName

data class Node(
    @SerializedName("avatar_large") val avatarLarge: String?, // //cdn.v2ex.com/navatar/c4ca/4238/1_large.png?m=1494924246
    @SerializedName("name") val name: String?, // babel
    @SerializedName("avatar_normal") val avatarNormal: String?, // //cdn.v2ex.com/navatar/c4ca/4238/1_normal.png?m=1494924246
    @SerializedName("title") val title: String?, // Project Babel
    @SerializedName("url") val url: String?, // https://www.v2ex.com/go/babel
    @SerializedName("topics") val topics: Int?, // 1122
    @SerializedName("footer") val footer: String?, // V2EX 基于 Project Babel 驱动。Project Babel 是用 Python 语言写成的，运行于 Google App Engine 云计算平台上的社区软件。Project Babel 当前开发分支 2.5。最新版本可以从 <a href="http://github.com/livid/v2ex" target="_blank">GitHub</a> 获取。
    @SerializedName("header") val header: String?, // Project Babel - 帮助你在云平台上搭建自己的社区
    @SerializedName("title_alternative") val titleAlternative: String?, // Project Babel
    @SerializedName("avatar_mini") val avatarMini: String?, // //cdn.v2ex.com/navatar/c4ca/4238/1_mini.png?m=1494924246
    @SerializedName("stars") val stars: Int?, // 378
    @SerializedName("root") val root: Boolean?, // false
    @SerializedName("id") val id: Int?, // 1
    @SerializedName("parent_node_name") val parentNodeName: String? // v2ex
)