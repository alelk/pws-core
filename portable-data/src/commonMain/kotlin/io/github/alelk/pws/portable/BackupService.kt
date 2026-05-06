package io.github.alelk.pws.portable

import io.github.alelk.pws.portable.model.Backup

class BackupService {
  fun writeAsString(backup: Backup): String = yamlConverter.encodeToString(Backup.serializer(), backup)
  fun readFromString(backupString: String): Backup = yamlConverter.decodeFromString(Backup.serializer(), backupString)
}