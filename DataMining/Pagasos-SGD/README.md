## Project: DM Assignment 4 - Training Classifiers via SGD
@date: 2015-11-23
@contact: qiufengyu1024@gmail.com
@paper-related:[Pegasos: Primal Estimated sub-GrAdient SOlver for SVM](http://download.springer.com/static/pdf/272/art%253A10.1007%252Fs10107-010-0420-4.pdf?originUrl=http%3A%2F%2Flink.springer.com%2Farticle%2F10.1007%2Fs10107-010-0420-4&token2=exp=1448292056~acl=%2Fstatic%2Fpdf%2F272%2Fart%25253A10.1007%25252Fs10107-010-0420-4.pdf%3ForiginUrl%3Dhttp%253A%252F%252Flink.springer.com%252Farticle%252F10.1007%252Fs10107-010-0420-4*~hmac=9578382f94b76a16d6fd84b2135818736ab26c37fcc36358222d8ce647035126)

### Task
Please implement two algorithms which are tailored to large-scale classification. Given the datasets with a large number of examples, we will train classifiers via stochastic gradient descent (SGD) in order to make training phase more efficient.

#### Dataset
1. Data: We adopt two datasets which come from here. Each dataset contains both training set and testing set. (Please use editors like Sublime or Notepad++ to open them. )
  a. Dataset 1 (Training) Download, Dataset 1 (Testing) Download
  b. Dataset 2 (Training) Download, Dataset 2 (Testing) Download
2. Data Description and Format: Dataset 1 (Training) contains 22696 rows and 124 columns, and dataset 1 (Testing) contains 9865 rows and 124 columns; Dataset 2 (Training) contains 32561 rows and 124 columns, and dataset 2 (Testing) contains 16281 rows and 124 columns. Each row represents an example. The last column represents the label of the corresponding example, and the remaining columns represent the features of the corresponding example. For each dataset, label mathcal{Y} in {-1,+1}.

#### Task Description
Please first download the article, and then accomplish the following two tasks which are depicted in the article. Please implement the algorithms by yourself, and do not invoke other existing codes or tools. But, for basic vector or matrix operations and random number generation, you may use the existing codes or tools.

#### Task 1: Binary classification with the hinge-loss on dataset 1 and 2.
1. Training phase (on the training set): Please implement the Pegasos algorithm which is depicted in the Section 2.1 Figure 1 of the article (page 4-5) you have downloaded.
  a. Please note that the bias term here is ignored, i.e., b=0.
  b. For simplicity, please ignore line 10 in the Figure 1 of the article when implementing the Pegasos algorithm.
  c. Let m denote the number of examples in the training set, set lambda=10^{-4} for training dataset 1 and lambda=5times 10^{-5} for training dataset 2, and set T=5m.
2. Testing phase (on the testing set): Please test the classifier you have trained and record the test error in the testing set, i.e., comparing the label that the classifier predicts with the true label and recording the error rate.

#### Task 2: Binary classification with the log-loss on dataset 1 and 2.
1. Training phase (on the training set): Replace the hinge-loss with log-loss, and keep the others as same as the the Pegasos algorithm mentioned in the Task 1. The definition of log-loss and its sub-gradient can be found in the Section 5 (Example 1 and the table therein) of the article.
2. Testing phase (on the testing set): As same as the Task 1.

#### How to present the results of Task 1 and 2:
The report should contain four figures and a table that show the test error with respect to the number of iterations t for each loss function on each dataset. Specifically,
1. for the four figures: x-axis of the figure is the number of iterations t in the training phase, and y-axis is the test error ranging from 0 to 1. For each dataset and each loss function (four figures in total), please plot the test error when t=0.1T, 0.2T, 0.3T, ldots, 0.9T, T (where T=5m and m is the number of examples in the training set) with 10 points, and connect these 10 points with lines.
2. for the table: arrange the value of test error with respect to t=0.1T, 0.2T, 0.3T, ldots, 0.9T, T for each loss function on each dataset depicted above in a table.

### Execute
1. Ensure the training file and testing file in the same directory, and do NOT change the name when you download it from the website.                                                       |
2. Parameters in pagasos:
   * without parameters: train and test on dataset1 with pagasos with the hinge-loss by default
   * with only 1 parameter: pagasos(x) -> train and test on dataset1 with pagasos with log-loss
                                       -> if x == 2, otherwize, pagasos with hinge-loss.
   * with 2 parameters: pagasos(x,y) -> if x == 2, pagasos with log-loss, otherwize, with hinge-loss
                                     -> if y == 2, pagasos on dataset2, otherwize, on dataset1
3. The result of error will show in the command window of Matlab, together with a figure as required.

### Configuration
1. with testing file and training in the same directory
2. Test environment: Windows 7 64-bit, MatLab 2015b, JDK 1.8.0_60
3. Any question, or if you have any problem when running the program, do not hesitate to contact me!
4. Thanks & Regards
