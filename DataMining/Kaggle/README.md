## Project: DM Assignment 6 - Kaggle Competition

@author: Godfray

@date: 2016-01-10

@contact: qiufengyu1024@gmail.com

### Task
[Prudential Life Insurance Assessment](https://www.kaggle.com/c/prudential-life-insurance-assessment) on [Kaggle](https://www.kaggle.com/)

### Execute and requirements:
1. For all codes, please put the train and test csv files in the same directory!!!
2. You would better import the java codes into eclipse, and run directly!
3. For python codes, when configured environment, you can run direclty, but you need to run java codes to genenrate the formatted train, test, and a testid file.

### 具体说明如下：

#### 1. CFDT
由之前的实验改编，没有引用外界工具包，代码导入Eclipse中，并将train/test.csv放置在根目录下，即可运行

#### 2. Weka
Java环境，在当前目录下放置train.csv和test.csv文件，生成Weka专用的arff格式文件，代码中给出了一个简单的demo，可以进行模型训练和预测。需要调用weka.jar依赖包，请访问[Weka主页](http://www.cs.waikato.ac.nz/ml/weka/)获取

#### 3. xgboost
Java用于数据预处理，生成xgboost支持的libsvm格式输入文件，请将train.csv和test.csv文件放置在运行目录下，生成三个文件请按照Python代码的要求，放置在对应目录下，这里是当前Python的运行目录。而Python用于xgboost的训练和预测。xgboost安装配置请参见[项目主页](https://github.com/dmlc/xgboost)，需要[numpy](http://sourceforge.net/projects/numpy/files/NumPy/)、[scipy](http://sourceforge.net/projects/scipy/files/scipy/)和[scikit-learn](http://scikit-learn.org/)包的支持，并包括用于分割点优化的模块cut，为[ml_metrics包](https://github.com/benhamner/Metrics/blob/master/Python/ml_metrics/quadratic_weighted_kappa.py)的一部分。
注：xgboost同样支持Java，xgboost4j.jar安装同样参考，注意其他需要的jar包，可用于简单的训练和预测。但所附代码并没有调用，交由python代码执行，仅保留了生成libsvm格式文件和生成辅助Python程序的testid文件的代码，其他部分以注释形式存在。

#### 4. ensemble
Java代码，请将预测结果放在代码对应的results目录下，并且将3中辅助文件的testid.txt一同放置在results目录下，每个需要ensemble的文件名为xxxxx.csv，xxxxx为kaggle给出的Score，作为权重。

### Configuration:
1. with *.txt in the same runtime directory
2. Test environment: Windows 7 64-bit, JDK 1.8.0_60, Python 2.7, Eclipse Mars，other requirements please refer to latest version.
3. Any question, do not hesitate to contact me!
4. Thanks & Regards
