# Spyglass Scroll

Scroll to zoom while using a spyglass. Works in either hand, with no server mod required.

Scroll up to zoom in and down to zoom out. Each step changes magnification by 1.25×, from 2× to 50×. Every use starts at the vanilla 10× zoom. Mouse sensitivity follows magnification; vanilla transitions and scroll sensitivity are preserved.

## Installation

Requires Minecraft 26.3.x, Fabric Loader 0.19.5 or newer, Fabric API, and Java 25.
Place the mod jar in your client's `mods` folder.

## Building

```sh
./gradlew build
./gradlew runClientGameTest
```

Use `gradlew.bat` on Windows. Builds require JDK 25; game tests also require a display. Jars are written to `build/libs`.

## License

[MIT](LICENSE).
