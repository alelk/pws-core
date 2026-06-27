package io.github.alelk.pws.database.installed_book

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.alelk.pws.domain.booklibrary.model.BookInstallSource
import io.github.alelk.pws.domain.core.ids.BookId
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalledBookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InstalledBookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<InstalledBookEntity>)

    @Query("SELECT * FROM installed_books")
    fun observeAll(): Flow<List<InstalledBookEntity>>

    @Query("SELECT * FROM installed_books WHERE book_id = :bookId")
    suspend fun getByBookId(bookId: BookId): InstalledBookEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM installed_books WHERE source = :source LIMIT 1)")
    suspend fun existsBySource(source: BookInstallSource): Boolean

    @Query("DELETE FROM installed_books WHERE book_id = :bookId")
    suspend fun deleteByBookId(bookId: BookId)
}
