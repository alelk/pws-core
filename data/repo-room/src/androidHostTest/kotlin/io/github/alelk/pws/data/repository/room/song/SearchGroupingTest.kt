package io.github.alelk.pws.data.repository.room.song

import io.github.alelk.pws.database.song.SongSearchResultEntity
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class SearchGroupingTest : StringSpec({

  fun row(songId: Long, bookId: String, songName: String, number: Int, bookName: String, snippet: String) =
    SongSearchResultEntity(
      songId = SongId(songId),
      bookId = BookId.parse(bookId),
      songName = songName,
      songNumber = number,
      bookDisplayName = bookName,
      snippet = snippet,
    )

  "merges rows of the same song into one suggestion with all book references" {
    val rows = listOf(
      row(1, "Book-1", "Amazing Grace", 10, "B1", ""),
      row(1, "Book-2", "Amazing Grace", 25, "B2", "snippet"),
    )
    val result = groupSuggestions(rows)

    result.size shouldBe 1
    val s = result.single()
    s.id shouldBe SongId(1L)
    s.name.value shouldBe "Amazing Grace"
    s.bookReferences.map { it.bookId to it.songNumber } shouldContainExactlyInAnyOrder
      listOf(BookId.parse("Book-1") to 10, BookId.parse("Book-2") to 25)
  }

  "keeps the first non-blank snippet" {
    val rows = listOf(
      row(1, "Book-1", "Song", 1, "B1", ""),
      row(1, "Book-2", "Song", 2, "B2", "the match"),
    )
    groupSuggestions(rows).single().snippet shouldBe "the match"
  }

  "produces one suggestion per distinct song" {
    val rows = listOf(
      row(1, "Book-1", "First", 1, "B1", "a"),
      row(2, "Book-1", "Second", 2, "B1", "b"),
    )
    groupSuggestions(rows).map { it.id } shouldContainExactlyInAnyOrder listOf(SongId(1L), SongId(2L))
  }

  "empty input yields no suggestions" {
    groupSuggestions(emptyList()) shouldBe emptyList()
  }
})
