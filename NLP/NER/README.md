# NLPAssignment 5 - [Named Entity Recognition](https://en.wikipedia.org/wiki/Named-entity_recognition)

@author: Godfray

@date: 2015-12-12

@contact: qiufengyu1024@gmail.com

### Data come from [BosonNLP](http://bosonnlp.com/)

### Execute

Tested On： Windows 7 64-bit, Core i3 @2.30GHz, RAM: 6GB；JDK：1.8.0_60，with Eclipse Mars

1. Simplified Markov Model
该目录中file文件夹下的所有文件为词性标注和分词的结果，由于调用API 分词有频率和次数限制，且进行一次分词需要花费1s 左右，故将分词结果存储下来，避免了频繁调用。因此分词部分在源代码中以注释的方式体现。运行时请将API 工具包BosonUnirest.jar 和file文件夹和BosonNLP_NER_6C.txt放在当前目录下
编译并运行Markov 模型生成运行结果：输出信息仅包括5次交叉验证的结果。命令行编译
方式：
javac -cp .;BosonUnirest.jar NERTest.java  !请注意-cp 之后的点！
java -cp .;BosonUnirest.jar NERTest
最终输出的是对每一类实体预测的准确率F1值

2. CRF++ Toolkit Method
将train.data，test.data 以及template 文件与可执行文件crf_learn.exe，crf_test.ext，以及引用的链接库libcrfpp.dll 放在同一文件夹下,为节约空间，将数据删除了大部分，完整数据请访问[BosonNLP_NER_6C.txt](./Markov/BosonNLP_NER_6C.txt)；请参照上一项目中的[DataPre.java](./Markov/DataPre.java)文件，生成train和test数据。
crf_learn -p 2 template train.data model >> logfile
\>\> logfile 表示将训练信息写入文件中，否则直接在命令行输出，-p 2 表示使用2 个线程运行，可以提高运行效率。即便如此，在本地主机上训练时间也需要40 分钟到1 小时左右。
crf_test -m model test.data >> result
\>\> result 表示将预测结果写入文件中，否则直接在命令行输出。

