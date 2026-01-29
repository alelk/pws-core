package io.github.alelk.pws.domain.core.ids

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.checkAll

class BookIdTest : StringSpec({
  "create book id from valid string contains latin letters and digits" {
    BookId.parse("Text-1234").identifier shouldBe "Text-1234"
  }

  "create book id from valid string contains cyrillic" {
    BookId.parse("Текст_1234").identifier shouldBe "Текст_1234"
  }

  "create book id fails when string starts from digit" {
    shouldThrow<IllegalArgumentException> {
      BookId.parse("1Book")
    }.message shouldContain "book id should"
  }

  "create book id fails when string ends with underscore" {
    shouldThrow<IllegalArgumentException> {
      BookId.parse("Book123_")
    }.message shouldContain "book id should"
  }

  "convert book id to string and parse it back for random book id" {
    checkAll(Arb.Companion.bookId()) { bookId ->
      val string = bookId.toString()
      val parsed = BookId.parse(string)
      parsed shouldBe bookId
    }
  }
})