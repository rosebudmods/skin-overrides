# development docs

this project uses [stonecutter](https://stonecutter.kikugie.dev/) so the gradle tasks are kind of a mess. here are the ones you probably want:

- `client`: run client for the active version
- `server`: run server for the active version
- `buildCurrent`: build the active version
- `chiseledBuild`: build for ALL versions
- `chiseledPublish`: publish for all versions
- `Reset active project`: reset active version
- `Set active project to 1.21.4`: change active version

before doing a commit you want to reset version. probably also run `chiseledBuild` to make sure everything builds.

tasks you SHOULD NOT use are `runClient`, `runServer`, and `publishMods`.
