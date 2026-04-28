package io.github.alelk.pws.data.repository.room.transaction

import androidx.room.withTransaction
import io.github.alelk.pws.database.PwsDatabase
import io.github.alelk.pws.domain.core.transaction.RoTransactionScope
import io.github.alelk.pws.domain.core.transaction.RwTransactionScope
import io.github.alelk.pws.domain.core.transaction.TransactionRunner

/**
 * Room-backed [TransactionRunner].
 * Both read-only and read-write scopes delegate to [RoomDatabase.withTransaction].
 */
class RoomTransactionRunner(private val db: PwsDatabase) : TransactionRunner {

  override suspend fun <T> inRoTransaction(block: suspend RoTransactionScope.() -> T): T =
    db.withTransaction { block(object : RoTransactionScope {}) }

  override suspend fun <T> inRwTransaction(block: suspend RwTransactionScope.() -> T): T =
    db.withTransaction { block(object : RwTransactionScope {}) }
}

