package io.github.alelk.pws.api.mapping.book

import io.github.alelk.pws.api.contract.core.YearDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.book.BookUpdateRequestDto
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.ids.BookId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * PATCH-semantics tests for [BookUpdateRequestDto.toDomainCommand]: absent → `Unchanged`,
 * present → `Set(value)` for `releaseDate` / `description` / `preface`.
 */
class BookCommandMappingTest : StringSpec({

  val id = BookIdDto("Book-1")

  "absent optional fields map to Unchanged" {
    val cmd = BookUpdateRequestDto().toDomainCommand(id)

    cmd.id shouldBe BookId.parse("Book-1")
    cmd.releaseDate shouldBe OptionalField.Unchanged
    cmd.description shouldBe OptionalField.Unchanged
    cmd.preface shouldBe OptionalField.Unchanged
    cmd.name shouldBe null
    cmd.priority shouldBe null
  }

  "present optional fields map to Set(value)" {
    val cmd = BookUpdateRequestDto(
      name = "New Book",
      releaseDate = YearDto(2001),
      description = "desc",
      preface = "preface",
      priority = 5,
    ).toDomainCommand(id)

    cmd.name?.value shouldBe "New Book"
    cmd.releaseDate shouldBe OptionalField.Set(Year(2001))
    cmd.description shouldBe OptionalField.Set("desc")
    cmd.preface shouldBe OptionalField.Set("preface")
    cmd.priority shouldBe 5
  }
})
