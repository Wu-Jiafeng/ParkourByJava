# ParkourByJava 2.0

对原RGB测试版作处如下改动：

1.将原程序类2d物理逻辑通过jme.bullet重构为3d

2.将原程序扩充根据处理对象的不同重构为5个类：Main,InputAppState,SceneAppState,CubeAppState,CharacterAppState

Main类：
处理与GUI相关的内容及控制游戏整体逻辑

InputAppState类：
主要用于监听外部输入，现实现的外部输入有：移动WASD/跳跃SPACE/调出设置菜单ESC

SceneAppState类：
主要用于渲染外部场景，如海水/山脉/阳光/天空/环境音

CubeAppState类：
主要用于生成方块道路，不同类型的方块对应不同关卡，
现已实现类型有：正常方块/蓝方块（加速）/红方块（减速）/绿方块（治疗）/白方块（受伤）。
并可能在方块上方产生bonus(bonus为主要得分手段)

CharacterAppState类：
与游戏主人公相关的构建，包括但不限于主人公模型的初始化/第三人称摄像机

包含物理性质设置的类有：SceneAppState,CubeAppState,CharacterAppState

3.增添音效和人物模型：
音效设置分布于SceneAppState.initNatureSound/ CubeAppState.initSound/ CharacterAppState.initRunSound
人物模型设置于CharacterAppState.initCharacter

必须的改进：
1.路面生成规则尽可能不能与原来道路重叠
2.增加保存/载入/排行榜功能
		
可能的改进：
1.能不能锁视角？
2.能不能关卡设置更有趣一些？
3.静态风景如山脉能不能随机增加而不是只有一座在起点？
4.音效和其他视觉效果能不能炫酷一些？

注:路面地基长高宽为（4，2，4）（也可能是一半），请保证您的地基方块中心相对返回节点的位置为0，0，0

关于小游戏植入:
目前的想法是并行植入，如果没打过就终止前方路面生成(断头路.jpg)
然后角色掉下去(一般游戏剧本),或者停住(傻子才会跳崖)。
