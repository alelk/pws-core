
import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.TonalityDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.YearDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.LyricDto
import kotlinx.serialization.Serializable

@Serializable
data class SongUpdateRequestDto(
  val id: SongIdDto,
  val locale: LocaleDto? = null,
  val name: String? = null,
  val lyric: LyricDto? = null,
  val author: PersonDto? = null,
  val translator: PersonDto? = null,
  val composer: PersonDto? = null,
  val tonalities: List<TonalityDto>? = null,
  val year: YearDto? = null,
  val bibleRef: String? = null,
  val expectedVersion: VersionDto? = null
)
