# Spyglass Scroll

望远镜滚轮变焦模组，适用于 Minecraft 26.3.x 和 Fabric。

第一人称使用望远镜时，向上滚动放大，向下滚动缩小。每格调整 1.25 倍，视角倍率范围约为 2 倍至 50 倍，每次开镜从原版 10 倍开始。主手和副手均可使用。开镜时屏幕右下角显示滚轮缩放提示。

视角灵敏度与倍率成反比，10 倍时与原版相同，平滑镜头下只在超过 10 倍后降低。

缩放沿用原版视野过渡及滚轮灵敏度设置。开镜时滚轮不切换快捷栏，关镜后恢复原版行为。无需安装服务端模组。

## 安装

需要 Minecraft 26.3.x、Fabric Loader 0.19.5 或更新版本、对应版本 Fabric API，以及 Java 25 或更新版本。

将 `spyglass-scroll-*.jar` 放入客户端 `mods` 目录并重启游戏。不要安装 `-sources.jar`。

## 构建

使用 JDK 25：

```powershell
.\gradlew.bat build
.\gradlew.bat runClientGameTest
```

Linux 或 macOS 使用 `./gradlew`。产物位于 `build/libs/`，游戏测试需要图形界面。

## 许可

[MIT](LICENSE)，作者 PluginsKers。
