# Nimingban

![Icon](art/launcher_icon-web.png)

这是一个匿名版客户端，现在你可以用它上 [A 岛](http://h.nimingban.com/Forum)


# Screenshot

![screenshot-00](art/screenshot-00.png)

![screenshot-01](art/screenshot-01.png)


# Build

    $ git clone https://github.com/seven332/Nimingban
    $ cd Nimingban
    $ git submodule update --init
    $ gradlew daogenerator:executeDaoGenerator
    $ gradlew app:assembleDebug

生成的 apk 文件在 app\build\outputs\apk 目录下


# Thanks

本项目受到了诸多开源项目的帮助

- [AOSP](http://source.android.com/)
- [android-advancedrecyclerview](https://github.com/h6ah4i/android-advancedrecyclerview)
- [android-gif-drawable](https://github.com/koral--/android-gif-drawable)
- [fastjson](https://github.com/alibaba/fastjson)
- [greenDAO](https://github.com/greenrobot/greenDAO)
- [jsoup](https://github.com/jhy/jsoup)
- [leakcanary](https://github.com/square/leakcanary)
- [PhotoView](https://github.com/chrisbanes/PhotoView)
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
