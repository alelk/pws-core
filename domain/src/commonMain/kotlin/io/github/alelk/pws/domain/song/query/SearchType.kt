package io.github.alelk.pws.domain.song.query

/**
 * Type of search to perform.
 */
enum class SearchType {
  /** Search in all fields */
  ALL,

  /** Search only in song name */
  NAME,

  /** Search only in song lyric */
  LYRIC,

  /** Search by song number */
  NUMBER
}