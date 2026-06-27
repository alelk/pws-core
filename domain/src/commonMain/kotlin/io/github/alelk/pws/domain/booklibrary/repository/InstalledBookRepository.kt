package io.github.alelk.pws.domain.booklibrary.repository

/** Convenience aggregate: one impl can satisfy all three repository roles. */
interface InstalledBookRepository :
    InstalledBookObserveRepository,
    InstalledBookReadRepository,
    InstalledBookWriteRepository
