# skin overrides

a simple mod for locally changing skins and capes.

## how to use it

by default, this mod will do nothing. skins can be overridden by placing images in `.minecraft/skin_overrides`. file names can be the username or uuid of the player whose skin will be changed. file names can also include `.wide` or `.slim` for wide/slim skins respectively. for example:

-   `Steve.png`
-   `oriifu.slim.png`
-   `f369e23f-ed67-4f6a-8e79-4a2ca148691a.slim.png`
-   `c06f89064c8a49119c29ea1dbd1aab82.wide.png`

additionally, creating a txt file (eg `Steve.txt`) and putting a username or uuid in it will copy that player's skin.

capes/elytra can also be overridden by placing cape textures in `.minecraft/cape_overrides`. like skins, player names or uuids can be used as the file name.

## notes

this mod supports 1.20.2+. for 1.16 - 1.20.1, see [selfskin](https://modrinth.com/mod/selfskin)!

this mod needs [quilt loader](https://quiltmc.org/). if you're using fabric, you can switch over and keep all your mods!

on \*nix systems, file names are case sensitive. player names should match how they are shown in-game and uuids should be all lowercase.
