## 0. Desc

### How to use

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

## TODO

### 1. mapping-log

### 2. Setting

    1. custon name rule(class, field/property/parameter/variable, method/function, layoutRes, idRes)
    2. exclude package
    3. skip data