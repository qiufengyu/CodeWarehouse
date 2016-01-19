## Project: DM Assignment 5 - Random Forest and AdaBoost

@author: 121220074 Godfray

@date: 2015-12-08

@contact: qiufengyu1024@gmail.com

### Task
Implement two ensemble learning algorithms, which are Random Forest and AdaBoost, for classification tasks. Before implementing them, you should implement a base classifier first.

#### Dataset
Data: We adopt two binary classification datasets which come from [UC Irvine Machine Learning Repository](http://archive.ics.uci.edu/ml/index.html). (Please use editors like Sublime or Notepad++ to open them.)

Data Description and Format: Both of them are binary classification datasets. The first row indicates the type of each feature, i.e., 1 representing discrete feature and 0 representing numeric feature. Except for the first row, each row represents an example. Except for the first row, the last column represents the label of the corresponding example, and the remaining columns represent the features of the corresponding example.

#### Task Description
Please implement a decision tree algorithm first, and then implement two ensemble learning algorithms which use the decision tree you have implemented as the base classifier. Please implement the algorithms (both decision tree and ensemble algorithms) by yourself, and do not invoke other existing codes or tools. But, for random number generation, you may use the existing codes or tools.

#### Task 1: Decision Tree for Binary Classification.
Implement only a kind of [decision tree algorithm](https://en.wikipedia.org/wiki/Decision_tree), e.g., [ID3 algorithm](https://en.wikipedia.org/wiki/ID3_algorithm), [C4.5 algorithm](https://en.wikipedia.org/wiki/C4.5_algorithm), etc.
Handle both discrete and numerical features.
Optional: Use pruning technique to avoid over-fitting. (This step is not necessary for the assignment, but you may gain higher scores if you accomplish this step.)

#### Task 2: Random Forest for Binary Classification.
Implement the [Random Forest algorithm](https://en.wikipedia.org/wiki/Random_forest) using the decision tree you have implemented in the Task 1 as the base classifier.
Conduct [10 fold cross validation](https://en.wikipedia.org/wiki/Cross-validation_(statistics)) on dataset 1 and 2 for Random Forest, and then report the mean and standard deviation of accuracy.

#### Task 3: AdaBoost for Binary Classification. 
Implement the [AdaBoost algorithm](https://en.wikipedia.org/wiki/AdaBoost) using the decision tree you have implemented in the Task 1 as the base classifier.
Conduct [10 fold cross validation](https://en.wikipedia.org/wiki/Cross-validation_(statistics)) on dataset 1 and 2 for AdaBoost, and then report the mean and standard deviation of accuracy.

#### How to present and evaluate the results:
The report should contain a table that shows the mean and standard deviation of accuracy for Random Forest and AdaBoost on dataset 1 and 2, respectively. Please arrange the table in a proper way. The main part of scores for this assignment will be on the basis of both the performance (accuracy) and the efficiency of the algorithms you have implemented.

### Execute
command line get into ID3/RandomForest/AdaBoost directory, then complie(without .class files)

$ javac *Test.java

$ java *Test -args

Attention: only args == String german, load the dataset 2, else dataset 1 default
Here * means ID3/RandomForest/AdaBoost.

### Configuration:
1. with *.txt in the same runtime directory
2. Test environment: Windows 7 64-bit, JDK 1.8.0_60, Eclipse Mars
3. Any question, do not hesitate to contact me!
4. Thanks & Regards
