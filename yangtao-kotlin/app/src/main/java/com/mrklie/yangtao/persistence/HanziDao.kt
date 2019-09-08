package com.mrklie.yangtao.persistence

import androidx.room.*

@Dao
interface HanziDao {
    @Query("SELECT * FROM hanzi WHERE hanzi=:character")
    fun selectById(character: String): Hanzi

    @Query("SELECT * FROM hanzi")
    fun selectAll(): List<Hanzi>

    @Update
    fun update(hanzi: Hanzi)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(characters: List<Hanzi>)
}