# NLP Assignment6 - Machine Translation with IBM Model 1

@author: Godfray

@date: 2016-01-14

@contact: qiufengyu1024@gmail.com

### Introduction

Use IBM Model 1 to do Alignment Task, English and Chinese

Run over 100 sentences ([test.ch](https://github.com/qiufengyu/NLPAssignments/blob/master/IBM%20Model%201/code/test.ch.txt), [test.en]((https://github.com/qiufengyu/NLPAssignments/blob/master/IBM%20Model%201/code/test.en.txt))).

Compare with the given [alignment]((https://github.com/qiufengyu/NLPAssignments/blob/master/IBM%20Model%201/code/test.align.txt)) given by people.

### Execute

Tested On： Windows 7 64-bit, Core i3 @2.30GHz, RAM: 6GB；JDK：1.8.0_60，with Eclipse Mars

把双语语料(test.en.txt, test.ch.txt) 和给定的Alignment(test.align.txt) 作为程序的输入，编译及运行方法：

`javac IBMTest.java`

`java IBMTest`

最终生成的文件主要有t(c|e) 表(matrixXXX.txt)、t(e|c) 表(matrixXXXInverse.txt)，XXX 表示迭代的次数，以及对其结果myalignment.txt，命令行输出运行情况、对齐的准确率和迭代次数及运行时间。

### References

* [IBM模型学习总结](http://luowei828.blog.163.com/blog/static/31031204201123010316963/)

* [Philipp Koehn - Machine Translation - Statistical Machine Translation](http://www.inf.ed.ac.uk/teaching/courses/mt/)

* [Michael Collins - Natural Language Processing Notes on Statistical Machine Translation: IBM Models 1 and 2](http://www.cs.columbia.edu/~cs4705/)

* [Dark_Scope - NLP 学习笔记 04 (Machine Translation)](http://blog.csdn.net/dark_scope/article/details/8774000)

* [Philipp Koehn and Adam Lopez - Machine Translation - Reading Materials on Word alignment and the expectation maximization algorithm](http://www.inf.ed.ac.uk/teaching/courses/mt/syllabus.html)
* Liang P, Taskar B, Klein D. Alignment by agreement[C]
Proceedings of the main conference on Human Language Technology Conference of the North American Chapter of the Association of Computational Linguistics. Association for Computational Linguistics, 2006: 104-111.

