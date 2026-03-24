package io.github.alelk.pws.database

import androidx.room.Room
import androidx.sqlite.driver.NativeSQLiteDriver

actual fun pwsDbForTest(inMemory: Boolean, name: String): PwsDatabase =
  if (inMemory) Room.inMemoryDatabaseBuilder<PwsDatabase>().setDriver(NativeSQLiteDriver()).build()
  else Room.databaseBuilder<PwsDatabase>(name).setDriver(NativeSQLiteDriver()).build()
