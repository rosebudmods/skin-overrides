# development docs

this project uses [stonecutter](https://stonecutter.kikugie.dev/) so the gradle tasks are kind of a mess. here are the ones you probably want:

- `client`: run client for the active version
- `server`: run server for the active version
- `buildCurrent`: build the active version
- `build`: build all versions
- `publishMods`: publish for all versions
- `runClient`: runs all clients in increasing version
- `runServer`: runs all servers in increasing version
- `Reset active project`: reset active version
- `Set active project to (version)`: change active version

before doing a commit you want to reset version. probably also run `build` to make sure everything builds.
