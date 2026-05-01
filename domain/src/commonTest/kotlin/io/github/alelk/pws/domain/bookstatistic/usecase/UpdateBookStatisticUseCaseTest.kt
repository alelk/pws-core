package io.github.alelk.pws.domain.bookstatistic.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.bookstatistic.command.UpdateBookStatisticCommand
import io.github.alelk.pws.domain.bookstatistic.model.BookStatisticDetail
import io.github.alelk.pws.domain.bookstatistic.query.BookStatisticQuery
import io.github.alelk.pws.domain.bookstatistic.repository.BookStatisticRepository
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateBookStatisticUseCaseTest : FunSpec({

  val tx = NoopTransactionRunner()
  val bookId = BookId.parse("book-1")

  test("returns ValidationError when command is empty") {
    val repo = FakeBookStatisticRepository()
    val useCase = UpdateBookStatisticUseCase(repo, tx)

    val result = useCase(UpdateBookStatisticCommand(id = bookId))

    result shouldBe Either.Left(UpdateError.ValidationError("No changes specified for book statistic update"))
    repo.updateCalls shouldBe 0
  }

  test("delegates update and returns Right") {
    val expected = BookStatisticDetail(id = bookId, priority = 3, readings = 10)
    val repo = FakeBookStatisticRepository(updateResult = Either.Right(expected))
    val useCase = UpdateBookStatisticUseCase(repo, tx)

    val result = useCase(UpdateBookStatisticCommand(id = bookId, priority = 3))

    result shouldBe Either.Right(expected)
    repo.updateCalls shouldBe 1
  }

  test("propagates repository error") {
    val repo = FakeBookStatisticRepository(updateResult = Either.Left(UpdateError.NotFound))
    val useCase = UpdateBookStatisticUseCase(repo, tx)

    val result = useCase(UpdateBookStatisticCommand(id = bookId, priority = 7))

    result shouldBe Either.Left(UpdateError.NotFound)
  }
})

private class FakeBookStatisticRepository(
  private val updateResult: Either<UpdateError, BookStatisticDetail> = Either.Right(
    BookStatisticDetail(id = BookId.parse("book-1"), priority = 1)
  )
) : BookStatisticRepository {

  var updateCalls = 0

  override fun observe(id: BookId): Flow<BookStatisticDetail?> = flowOf(null)

  override fun observeMany(query: BookStatisticQuery): Flow<List<BookStatisticDetail>> = flowOf(emptyList())

  override suspend fun get(id: BookId): BookStatisticDetail? = null

  override suspend fun update(command: UpdateBookStatisticCommand): Either<UpdateError, BookStatisticDetail> {
    updateCalls += 1
    return updateResult
  }
}

