# WeatherPredictModel

HMM 隐马尔科夫模型的MapReduce并行化程序 感谢@https://github.com/pyyyyyyy @https://github.com/xiaoshankeji

jar 包的运方式式为：hadoop jar WeatherPredictModel.jar 4 X inputPath outputPath IterationTime

其中，4为观察变量状态大小，X 为隐变量个数，inputPath 为输入文件路径，outputPath 为输出文件路径，IterationTime 为迭代次数。
