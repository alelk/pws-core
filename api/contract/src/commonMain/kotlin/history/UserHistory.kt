package io.github.alelk.pws.api.contract.history

import io.ktor.resources.Resource

/**
 * User history API endpoints.
 *
 * GET /v1/user/history - list history entries
 * POST /v1/user/history - record a song view (body: HistorySubjectDto)
 * DELETE /v1/user/history - clear all history
 * DELETE /v1/user/history/entry - remove specific entry (body: HistorySubjectDto)
 */
@Resource("/v1/user/history")
class UserHistory(
  val limit: Int = 50,
  val offset: Int = 0
) {
  /**
   * DELETE /v1/user/history/entry - remove specific history entry.
   * Body: HistorySubjectDto
   */
  @Resource("entry")
  class Entry(val parent: UserHistory = UserHistory())
}
