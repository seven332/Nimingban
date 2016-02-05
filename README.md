# Nimingban

![Icon](art/launcher_icon-web.png)

这是一个匿名版客户端，现在你可以用它上 [A 岛](http://h.nimingban.com/Forum)

An imageboard or textboard client. [AC Nimingban](http://h.nimingban.com/Forum) is supported now.


# Screenshot

![screenshot-00](art/screenshot-00.png)

![screenshot-01](art/screenshot-01.png)


# Build

    $ git clone https://github.com/seven332/Nimingban
    $ cd Nimingban
    $ git submodule update --init
    $ gradlew daogenerator:executeDaoGenerator
    $ gradlew app:copyNotice
    $ gradlew app:assembleDebug

生成的 apk 文件在 app\build\outputs\apk 目录下

The apk is in app\build\outputs\apk


# Thanks

本项目受到了诸多开源项目的帮助

Here is the libraries

- [AOSP](http://source.android.com/)
- [android-advancedrecyclerview](https://github.com/h6ah4i/android-advancedrecyclerview)
- [android-gesture-detectors](https://github.com/Almeros/android-gesture-detectors)
- [android-gif-drawable](https://github.com/koral--/android-gif-drawable)
- [apng](http://apng.sourceforge.net/)
- [fastjson](https://github.com/alibaba/fastjson)
- [giflib](http://giflib.sourceforge.net)
- [gifsicle](https://github.com/kohler/gifsicle)
- [greenDAO](https://github.com/greenrobot/greenDAO)
- [jsoup](https://github.com/jhy/jsoup)
- [leakcanary](https://github.com/square/leakcanary)
- [libjpeg-turbo](http://libjpeg-turbo.virtualgl.org/)
- [libpng](http://www.libpng.org/pub/png/libpng.html)
- [okhttp](https://github.com/square/okhttp)
- [PhotoView](https://github.com/chrisbanes/PhotoView)
- [recyclerview-animators](https://github.com/wasabeef/recyclerview-animators)
- [SwipeBackLayout](https://github.com/ikew0ng/SwipeBackLayout)


# License

    Copyright (C) 2015 Hippo Seven

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

ic_launcher 图标为 Hippo Seven 所有，所有权利保留

ic_launcher_ibuki 图标为 ibuki 所有，如需使用请注明出处与作者
