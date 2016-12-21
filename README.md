# zxing-lite
Zxing精简版

根据需要对官方的zxing代码进行了精简，添加了IViewfinder接口，避免了与Activity的耦合。
项目添加zxing-lite模块依赖即可使用，对应Activity需要实现IViewfinder、SurfaceHolder.Callback接口

具体可以参考使用示例：
https://github.com/liushl/example-zxing-lite.git
