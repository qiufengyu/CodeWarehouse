## Project: DM Assignment 3 - Clustering
@date: 2015-11-08
@contact: qiufengyu1024@gmail.com

### Task
Please implement three clustering algorithms: k-means, clustering by non-negative matrix factorization (NMF), and spectral clustering.
#### Dataset
Data: The two datasets come from Statlog/German and the MNIST database of handwritten digits, respectively. Some modifications are made by us from the original datasets, e.g., normalization. (Please use editors like Sublime or Notepad++ to open them. )

1. Dataset 1 Download
2. Dataset 2 Download
Data Description and Format: Dataset 1 contains 1000 rows and 25 columns, and dataset 2 contains 10000 rows and 785 columns. Each row represents an example. The last column represents the label of the corresponding example, and the remaining columns represent the features of the corresponding example. Namely, dataset 1 contains 1000 examples, and each example has 24 features and 1 label; dataset 2 contains 10000 examples, and each example has 784 features and 1 label. The number of classes in dataset 1 is 2, and the number of classes in dataset 2 is 10.
#### Task Description
Please download the task description PDF file and implement three clustering algorithms described in it. Some requirements and suggestions are listed below:
1. Please simply set the number of clusters for each dataset as the number of classes, i.e., the number of clusters for dataset 1 is 2 and the number of clusters for dataset 2 is 10.

2. The label information in each dataset is used to calculate the Purity and Gini index only.

3. Please implement three algorithms by yourself, and do not invoke other existing codes or tools. But, for computing generalized eigenvalue, generalized eigenvector, and matrix inverse, you may use the existing codes or tools.

4. You may download the two papers below which are mentioned in the task description PDF file:

(a) Xu et al. Document clustering based on non-negative matrix factorization. In SIGIR, 2003.

(b) Belkin and Niyogi. Laplacian Eigenmaps and Spectral Techniques for Embedding and Clustering. In NIPS 14, 2001.

### Execute
1. For kmeans algorithm, double click kmeans.jar in the /code/kmeans/ folder or use javac and java command,
 result.txt is the result label.

2. For NMF and Spectral algorithm, execute in Matlab, and the a result file end with .txt will generated.

3. Attention that the program are default to execute on the smaller scale data(german.txt) as the dataset! If you want to test the larger one, change the parameters such as K, M, N and everything related to dataset and the file name! All these have be a part of the comment in the program code file.

4. For purity and gini index calculation, please make sure that the result file is formatted with 1000(10000)rows
   and only 1 column!! The ground.txt file is directly from the dataset file, tagged with {-1,1} or {0-9}. Then run
   it in MatLab, and the two value will show on screen.(with formatted file in the folder, :) )

5. In my result files, all the labels start from 1, end with 2 and 10 according to the test dataset.

### Configuration
1. with german.txt(mnist.txt) in the same directory
2. Test environment: Windows 7 64-bit, MatLab 2015b, JDK 1.8.0_60, Eclipse Mars
3. Any question, or if you have any problem when running the program, do not hesitate to contact me!
4. Thanks & Regards
