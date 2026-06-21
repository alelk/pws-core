package io.github.alelk.pws.api.mapping.songreference

import io.github.alelk.pws.domain.core.SongRefReason
import io.github.alelk.pws.domain.core.ids.songId
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class SongReferenceMappingTest : StringSpec({

  val arbIdPair = Arb.bind(Arb.songId(), Arb.songId()) { a, b -> a to b }.filter { it.first != it.second }

  val arbReference = Arb.bind(
    arbIdPair,
    Arb.enum<SongRefReason>(),
    Arb.int(1..100),
    Arb.int(0..50),
  ) { (songId, refSongId), reason, volume, priority ->
    SongReference(songId = songId, refSongId = refSongId, reason = reason, volume = volume, priority = priority)
  }

  "SongReference round-trips through its DTO" {
    checkAll(arbReference) { it.toDto().toDomain() shouldBe it }
  }

  "every SongRefReason maps both ways" {
    checkAll(Arb.enum<SongRefReason>()) { it.toDto().toDomain() shouldBe it }
  }
})
