package info.gaohuiyu.v2exdemo.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import info.gaohuiyu.v2exdemo.data.model.Member

@Dao
interface MemberDao {
    @Insert
    fun insert(member: Member)
}