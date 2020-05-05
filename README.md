# ParkourByJava
This is a course project for Java Programming PKU 2020 spring. 
教程1：https://www.cntofu.com/book/71/index.html
教程2：http://www.jmecn.net/wiki/getting-start/index.html


关于RGB测试版
asdw操作，大概是红右绿左蓝跳跃，开始按空格键

更改人物模型：
请重写 class RoleMaker
(其实只用重写一个函数)

添加路面方块材质：
普通地基方块请重写 class CubeMaker 中的 makeNormal函数
特殊方块(地基+上面的障碍)请重写 class CubeMaker 中的 makeSpcial函数
        并在  class CrashCheck中  重写对应判定规则
如果特殊方块(障碍)总类型多于3种，请修改常数NUMOFTYPES

注:路面地基宽度为4，长度为1，请保证您的地基方块中心相对返回节点的位置为0，0，0

关于小游戏植入:
目前的想法是并行植入，如果没打过就终止前方路面生成(断头路.jpg)
然后角色掉下去(一般游戏剧本),或者停住(傻子才会跳崖)。