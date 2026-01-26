# PWS Features

## Song Search

### Full-text Search
- **Input**: songbook number OR arbitrary text
- **Behavior**: search by numbers, titles, song lyrics
- **Result**: `List<SongSearchResult>` with relevance, match highlighting, and book references (bookId, displayShortName, songNumber)
- **Use Cases**: `SearchSongsUseCase`, `SearchSongSuggestionsUseCase`

### Search Suggestions
- Displayed as user types
- Returns `SongSearchSuggestion` with suggested text and book references
- Book references include: bookId, displayShortName, songNumber for navigation

## Songbook Browsing

### Songbook List
- Displays all available songbooks (Book)
- Each songbook has: title, description, song count
- **Use Cases**: `GetBooksUseCase`

### Songbook Songs List
- Shows songs from selected songbook
- Sorted by song number in songbook
- **Use Cases**: `GetSongNumbersUseCase`

## Song View

### Song Screen
- Displays full song lyrics
- Shows metadata: author, key, Bible verse
- **Use Cases**: `GetSongDetailUseCase`, `ObserveSongUseCase`

### Related Content
- **Tags**: song categories (displayed as chips)
- **Similar songs**: links to related songs (Song references)
- Tapping a tag → navigates to song list with that tag
- Tapping a similar song → navigates to that song

### Automatic History Addition
- Condition: song opened for > 10 seconds
- Action: create History record
- **Use Cases**: `AddHistoryUseCase`

## Favorites

### Adding to Favorites
- "Add to favorites" button on song screen
- Toggle: add/remove
- **Use Cases**: `AddFavoriteUseCase`, `RemoveFavoriteUseCase`

### Favorites List
- Displays all user's favorite songs
- Sorted by date added (newest first)
- **Use Cases**: `GetFavoritesUseCase`

## History

### Automatic Tracking
- Songs are added after 10 seconds of viewing
- No duplication (timestamp is updated)

### History View
- List of viewed songs
- Sorted by date (most recent first)
- **Use Cases**: `GetHistoryUseCase`

## Tags / Categories

### Global Tags
- Defined on backend
- Read-only
- Examples: "Christmas", "Easter", "Worship"

### User Tags
- User can create custom tags
- Assign custom tags to songs
- CRUD operations: create, edit, delete
- **Use Cases**: `CreateTagUseCase`, `UpdateTagUseCase`, `DeleteTagUseCase`

### Tag Assignment
- Add tag to song
- Remove tag from song
- **Use Cases**: `AddSongTagUseCase`, `RemoveSongTagUseCase`

### Songs by Tag View
- Tapping a tag → list of all songs with that tag
- **Use Cases**: `GetSongsByTagUseCase`

## Song Editing (User Overrides)

### Concept
- Global songs are immutable
- User creates an **override**
- When displaying: user override is merged with global song

### What Can Be Overridden
- Song lyrics
- Key
- Metadata

### Merge Strategy
```
Displayed song = Global Song + User Override
                 (override takes priority)
```

## Planned Features (Future)

### User Songbooks
- Create custom songbooks
- Add songs to songbook
- Publish songbook (request to add to global)

### Publishing Changes
- User can propose corrections
- After moderation — applied to global song

---

## Feature Matrix by Platform

| Feature               | Android/iOS  | Web/TG Mini App |
|-----------------------|--------------|-----------------|
| Search                | ✅            | ✅               |
| Song viewing          | ✅            | ✅               |
| Favorites             | ✅ (local)    | ✅ (synced)      |
| History               | ✅ (local)    | ✅ (synced)      |
| Global tags           | ✅            | ✅               |
| User tags             | ✅ (local)    | ✅ (synced)      |
| User Overrides        | ✅ (local)    | ✅ (synced)      |
| Offline mode          | ✅            | ❌               |
