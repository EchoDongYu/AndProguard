[![Kotlin](https://img.shields.io/badge/%20language-Kotlin-blueviolet.svg)](https://kotlinlang.org/)
[![IntelliJ Idea Plugin](https://img.shields.io/badge/plugin-IntelliJ%20%20Idea-blue.svg)](https://plugins.jetbrains.com/plugin/23840-andproguard)
[![Android Studio Plugin](https://img.shields.io/badge/plugin-AndroidStudio-green.svg)](https://plugins.jetbrains.com/plugin/23840-andproguard)  

[Install from Plugin Marketplace](https://plugins.jetbrains.com/plugin/23840-andproguard)

## AndProguard 简介

- AndProguard 是一个帮助混淆**源文件**的插件，支持多种语言`Java/Kotlin/Xml`，支持批量混淆。
- AndProguard 执行时会查找所有能修改的元素执行重命名，包括`Java/Kotlin`中的：类名、文件名、方法/函数名、字段/属性名、参数名和局部变量名，
  `Xml`中的属性名和文件名。
- 它的原理是基于官方插件的重命名功能，在修改元素时会自动处理所有引用的修改；AndProguard 是在此基础上功能扩展。
- 支持在设置中[自定义命名规则（Custom naming）](NamingRule.md)

> #### AndProguard 能做什么？
>- 应用加固，增加 aab、apk 反编译的难度。
>- 降低 aab 包查重率，避免上架`Google Play`因查重率过高，导致下架或封号问题。

### 警告⚠️

- **AndProguard 是在本地操作，且任务执行是不可逆的，故务必做好代码备份或在版本分支工具管理下操作，否则代码将很难还原**
- **AndProguard 不侵入打包流程，无法混淆编译后的 `Class` 文件，需要在打包前执行**
- **重命名会关联到 `build` 目录下的无效引用，故执行前请清理 `build` 目录：执行 `Build -> Clean Project`，
  否则可能会出现阻塞任务的窗口**

### 简单使用

只需选择想要混淆的文件或文件夹，右键菜单选择 `Proguard -> Obfuscate *`，支持对 `Java/Kotlin/Xml` 文件或文件夹使用：

![AndProguard](img/AndProguard.gif)

1. **Obfuscate Tree**：混淆当前元素，并递归子元素的混淆
2. **Obfuscate Node**：仅混淆当前元素
3. **JSON Mapping Interface**：根据接口文档生成的JSON文件，修改项目的接口数据类
4. **Refactor Packages**：混淆当前文件目录，并递归子目录的混淆

### 混淆示例

```
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Start [Obfuscate Tree] 2024-11-25 16:23:43 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
[KtVariable] oldName >>> rolpteijoyiVelesu
[KtVariable] name >>> cejijije
[KtVariable] orderId >>> sneyivaqebu
[KtVariable] entityDemo >>> gayortarthi
[KtVariable] aaa >>> kupeevek
[KtVariable] bbb >>> huqixazalch
[KtVariable] ccc >>> eautirinerko
[KtParameter] orderId >>> setrupiesha
[KtParameter] orderId >>> ipoqero
[KtParameter] value >>> ziefala
[KtParameter] open >>> taweyewephu
[PsiParameter] a >>> zistopaunYudupuyedojo
[PsiParameter] b >>> aravanelesFeeta
[PsiParameter] entityDemo >>> qedeukiGemptu
[PsiParameter] entityDemoKt >>> tupinorasnab
[PsiParameter] interfaceDemo >>> lerkiousilaBedov
[PsiParameter] name >>> misplarawufe
[PsiParameter] age >>> pekigrutaik
[PsiParameter] name >>> vimpixun
[PsiField] context >>> aruneeaIcopifl
[PsiField] age >>> pijijeea
[PsiField] entityDemoKt >>> qexedakaq
[PsiField] age >>> hexepai
[PsiField] classDemo >>> owofeesKusakumpti
[PsiVariable] aaa >>> wiwilyog
[PsiVariable] bbb >>> whobubortiew
[PsiVariable] ccc >>> singoleqeyeJacir
[PsiVariable] ddd >>> tailuxawoa
[PsiVariable] eee >>> ltini
[PsiVariable] fff >>> zeifane
[PsiVariable] classDemo >>> rthuwojeilch
[KtClass] InnerClassDemo >>> BubiroLinuckawicav
[KtClass] ClassDemoKt >>> DeikoruraifeSaduwSukiv
[KtClass] EntityDemoKt >>> VipudeWemucecubega
[KtClass] InterfaceDemoKt >>> GezinabroaxPirtupunihiHoyiwoy
[KtClass] AnnotationDemoKt >>> ZozumeroqoAtovevon
[KtFunction] entityDemo >>> clobriholDeneiwaciAskazo
[KtFunction] demo >>> eubayejSuquwaz
[KtFunction] functionDemo >>> sweutoojIwafire
[KtFunction] getOrderId >>> jertoivaiPounichejaGelyiwo
[KtFunction] setOrderId >>> diderobQoaraOkojomun
[KtFunction] getOrderId >>> xusneuBezudeqinu
[KtFunction] getB >>> bisnomNozascheyiWoqonacaq
[PsiMethod] entityDemo >>> ntecoVigegXijezufuqa
[PsiMethod] innerMethodDemo >>> loftupohNirepoxa
[PsiMethod] methodDemo >>> uhaseeasaXokovoxunJobawi
[PsiMethod] forNothing >>> qaftamOtihovomOnariredej
[PsiMethod] methodDemo >>> wufutefNeerthudedYurapocem
[PsiEnumConstant] DEFAULT >>> JoboithrauwXananSaxipev
[PsiEnumConstant] ANDROID >>> XefivoigejuUlohiscr
[PsiClass] EnumDemo >>> HoziwhoWomosedeajo
[PsiClass] ClassDemo >>> JaxulpteseltWarejou
[PsiClass] EntityDemo >>> UsadafonExemoyeimek
[PsiClass] InterfaceDemo >>> OsujieveiZafanoivuTuhicugiou
[PsiClass] ReferenceDemo >>> EroboreropasLoxespribeqo
[PsiClass] AnnotationDemo >>> MogajFealchucay
[PsiClass] InterfaceDemo2 >>> VocatWolahavoic
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> End [Obfuscate Tree] 2024-11-25 16:24:00 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
```