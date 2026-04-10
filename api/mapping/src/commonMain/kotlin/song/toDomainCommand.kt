package io.github.alelk.pws.api.mapping.song

import io.github.alelk.pws.api.contract.core.error.FieldError
import io.github.alelk.pws.api.contract.core.error.ValidationErrors
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongCreateRequestDto
import io.github.alelk.pws.api.contract.song.SongUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.nonEmpty
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.api.mapping.core.validateNonEmpty
import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand

// ---- Throwing variants (legacy, kept for compatibility) ----

fun SongCreateRequestDto.toDomainCommand(): CreateSongCommand = CreateSongCommand(
  id = id.toDomain(),
  locale = locale.toDomain(),
  name = nonEmpty(name, "name"),
  lyric = lyric.toDomain(),
  author = author?.toDomain(),
  translator = translator?.toDomain(),
  composer = composer?.toDomain(),
  tonalities = tonalities?.map { it.toDomain() },
  year = year?.toDomain(),
  bibleRef = bibleRef?.let { BibleRef(it) },
  edited = edited
)

fun SongUpdateRequestDto.toDomainCommand(): UpdateSongCommand = UpdateSongCommand(
  id = id.toDomain(),
  locale = locale?.toDomain(),
  name = name?.let { nonEmpty(it, "name") },
  lyric = lyric?.toDomain(),
  author = OptionalField.fromNullable(author?.toDomain(), treatNullAsClear = false),
  translator = OptionalField.fromNullable(translator?.toDomain(), treatNullAsClear = false),
  composer = OptionalField.fromNullable(composer?.toDomain(), treatNullAsClear = false),
  tonalities = tonalities?.map { it.toDomain() }?.let { OptionalField.Set(it) } ?: OptionalField.Unchanged,
  year = OptionalField.fromNullable(year?.toDomain(), treatNullAsClear = false),
  bibleRef = bibleRef?.let { OptionalField.Set(BibleRef(it)) } ?: OptionalField.Unchanged,
  expectVersion = expectedVersion?.toDomain()
)

/** Convert to UpdateSongCommand using the song ID from the URL path. */
fun SongUpdateRequestDto.toDomainCommand(songId: SongIdDto): UpdateSongCommand = UpdateSongCommand(
  id = songId.toDomain(),
  locale = locale?.toDomain(),
  name = name?.let { nonEmpty(it, "name") },
  lyric = lyric?.toDomain(),
  author = OptionalField.fromNullable(author?.toDomain(), treatNullAsClear = false),
  translator = OptionalField.fromNullable(translator?.toDomain(), treatNullAsClear = false),
  composer = OptionalField.fromNullable(composer?.toDomain(), treatNullAsClear = false),
  tonalities = tonalities?.map { it.toDomain() }?.let { OptionalField.Set(it) } ?: OptionalField.Unchanged,
  year = OptionalField.fromNullable(year?.toDomain(), treatNullAsClear = false),
  bibleRef = bibleRef?.let { OptionalField.Set(BibleRef(it)) } ?: OptionalField.Unchanged,
  expectVersion = expectedVersion?.toDomain()
)

// ---- Validated variants — throw ValidationErrorsException (caught by StatusPages → 422) ----

/**
 * Validate [SongCreateRequestDto] and convert to [CreateSongCommand].
 * Throws [ValidationErrorsException] with structured per-field errors on invalid input.
 * StatusPages catches this and returns HTTP 422 with a detailed error body.
 */
fun SongCreateRequestDto.toDomainCommandValidated(): CreateSongCommand {
  val errors = buildList<FieldError> {
    validateNonEmpty(name, "name")?.let { add(it) }
    if (locale.value.isBlank()) add(FieldError("locale", "must not be blank"))
    if (lyric.isEmpty()) add(FieldError("lyric", "must not be empty"))
  }
  if (errors.isNotEmpty()) throw ValidationErrorsException(ValidationErrors(errors))
  return toDomainCommand()
}

/**
 * Validate [SongUpdateRequestDto] and convert to [UpdateSongCommand] using ID from URL path.
 * Throws [ValidationErrorsException] with structured per-field errors on invalid input.
 */
fun SongUpdateRequestDto.toDomainCommandValidated(songId: SongIdDto): UpdateSongCommand {
  val errors = buildList<FieldError> {
    name?.let { validateNonEmpty(it, "name")?.let { e -> add(e) } }
  }
  if (errors.isNotEmpty()) throw ValidationErrorsException(ValidationErrors(errors))
  return toDomainCommand(songId)
}

/** Carries structured [ValidationErrors] — caught by StatusPages, returns HTTP 422. */
class ValidationErrorsException(val errors: ValidationErrors) :
  IllegalArgumentException(errors.toString())
