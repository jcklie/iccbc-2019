package com.mrklie.yangtao.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HanziDao {
    @Query("SELECT * FROM hanzi")
    fun selectAll(): List<Hanzi>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(characters: List<Hanzi>)
}