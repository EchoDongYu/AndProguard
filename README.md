[![Kotlin](https://img.shields.io/badge/%20language-Kotlin-blueviolet.svg)](https://kotlinlang.org/)
[![IntelliJ Idea Plugin](https://img.shields.io/badge/plugin-IntelliJ%20%20Idea-blue.svg)](https://plugins.jetbrains.com/)
[![Android Studio Plugin](https://img.shields.io/badge/plugin-AndroidStudio-green.svg)](https://plugins.jetbrains.com/)

***Read this in other languages: [English](README.md), [简体中文](README.zh.md).***

[Install from Plugin Marketplace](https://plugins.jetbrains.com/)

## AndGuard

This is a plugin to obfuscate resources

### Easy use

    Tips clean build, viewbinding cache...

### Custom naming

#### ① 标识 []：

| [ | 0 \| 1 | 0 \| 1 | 0 \| 1 | 0 \| 1 | ] |
|---|--------|--------|--------|--------|---|
| * | 大写字母   | 小写字母   | 数字     | 下划线    | * |

#### ② 长度 ()：

> - 区间表示 (start, end)，闭区间
> - 固定表示 (length)

#### ③ 示例组合：

`[1000](1)[0100](3,9)` （伪单词）表示大写字母开头，后接3至9位小写字母

#### ④ 复用组合 {}：

`{[1000](1)[0100](3,9)}(2,3)` 表示2至3个伪单词

#### ⑤ 固定字符串组合 <>：

`{[1000](1)[0100](3,9)}(1,2)<Activity>` 表示1至2个伪单词，后接 `Activity`

#### Tips invalid name