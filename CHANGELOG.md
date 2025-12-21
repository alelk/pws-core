# PWS Android

# [1.18.0](https://github.com/alelk/pws-core/compare/v1.17.1...v1.18.0) (2025-12-21)


### Features

* **usecase:** Refactor ReplaceSongReferencesUseCase to streamline create and update logic ([20bf93e](https://github.com/alelk/pws-core/commit/20bf93e7e840f303c2bf2cf4522889307f6aba06))

## [1.17.1](https://github.com/alelk/pws-core/compare/v1.17.0...v1.17.1) (2025-12-21)


### Bug Fixes

* **version:** Bump version to 1.17.1 ([f0bb109](https://github.com/alelk/pws-core/commit/f0bb109e2bb48a32ae1b3206bd13a9b165ab265c))

# [1.17.0](https://github.com/alelk/pws-core/compare/v1.16.0...v1.17.0) (2025-12-21)


### Features

* **api:** Add AdminSongReferenceApi and integrate with ApiClientContainer and DI module ([310f25b](https://github.com/alelk/pws-core/commit/310f25bf08bfe42831b5ea9f0813992c5e82cbe2))

# [1.16.0](https://github.com/alelk/pws-core/compare/v1.15.0...v1.16.0) (2025-12-21)


### Features

* **api:** Add AdminSongReferences and song reference DTOs with mappings ([1f4f103](https://github.com/alelk/pws-core/commit/1f4f1031317ad26a35444253a469d12c8973273d))

# [1.15.0](https://github.com/alelk/pws-core/compare/v1.14.0...v1.15.0) (2025-12-21)


### Features

* **domain:** Add song reference models, repositories, and use cases ([de51a34](https://github.com/alelk/pws-core/commit/de51a3469d34eb2204c0f5acef0a497e2b818b58))

# [1.14.0](https://github.com/alelk/pws-core/compare/v1.13.0...v1.14.0) (2025-12-21)


### Features

* **api:** Add AdminTagApi and its implementation to ApiClientContainer ([79e0515](https://github.com/alelk/pws-core/commit/79e051534e553a24c04dde1ff244eca40012c99e))

# [1.13.0](https://github.com/alelk/pws-core/compare/v1.12.2...v1.13.0) (2025-12-21)


### Bug Fixes

* Update dependencies ([49331a1](https://github.com/alelk/pws-core/commit/49331a10c61b48f65e34409aee9520bd9bc96260))


### Features

* **api:** Add GetSongTagsUseCase and song tags resource under AdminSongs API ([1f89116](https://github.com/alelk/pws-core/commit/1f89116acf887678babdf61bd6c87c03c478f18d))
* **api:** Add Tag API clients and tests for global, user, and admin tag operations ([ef15ea9](https://github.com/alelk/pws-core/commit/ef15ea95aa6f58780f35a5f46d7aafe4ae0e0ae3))
* **api:** Introduce SongTagAssociation model and update repository methods for song-tag management ([105fc6f](https://github.com/alelk/pws-core/commit/105fc6f08e8c7b51475ca61ecda1ca186b25eb31))
* **domain:** Refactor song-tag management and add comprehensive test coverage ([939d6dc](https://github.com/alelk/pws-core/commit/939d6dc2976f91a0874a2fef3fc7e2ebf30e1ab9))
* **tags:** Add tagging models, mappings, and use cases ([6fe4d72](https://github.com/alelk/pws-core/commit/6fe4d72b77df9093ae614a71d9a9f2294015836b))

## [1.12.2](https://github.com/alelk/pws-core/compare/v1.12.1...v1.12.2) (2025-12-17)


### Bug Fixes

* **api:** Enhance token management in HTTP client for dynamic loading and refreshing ([a355fbb](https://github.com/alelk/pws-core/commit/a355fbb0fdfa3e2ded3e2a17aee142ecec6fa914))

## [1.12.1](https://github.com/alelk/pws-core/compare/v1.12.0...v1.12.1) (2025-12-14)


### Bug Fixes

* Fix book api test ([6c24eb7](https://github.com/alelk/pws-core/commit/6c24eb79f8fcbc40dc677f491d83f5e632b3c5ba))
* Fix tests ([249d8d6](https://github.com/alelk/pws-core/commit/249d8d64040aea80e5051c1abf839c03c696f34b))

# [1.12.0](https://github.com/alelk/pws-core/compare/v1.11.0...v1.12.0) (2025-12-13)


### Features

* **api:** Implement User APIs for managing user history and favorites ([67f7283](https://github.com/alelk/pws-core/commit/67f7283baad99b66179ab973290ac03a59153799))

# [1.11.0](https://github.com/alelk/pws-core/compare/v1.10.0...v1.11.0) (2025-12-13)


### Features

* **api:** Add resourcesAlreadyExists error factory method for api. ([ce74730](https://github.com/alelk/pws-core/commit/ce74730e6a4202e9e5359c93d323dcfb698364c1))

# [1.10.0](https://github.com/alelk/pws-core/compare/v1.9.0...v1.10.0) (2025-12-13)


### Features

* **api:** Add new api endpoints: user history, favorites, tags, edied songs, books etc. ([df944ef](https://github.com/alelk/pws-core/commit/df944ef90fc5c03a15e997793c0a1729cc2d7264))

# [1.9.0](https://github.com/alelk/pws-core/compare/v1.8.2...v1.9.0) (2025-12-13)


### Bug Fixes

* Fix build issues ([6300b5a](https://github.com/alelk/pws-core/commit/6300b5a3adf55be9b9d12c9b9b980f3d8735c811))


### Features

* **domain:** Add user book/song override commands/repositiries ([fe97cba](https://github.com/alelk/pws-core/commit/fe97cba2965511614d90780cc5a9f60ad0e93b31))
* **history,favorites:** Update history/favirites domain model/repositories ([bcfcb83](https://github.com/alelk/pws-core/commit/bcfcb83a3454ba660c708e0c3868e250f770f202))
* **tags:** Add interfaces to control user tags ([a121525](https://github.com/alelk/pws-core/commit/a12152537556a7d49241efafdd02ea1526a988c2))

## [1.8.2](https://github.com/alelk/pws-core/compare/v1.8.1...v1.8.2) (2025-12-10)


### Bug Fixes

* Configure :features module publication ([a6fe733](https://github.com/alelk/pws-core/commit/a6fe733e30a43ec682260bf44ee04d1cac2e1da3))

## [1.8.1](https://github.com/alelk/pws-core/compare/v1.8.0...v1.8.1) (2025-12-10)


### Bug Fixes

* Configure :core:navigation module publication ([7d31e34](https://github.com/alelk/pws-core/commit/7d31e34ab13b03546eeb44f71fde8614217a9635))

# [1.8.0](https://github.com/alelk/pws-core/compare/v1.7.0...v1.8.0) (2025-12-09)


### Bug Fixes

* Fix build issue ([d427414](https://github.com/alelk/pws-core/commit/d427414905214d03f1582302adf33e6f066b58ce))
* fix features module publishing ([4c04862](https://github.com/alelk/pws-core/commit/4c048622f85b22082f45003a8b50be959a7bbfb3))


### Features

* **ui:** Implement more use cases in ui ([5f4f92a](https://github.com/alelk/pws-core/commit/5f4f92ae70b7b51a055bcdfc7f63528ab699a747))

# [1.7.0](https://github.com/alelk/pws-core/compare/v1.6.0...v1.7.0) (2025-12-09)


### Features

* **domain:** Add models, repositories, use cases ([9f6d443](https://github.com/alelk/pws-core/commit/9f6d443a80898d27debed92c74543d9b31cfbf45))
* **features:** Add ui components ([39906b6](https://github.com/alelk/pws-core/commit/39906b6e3f035f52bbd7b574ee1f87849e9b9469))

# [1.7.0-rc.1](https://github.com/alelk/pws-core/compare/v1.6.0...v1.7.0-rc.1) (2025-12-09)


### Features

* **domain:** Add models, repositories, use cases ([9f6d443](https://github.com/alelk/pws-core/commit/9f6d443a80898d27debed92c74543d9b31cfbf45))
* **features:** Add ui components ([39906b6](https://github.com/alelk/pws-core/commit/39906b6e3f035f52bbd7b574ee1f87849e9b9469))

# [1.6.0](https://github.com/alelk/pws-core/compare/v1.5.0...v1.6.0) (2025-12-08)


### Bug Fixes

* **api:** Fix api client di ([08cad13](https://github.com/alelk/pws-core/commit/08cad135054c5a3ff69d78c52d428942e0459a19))


### Features

* **auth:** Enhance AccessPlan and AuthProvider enums with identifiers and companion objects ([7b55258](https://github.com/alelk/pws-core/commit/7b55258f762b918174833c341b016678c07764e1))

# [1.5.0](https://github.com/alelk/pws-core/compare/v1.4.0...v1.5.0) (2025-12-08)


### Features

* **auth:** Support refresh auth tokens ([247d7b2](https://github.com/alelk/pws-core/commit/247d7b29a19ae839501f6fc1d1e20de93e810f2e))

# [1.4.0](https://github.com/alelk/pws-core/compare/v1.3.1...v1.4.0) (2025-12-05)


### Features

* **auth:** Implement client authentication ([576a526](https://github.com/alelk/pws-core/commit/576a52645be41dbc5ed97065f368dd1ee5d518cc))

## [1.3.1](https://github.com/alelk/pws-core/compare/v1.3.0...v1.3.1) (2025-12-05)


### Bug Fixes

* fix invalid package name issue ([4c6f803](https://github.com/alelk/pws-core/commit/4c6f8033e12ff17aa19e75e9e09fe7c0e08b844a))

# [1.3.0](https://github.com/alelk/pws-core/compare/v1.2.3...v1.3.0) (2025-12-05)


### Bug Fixes

* temporary skip android unit tests running ([70009f0](https://github.com/alelk/pws-core/commit/70009f047c083b8cfc4db5bf7ca405c5c6b60afd))


### Features

* **auth:** Implement pws authentication ([722a8f5](https://github.com/alelk/pws-core/commit/722a8f57e0f17e84af10b5910e3e231f036fd603))

## [1.2.3](https://github.com/alelk/pws-core/compare/v1.2.2...v1.2.3) (2025-12-03)


### Bug Fixes

* Update android gradle plugin version ([a15698d](https://github.com/alelk/pws-core/commit/a15698ddeea07c129e2de35c66606a03f8216fe3))

## [1.2.2](https://github.com/alelk/pws-core/compare/v1.2.1...v1.2.2) (2025-12-03)


### Bug Fixes

* fix domain test fixtures artifact package ([58548ad](https://github.com/alelk/pws-core/commit/58548adb9bf5c16d6a0e4b527bf56023459e01f7))

## [1.2.1](https://github.com/alelk/pws-core/compare/v1.2.0...v1.2.1) (2025-12-03)


### Bug Fixes

* fix db-repo android publishing ([f0e8032](https://github.com/alelk/pws-core/commit/f0e803279c201d91bfd221b2e855dd16d016434a))

# [1.2.0](https://github.com/alelk/pws-core/compare/v1.1.0...v1.2.0) (2025-12-03)


### Features

* configure db-repo android publishing ([d0e34b7](https://github.com/alelk/pws-core/commit/d0e34b72385df655fbef96c2274dd132dd38e43d))

# [1.1.0](https://github.com/alelk/pws-core/compare/v1.0.2...v1.1.0) (2025-12-03)


### Bug Fixes

* **api:** Fix person -> dto converter. ([97a3359](https://github.com/alelk/pws-core/commit/97a33592eafb868e2bfebbfe9c01047ccd7c4266))
* **api:** fix publication issues ([cb90699](https://github.com/alelk/pws-core/commit/cb9069983581b5ae025457793ef23adf314e505f))
* **api:** fix remote repositories issue ([cc5f991](https://github.com/alelk/pws-core/commit/cc5f991a05575df9b8faf5834f38b7724a2bd46d))
* **api:** Fix web songs observe repository. ([4eca529](https://github.com/alelk/pws-core/commit/4eca5297fa60989c39525d1fff36490a5f066337))
* **api:** Implement book/song/songnumber update endpoints ([b46af97](https://github.com/alelk/pws-core/commit/b46af975c8a81dc7b025e763aa4dc2fe845f7374))
* **api:** Implement lazy song list display. ([7ee5ba2](https://github.com/alelk/pws-core/commit/7ee5ba2ac434a90422841d450107039d7641a421))
* **api:** projects.api.contract is client's api ([2f6a6fa](https://github.com/alelk/pws-core/commit/2f6a6fa74f2f800eb396e1961f791cdfd7bef021))
* configure domain module publication ([ffee400](https://github.com/alelk/pws-core/commit/ffee40020c86e916a5d309c724560d32d5079d9f))
* fix build issues ([297c3f5](https://github.com/alelk/pws-core/commit/297c3f5c131da6e17de341c8fb5f260e77da2222))
* **server:** Fix build issues ([33fa1cc](https://github.com/alelk/pws-core/commit/33fa1cc1c3145a9e415e656b971db802fa1976ae))
* **server:** fix server app issues ([16f1dc6](https://github.com/alelk/pws-core/commit/16f1dc699cc349f1e7ad8f1d1c7faa781b3725fc))


### Features

* add modules api, features, core ([a9358b9](https://github.com/alelk/pws-core/commit/a9358b9cf3ed3d835c0a035ffc0d23b8a1cfdf2a))
* Add test ui screens ([0700ad9](https://github.com/alelk/pws-core/commit/0700ad93ff0b2752722335146312a30771039f86))
* **api-client:** add api client factory ([5b984ad](https://github.com/alelk/pws-core/commit/5b984ad95e0b801135d26ddb87ca0deebfb12820))
* **api-client:** Add method to create books ([2807fed](https://github.com/alelk/pws-core/commit/2807feda637fbe15d3be371f5a293213454285f2))
* **api-client:** implement book, song repositories in api client ([5309caa](https://github.com/alelk/pws-core/commit/5309caa4ad0add7d3ca33a8e618df9700a5da6f8))
* **api, server:** Implement songs search api ([d2cd5cf](https://github.com/alelk/pws-core/commit/d2cd5cfcea185a2aae55d3bcda3dbe2d7b0edcac))
* **api:** add api client di module ([f46d907](https://github.com/alelk/pws-core/commit/f46d907b29338b2641e4c607a336825e5977b4e4))
* **api:** Add song numbers create api endpoint ([06e2a78](https://github.com/alelk/pws-core/commit/06e2a783ac2fbbc07493984af77769cea2a67973))
* **api:** Fix songs api ([4d8a4ab](https://github.com/alelk/pws-core/commit/4d8a4abeb57ba1ec13b5e590e3bd256b28c6b969))
* **api:** improve books api: book create, book update endpoints ([73ba5a2](https://github.com/alelk/pws-core/commit/73ba5a2e66866c5ad5bc780d9db8864d7c7003b9))
* **app:** Add song / book observe repositories with caches ([a08f5bd](https://github.com/alelk/pws-core/commit/a08f5bd9a22b2382b7c8154b9f7ba2089524dfd1))
* **server-transport:** Add book routes tests. ([ca20de3](https://github.com/alelk/pws-core/commit/ca20de3168bcc50eda8f7399d777f204ac3980dc))
* **server:** Add create book endpoint ([97af29c](https://github.com/alelk/pws-core/commit/97af29c34fcc30f2c1f55d9461e4f8bdc67b380c))
* **server:** implement simple server modules ([c04ea4b](https://github.com/alelk/pws-core/commit/c04ea4b09406a644d0154ea64f21e09a3b0a4118))
* **server:** Implement song creation api ([7c46ad1](https://github.com/alelk/pws-core/commit/7c46ad125966a44b762dad8fae3055a8a3a49e3a))

## [1.0.2](https://github.com/alelk/pws-core/compare/v1.0.1...v1.0.2) (2025-12-02)


### Bug Fixes

* Update packages group ([e8ecb12](https://github.com/alelk/pws-core/commit/e8ecb1297010631d05c1a2bad7953b34cab8f85f))

## [1.0.1](https://github.com/alelk/pws-core/compare/v1.0.0...v1.0.1) (2025-12-02)


### Bug Fixes

* Fix packages publishing ([e868ec9](https://github.com/alelk/pws-core/commit/e868ec9e4b84881e9addb6b5676360d48fc74f05))

# 1.0.0 (2025-12-02)


* feat!: new pre-release ([5aa5c92](https://github.com/alelk/pws-core/commit/5aa5c92521563d9175ad3ecea344cdb5b0f7d8f3))


### Bug Fixes

* add build script; prepare for new public release ([#49](https://github.com/alelk/pws-core/issues/49)) ([de84bef](https://github.com/alelk/pws-core/commit/de84bef47998b9c66c9a3b76f63b56c5226852e2))
* **app:** fix books fragment issue: display book short name ([1a3c1b1](https://github.com/alelk/pws-core/commit/1a3c1b1323b4465647ea573f2e5511b47f2424aa))
* **backup:** fix backups ([dcddb35](https://github.com/alelk/pws-core/commit/dcddb358254531a3999d2b87f72e2bee8f521ec9))
* **backup:** fix backups ([ffb5c77](https://github.com/alelk/pws-core/commit/ffb5c774c99a0b9ab027f8a3ee08e4f7c1c9385a))
* **build:** add target iosSimulatorArm64 ([d5ac2a0](https://github.com/alelk/pws-core/commit/d5ac2a0dbd31a1c1f2cc948c98b1e9e2f550b288))
* **build:** add target iosX64 ([bed4cc5](https://github.com/alelk/pws-core/commit/bed4cc5aa507d59c2dbaf611a4215c34ed32a696))
* **build:** enable cross platform ios compilation ([fc16e5b](https://github.com/alelk/pws-core/commit/fc16e5bb5370d65b4a1a88f0edd415c91a4dacd4))
* **build:** Fix intellij idea build. ([dd27c54](https://github.com/alelk/pws-core/commit/dd27c543179a78bf052edfd4abc24923f7e8e7b0))
* **build:** update library dependencies ([8e13de9](https://github.com/alelk/pws-core/commit/8e13de9d2ba4ac74987a3cd328c128f4edeb2e30))
* **build:** update library dependencies ([#76](https://github.com/alelk/pws-core/issues/76)) ([aab75f0](https://github.com/alelk/pws-core/commit/aab75f02bd7eabb316f9958be0c7f18a73cfeced))
* **ci:** fix publish script ([56f5707](https://github.com/alelk/pws-core/commit/56f5707b40e8910855a6336e373f65803e587293))
* configure domain package publication ([#50](https://github.com/alelk/pws-core/issues/50)) ([f0ba67a](https://github.com/alelk/pws-core/commit/f0ba67a141ad5b452c7af8ff2e83b868d4d4e015))
* **database:** add database-test-fixtures module ([1c76a04](https://github.com/alelk/pws-core/commit/1c76a04398472e18593733fe3af5f445ee0d2bc4))
* **database:** database refactoring: remove redundant dao methods ([f0342b9](https://github.com/alelk/pws-core/commit/f0342b9d570f4f9ad6015d42ca499b983683e618))
* **database:** fix database native build ([a9dc9fb](https://github.com/alelk/pws-core/commit/a9dc9fb79b9b3a0f9ccb1872dfab6d4e37b55a16))
* **database:** fix database native build ([1d9db87](https://github.com/alelk/pws-core/commit/1d9db87876f55298c06c7cb54239cad9fc44fb05))
* **database:** optimize build scripts ([132a11d](https://github.com/alelk/pws-core/commit/132a11d744c718bb4fa12b917ca98c09876f66f5))
* **database:** rebuild database 2.0.0 files ([a12b51e](https://github.com/alelk/pws-core/commit/a12b51ec316a921f40d6efdfde5806e4324f46d0))
* **domain:** fix CreateSongCommand ([5cc5de9](https://github.com/alelk/pws-core/commit/5cc5de93e96ef47a7b36502452d516a63c06633b))
* fix build issues ([e1a5cb1](https://github.com/alelk/pws-core/commit/e1a5cb166e2cc21af0ba1b833777fc0f137f9c1b))
* Fix build issues ([6913d5b](https://github.com/alelk/pws-core/commit/6913d5b5702a078961f315e6b4e0c17063b78ca0))
* fix github packages publication ([6c66135](https://github.com/alelk/pws-core/commit/6c66135f71cb815f0073fdb40b574ebed07b0052))
* **project:** update dependencies ([#78](https://github.com/alelk/pws-core/issues/78)) ([3fb174f](https://github.com/alelk/pws-core/commit/3fb174f42d6522f9e832bbaeff8eaf94ebce0e7e))
* **project:** update gradle version & kotlin version ([ee8ad9e](https://github.com/alelk/pws-core/commit/ee8ad9e096e4b3ae4efaf447abc1b727e3463486))
* **project:** update project dependencies ([e456a01](https://github.com/alelk/pws-core/commit/e456a01a54c5362f248c702b611cfa28b17abc98))
* **publish:** configure release publishing ([94e4206](https://github.com/alelk/pws-core/commit/94e420696887585f8084c4de745a5ee3d7b0f02f))
* **publishing:** fix maven publishing ([64e69ae](https://github.com/alelk/pws-core/commit/64e69ae43d72f5d5a4e6856d56539191d104edbd))
* **search:** fix case-insensitive search ([#55](https://github.com/alelk/pws-core/issues/55)) ([f5d82e0](https://github.com/alelk/pws-core/commit/f5d82e098829fc1dd2f71e0291407052cb002d79))
* **search:** fix song search issue: search by song name; search from song activity ([#73](https://github.com/alelk/pws-core/issues/73)) ([766e0a1](https://github.com/alelk/pws-core/commit/766e0a176516d09cea27addf312606f062570077))
* **test:** Add song number link arb ([6f4617d](https://github.com/alelk/pws-core/commit/6f4617d0d33bc7d4228c2cb7dc377a02b8ff0a28))
* update library versions ([#56](https://github.com/alelk/pws-core/issues/56)) ([5033a9d](https://github.com/alelk/pws-core/commit/5033a9d606841b0244ea2dc5bef8cfd5aadb02ce))


### Features

* **app:** add link to rustore download for 'ru' flavor ([e4d1c21](https://github.com/alelk/pws-core/commit/e4d1c212200c31bae8f8fae2291a4142b1622733))
* **backup:** backup feature refactoring ([#48](https://github.com/alelk/pws-core/issues/48)) ([7cdd16e](https://github.com/alelk/pws-core/commit/7cdd16e44d3abeb1377bfd13334c7deba0d0c79b))
* **database:** implement jvm database ([013d440](https://github.com/alelk/pws-core/commit/013d440f2ec5571c4313bf01e7843784a2bfabf9))
* **database:** pws db 1x data provider: fetch tags ([a4b84ac](https://github.com/alelk/pws-core/commit/a4b84ac364a40b8902b89d6f448a3f0622671849))
* **domain, database:** domain refactoring; database module refactoring ([#82](https://github.com/alelk/pws-core/issues/82)) ([46754e0](https://github.com/alelk/pws-core/commit/46754e0d3d9b8ea9a54c44904caa692297fd8edf))
* **domain:** add book and song use cases ([636edf9](https://github.com/alelk/pws-core/commit/636edf9378c1c9da479ff1d881693e725e30db0b))
* **domain:** add hasChanges() method into UpdateBookCommand model ([9c01c72](https://github.com/alelk/pws-core/commit/9c01c72e353b23c835772d4099d7e9b4b9fc4fff))
* **domain:** add interfaces: Song repositories ([ff5a853](https://github.com/alelk/pws-core/commit/ff5a85399eaa03abcff40d49cac4449d9379612a))
* **domain:** Add song number use cases ([2a499d0](https://github.com/alelk/pws-core/commit/2a499d037b6452dd0c97d37106dca73c199bf6b5))
* **domain:** add song use cases ([5e3e203](https://github.com/alelk/pws-core/commit/5e3e2036d0c484ed19f168c399a3b610dbbdbd02))
* **domain:** book repository interfaces refactoring ([2ec5f75](https://github.com/alelk/pws-core/commit/2ec5f753a5751e54bb9d146a71eca66e19534663))
* **domain:** Improve optional field api: add getOrElse helper method ([78bbfcd](https://github.com/alelk/pws-core/commit/78bbfcd88d1cc80f36c4851c772d0280172db956))
* **domain:** make some domain types comparable ([c16bd14](https://github.com/alelk/pws-core/commit/c16bd14d10b5093266be388c2dc8015085365612))
* **domain:** Reimplement CreateResourcesResult domain model. ([2217365](https://github.com/alelk/pws-core/commit/2217365585525f0b8f8ef83e665f89445eaa5613))
* **domain:** Reimplement song creation use case ([40f48b2](https://github.com/alelk/pws-core/commit/40f48b2fd552d942a337add14933e0ebb5c09ed0))
* **domain:** Update book repositories and use cases ([3eee018](https://github.com/alelk/pws-core/commit/3eee018c6310880d23b768f2a7b766096ce4ad27))
* **domain:** Update OptionalField api: add forEach method ([6c1f2bd](https://github.com/alelk/pws-core/commit/6c1f2bd92cf1c195cee0f3849b1ac475f95f6f9a))
* **domain:** Update Song use cases and repositories ([9352de6](https://github.com/alelk/pws-core/commit/9352de655223cd132919a1a88a0198414ac7e954))
* **publication:** configure multiplatform github packages publishing ([9803480](https://github.com/alelk/pws-core/commit/9803480e966128f9a97de1fe22bcad0b9b36e9da))
* **publication:** configure multiplatform github packages publishing ([#69](https://github.com/alelk/pws-core/issues/69)) ([860f433](https://github.com/alelk/pws-core/commit/860f433d06cfc70ecce4cb6f8f2efe563aeac822))
* **release:** update app version: v32 ([c622711](https://github.com/alelk/pws-core/commit/c622711689798581323a9f04b1c379ae1c583122))
* **repository:** implement book and bookstatistic repositories ([#83](https://github.com/alelk/pws-core/issues/83)) ([a84d189](https://github.com/alelk/pws-core/commit/a84d1895626134152575f39595d9b0a5d343ace4))
* **serialization:** implement domain model serialization ([#68](https://github.com/alelk/pws-core/issues/68)) ([c6782c1](https://github.com/alelk/pws-core/commit/c6782c1861db0411082ec2e28f4b6e47ccbd3ac1))


### BREAKING CHANGES

* database schema has changed
