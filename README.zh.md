[![Kotlin](https://img.shields.io/badge/%20language-Kotlin-blueviolet.svg)](https://kotlinlang.org/)
[![IntelliJ Idea Plugin](https://img.shields.io/badge/plugin-IntelliJ%20%20Idea-blue.svg)](https://plugins.jetbrains.com/)
[![Android Studio Plugin](https://img.shields.io/badge/plugin-AndroidStudio-green.svg)](https://plugins.jetbrains.com/)

***其他语言版本: [English](README.md), [简体中文](README.zh.md).***

[Install from Plugin Marketplace](https://plugins.jetbrains.com/)

## AndGuard 简介

- AndGuard 是一个帮助混淆任意类的插件，支持混淆 `Java/Kotlin/Xml` **源文件**，支持对文件夹执行批量混淆。
- AndGuard 执行时会查找所有能修改的元素执行重命名，包括`Java/Kotlin`中的：类名、文件名、方法/函数名、字段/属性名、参数名和局部变量名，
  `Xml`中的属性名和文件名。
- 它的原理是基于官方插件的重命名功能，在修改元素时会自动处理所有引用的修改；AndGuard 是在此基础功能扩展。

> #### AndGuard 能做什么？
>- 应用加固，增加 aab、apk 反编译的难度。
>- 降低 aab 包查重率，避免上架`Google Play`因查重率过高，导致下架或封号问题。

### 警告⚠️

- **AndGuard 是在本地操作，且任务执行是不可逆的，故务必做好代码备份或在版本分支工具管理下操作，否则代码将很难还原**
- **重命名会关联到`build`目录下的无效引用，故执行前务必清理`build`目录：执行 `Build-Clean Project`，否则会出现阻塞任务的窗口**

### 简单使用

只需选择需要混淆的文件或文件夹，右键菜单选择 `AndGuard` 或 `ViewBinding`：

- `AndGuard` 支持对 `Java/Kotlin/Xml` 文件或文件夹使用。
- `ViewBinding` 仅支持对 `Xml` 文件或文件夹使用，修改 `Xml` 时会自动处理 `ViewBinding` 引用。

### 设置

- **[Custom naming（自定义命名规则）](#自定义命名规则)**：支持自定以类名，方法名，变量名等等命名规则。
- Exclude package（白名单包目录）：填写包名，多个包名使用符号`;`衔接，对文件夹执行任务时会忽略填写的目录。
- Skip data（跳过数据类）：执行任务时会跳过 `Java` 中的 `getter`或`setter` 方法和对应的字段，
  以及 `Kotlin` 中的 `Data` 类的构造方法内的成员参数。

### 自定义命名规则

#### 1 标识 []：四位标识确定随机字符的范围

| [ | 0 \| 1 | 0 \| 1 | 0 \| 1 | 0 \| 1 | ] |
|---|--------|--------|--------|--------|---|
| * | 大写字母   | 小写字母   | 数字     | 下划线    | * |

#### 2 长度 ()：随机字符或组合的重复次数

闭区间表示 `(start, end)` 或固定长度表示 `(length)`

#### 3 简单示例：

`[1000](1)[0100](3,9)` （大驼峰伪单词）表示大写字母开头，后接3至9位小写字母

#### 4 复用组合 {}：

`{[1000](1)[0100](3,9)}(2,3)` 表示2至3个大驼峰伪单词

#### 5 固定字符串 <>：

`{[1000](1)[0100](3,9)}(1,2)<Activity>` 表示1至2个大驼峰伪单词，后接 `Activity`

### 注意事项

- AndGuard 不侵入打包流程，无法混淆编译后的 `Class` 文件，需要在打包前执行。
- 自定义命名规则务必保证符合命名规范，否则替换名字将会不执行或出现不可预知的错误。
- 复用规则`{}`不宜嵌套过深，会影响执行效率。

### 混淆示例