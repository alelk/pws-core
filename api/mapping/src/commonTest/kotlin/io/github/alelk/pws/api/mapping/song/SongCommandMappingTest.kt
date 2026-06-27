package io.github.alelk.pws.api.mapping.song

import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.TonalityDto
import io.github.alelk.pws.api.contract.core.YearDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.person.Person
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * PATCH-semantics tests for [SongUpdateRequestDto.toDomainCommand]. The mapper uses
 * `OptionalField.fromNullable(..., treatNullAsClear = false)`, so an **absent** field must become
 * `Unchanged` (never `Clear`) and a **present** field must become `Set(value)`. Getting this wrong
 * would silently wipe or fail to update song fields.
 */
class SongCommandMappingTest : StringSpec({

  val id = SongIdDto(7L)

  "absent optional fields map to Unchanged" {
    val cmd = SongUpdateRequestDto(id = id).toDomainCommand()

    cmd.id shouldBe SongId(7L)
    cmd.name shouldBe null
    cmd.locale shouldBe null
    cmd.lyric shouldBe null
    cmd.author shouldBe OptionalField.Unchanged
    cmd.translator shouldBe OptionalField.Unchanged
    cmd.composer shouldBe OptionalField.Unchanged
    cmd.year shouldBe OptionalField.Unchanged
    cmd.bibleRef shouldBe OptionalField.Unchanged
    cmd.tonalities shouldBe OptionalField.Unchanged
  }

  "present optional fields map to Set(value)" {
    val tonality = TonalityDto.entries.first()
    val cmd = SongUpdateRequestDto(
      id = id,
      name = "New Name",
      author = PersonDto("Author"),
      translator = PersonDto("Translator"),
      composer = PersonDto("Composer"),
      year = YearDto(1999),
      bibleRef = "John 3:16",
      tonalities = listOf(tonality),
    ).toDomainCommand()

    cmd.name?.value shouldBe "New Name"
    cmd.author shouldBe OptionalField.Set(Person("Author"))
    cmd.translator shouldBe OptionalField.Set(Person("Translator"))
    cmd.composer shouldBe OptionalField.Set(Person("Composer"))
    cmd.year shouldBe OptionalField.Set(Year(1999))
    cmd.bibleRef shouldBe OptionalField.Set(BibleRef("John 3:16"))
    cmd.tonalities shouldBe OptionalField.Set(listOf(tonality.toDomain()))
  }

  "the path-id overload uses the supplied song id, not the body" {
    val cmd = SongUpdateRequestDto(id = SongIdDto(1L), name = "x").toDomainCommand(SongIdDto(99L))
    cmd.id shouldBe SongId(99L)
  }

  "blank name is rejected" {
    io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
      SongUpdateRequestDto(id = id, name = "   ").toDomainCommand()
    }
  }
})
