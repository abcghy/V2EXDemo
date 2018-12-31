package info.gaohuiyu.v2exdemo.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import info.gaohuiyu.v2exdemo.data.model.Topic

@Dao
interface TopicDao {
    @Query("SELECT * FROM topic ORDER BY replyCount DESC")
    fun getTopicByNode(): LiveData<List<Topic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllByNode(topics: List<Topic>)
}