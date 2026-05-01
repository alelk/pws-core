package io.github.alelk.pws.domain.tag.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.query.TagSort
import io.github.alelk.pws.domain.tag.repository.TagReadRepository
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TagWriteUseCasesTest : FunSpec({

  val tx = NoopTransactionRunner()

  test("CreateTagUseCase returns AlreadyExists when id already exists") {
    val read = FakeTagReadRepository(existsById = true)
    val write = FakeTagWriteRepository()
    val useCase = CreateTagUseCase(read, write, tx)

    val result = useCase(createCommand())

    result shouldBe Either.Left(CreateError.AlreadyExists())
    write.createCalls shouldBe 0
  }

  test("CreateTagUseCase returns ValidationError when name already exists") {
    val read = FakeTagReadRepository(existsByName = true)
    val write = FakeTagWriteRepository()
    val useCase = CreateTagUseCase(read, write, tx)

    val result = useCase(createCommand(name = "Taken"))

    result shouldBe Either.Left(CreateError.ValidationError("Tag name 'Taken' already exists"))
    write.createCalls shouldBe 0
  }

  test("CreateTagUseCase delegates to write repository when checks pass") {
    val read = FakeTagReadRepository()
    val write = FakeTagWriteRepository(createResult = Either.Right(tagId()))
    val useCase = CreateTagUseCase(read, write, tx)

    val command = createCommand()
    val result = useCase(command)

    result shouldBe Either.Right(command.id)
    write.lastCreateCommand shouldBe command
  }

  test("UpdateTagUseCase returns Right(id) when command has no changes") {
    val read = FakeTagReadRepository(existsById = false)
    val write = FakeTagWriteRepository()
    val useCase = UpdateTagUseCase(read, write, tx)

    val command = UpdateTagCommand(id = tagId())
    val result = useCase(command)

    result shouldBe Either.Right(command.id)
    write.updateCalls shouldBe 0
  }

  test("UpdateTagUseCase returns NotFound when tag does not exist") {
    val read = FakeTagReadRepository(existsById = false)
    val write = FakeTagWriteRepository()
    val useCase = UpdateTagUseCase(read, write, tx)

    val result = useCase(UpdateTagCommand(id = tagId(), name = "New"))

    result shouldBe Either.Left(UpdateError.NotFound)
    write.updateCalls shouldBe 0
  }

  test("UpdateTagUseCase returns ValidationError on duplicate name") {
    val read = FakeTagReadRepository(existsById = true, existsByName = true)
    val write = FakeTagWriteRepository()
    val useCase = UpdateTagUseCase(read, write, tx)

    val result = useCase(UpdateTagCommand(id = tagId(), name = "Taken"))

    result shouldBe Either.Left(UpdateError.ValidationError("Tag name 'Taken' already exists"))
    write.updateCalls shouldBe 0
  }

  test("UpdateTagUseCase delegates to write repository for valid update") {
    val read = FakeTagReadRepository(existsById = true)
    val write = FakeTagWriteRepository(updateResult = Either.Right(tagId()))
    val useCase = UpdateTagUseCase(read, write, tx)

    val command = UpdateTagCommand(id = tagId(), name = "Updated")
    val result = useCase(command)

    result shouldBe Either.Right(command.id)
    write.lastUpdateCommand shouldBe command
  }

  test("DeleteTagUseCase returns NotFound when tag is absent") {
    val read = FakeTagReadRepository(existsById = false)
    val write = FakeTagWriteRepository()
    val useCase = DeleteTagUseCase(read, write, tx)

    val result = useCase(tagId())

    result shouldBe Either.Left(DeleteError.NotFound)
    write.deleteCalls shouldBe 0
  }

  test("DeleteTagUseCase delegates to write repository when tag exists") {
    val read = FakeTagReadRepository(existsById = true)
    val write = FakeTagWriteRepository(deleteResult = Either.Right(tagId()))
    val useCase = DeleteTagUseCase(read, write, tx)

    val id = tagId()
    val result = useCase(id)

    result shouldBe Either.Right(id)
    write.lastDeletedId shouldBe id
  }
})

private fun tagId() = TagId.Predefined("worship")

private fun createCommand(name: String = "Worship") =
  CreateTagCommand(
    id = tagId(),
    name = name,
    color = Color.parse("#112233"),
    priority = 1
  )

private class FakeTagReadRepository(
  private val existsById: Boolean = false,
  private val existsByName: Boolean = false
) : TagReadRepository<TagId.Predefined> {

  override suspend fun get(id: TagId.Predefined): TagDetail<TagId.Predefined>? = null

  override suspend fun getAll(sort: TagSort): List<Tag<TagId.Predefined>> = emptyList()

  override suspend fun exists(id: TagId.Predefined): Boolean = existsById

  override suspend fun existsByName(name: String, excludeId: TagId.Predefined?): Boolean = existsByName
}

private class FakeTagWriteRepository(
  private val createResult: Either<CreateError, TagId.Predefined> = Either.Right(tagId()),
  private val updateResult: Either<UpdateError, TagId.Predefined> = Either.Right(tagId()),
  private val deleteResult: Either<DeleteError, TagId.Predefined> = Either.Right(tagId())
) : TagWriteRepository<TagId.Predefined> {

  var createCalls = 0
  var updateCalls = 0
  var deleteCalls = 0

  var lastCreateCommand: CreateTagCommand<TagId.Predefined>? = null
  var lastUpdateCommand: UpdateTagCommand<TagId.Predefined>? = null
  var lastDeletedId: TagId.Predefined? = null

  override suspend fun create(command: CreateTagCommand<TagId.Predefined>): Either<CreateError, TagId.Predefined> {
    createCalls += 1
    lastCreateCommand = command
    return createResult
  }

  override suspend fun update(command: UpdateTagCommand<TagId.Predefined>): Either<UpdateError, TagId.Predefined> {
    updateCalls += 1
    lastUpdateCommand = command
    return updateResult
  }

  override suspend fun delete(id: TagId.Predefined): Either<DeleteError, TagId.Predefined> {
    deleteCalls += 1
    lastDeletedId = id
    return deleteResult
  }
}

