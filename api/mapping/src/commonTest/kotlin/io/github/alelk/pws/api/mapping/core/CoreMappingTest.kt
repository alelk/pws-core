package io.github.alelk.pws.api.mapping.core

import io.github.alelk.pws.domain.core.ids.bookId
import io.github.alelk.pws.domain.core.ids.songId
import io.github.alelk.pws.domain.core.ids.songNumberId
import io.github.alelk.pws.domain.core.ids.tagId
import io.github.alelk.pws.domain.core.color
import io.github.alelk.pws.domain.core.locale
import io.github.alelk.pws.domain.core.version
import io.github.alelk.pws.domain.core.year
import io.github.alelk.pws.domain.person.person
import io.github.alelk.pws.domain.tonality.tonality
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll

/**
 * Round-trip tests for the core value-object mappers. These conversions back the entire API
 * contract, and several are non-trivial (e.g. [Year.toDto] re-parses via `toString().toInt()`,
 * [SongNumberId] and [Version] round-trip through their string forms), so a silent regression here
 * would corrupt every DTO that embeds these types.
 */
class CoreMappingTest : StringSpec({

  "Locale round-trips" { checkAll(Arb.locale()) { it.toDto().toDomain() shouldBe it } }
  "Version round-trips" { checkAll(Arb.version()) { it.toDto().toDomain() shouldBe it } }
  "Year round-trips" { checkAll(Arb.year()) { it.toDto().toDomain() shouldBe it } }
  "BookId round-trips" { checkAll(Arb.bookId()) { it.toDto().toDomain() shouldBe it } }
  "SongId round-trips" { checkAll(Arb.songId()) { it.toDto().toDomain() shouldBe it } }
  "SongNumberId round-trips" { checkAll(Arb.songNumberId()) { it.toDto().toDomain() shouldBe it } }
  "TagId round-trips" { checkAll(Arb.tagId()) { it.toDto().toDomain() shouldBe it } }
  "Color round-trips" { checkAll(Arb.color()) { it.toDto().toDomain() shouldBe it } }
  "Tonality round-trips" { checkAll(Arb.tonality()) { it.toDto().toDomain() shouldBe it } }
  "Person round-trips" { checkAll(Arb.person()) { it.toDto().toDomain() shouldBe it } }
})
