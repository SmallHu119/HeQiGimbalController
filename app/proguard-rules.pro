# ProGuard rules
-keep public class * {
    public protected *;
}

# IJKPlayer
-keep class tv.danmaku.ijk.media.player.** { *; }
-keep class tv.danmaku.ijk.media.player.IjkMediaPlayer { *; }
-keep class tv.danmaku.ijk.media.player.ffmpeg.FFmpegApi { *; }
