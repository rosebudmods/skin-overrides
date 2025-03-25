- version support

  - 1.21.5 (spring to life) is now supported!

  - support durations have been simplified for 1.20.2 and below ([more info](https://rosebud.dev/skin-overrides/compatibility/))

    all versions are now part of the "support stack". for each new minecraft update, the oldest update is dropped. 12 mc versions will be supported at once (more may be supported later). if 4 drops are released per year, then each version will receive 3 years of support.

  - all versions without ui/networking are deprecated (1.15.2, 1.16.5, 1.17.1, 1.18.2, 1.19.2, 1.19.3). support for them will be removed in 2.4.0.

- the game no longer freezes when changing your skin
- the game no longer freezes when selecting an unsigned library entry

- internal reworks

  - the mineskin API client was rewritten ([gh#11](https://github.com/rosebudmods/skin-overrides/issues/11)). this should fix support for older java versions (and also shrink the mod filesize a bit)
  - library entries are no longer reloaded if not changed
  - other boring stuff

- bug fixes
  - fixed overrides not being shared when joining a server
  - fixed changing your skin globally applying to all players instead of just you ([gh#16](https://github.com/rosebudmods/skin-overrides/issues/16))
  - fixed spectator mode being very buggy ([gh#18](https://github.com/rosebudmods/skin-overrides/issues/18))
  - fixed overrides not working for player heads in 1.20.1 and older ([gh#17](https://github.com/rosebudmods/skin-overrides/issues/17))
  - fixed a freeze that could happen when deleting a library override
  - fixed being able to add no cape to the library ([gh#14](https://github.com/rosebudmods/skin-overrides/issues/14))
  - fixed a crash that does not seem to happen ever? ([gh#13](https://github.com/rosebudmods/skin-overrides/issues/13))
