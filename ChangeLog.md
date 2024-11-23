## 3.6.1

### Change Log

1. 适配新版本 IDE's dumb mode，修复 IndexNotReadyException
2. 跳过某种类型的随机重命名，配置命名规则为空时即生效
3. 增加单独随机重命名入口 Obfuscate Node
4. 优化确认对话框的静默弹出策略
5. Obfuscate Tree 合并 ViewBinding 功能

## 3.6.2

### Change Log

1. 重构命名规则，自定义控制字母/数字/下划线随机出现的权重，以及随机出现重复的概率
2. 新增根据接口文档生成的JSON映射文件，修改项目的接口数据类
3. 新增可以重命名文件目录
4. 新增输出映射文件