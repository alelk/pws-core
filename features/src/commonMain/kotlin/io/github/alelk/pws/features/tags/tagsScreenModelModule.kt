package io.github.alelk.pws.features.tags

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.usecase.ObserveSongsByTagUseCase
import io.github.alelk.pws.domain.tag.usecase.CreateTagUseCase
import io.github.alelk.pws.domain.tag.usecase.DeleteTagUseCase
import io.github.alelk.pws.domain.tag.usecase.GetTagDetailUseCase
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import io.github.alelk.pws.domain.tag.usecase.UpdateTagUseCase
import io.github.alelk.pws.features.tags.songs.TagSongsScreenModel
import org.koin.dsl.module

val tagsScreenModelModule = module {
  factory { TagsScreenModel(get<ObserveTagsUseCase<TagId>>(), get<CreateTagUseCase<TagId>>(), get<UpdateTagUseCase<TagId>>(), get<DeleteTagUseCase<TagId>>()) }
  factory { (tagId: TagId) -> TagSongsScreenModel(tagId, get<GetTagDetailUseCase<TagId>>(), get<ObserveSongsByTagUseCase<TagId>>()) }
}
