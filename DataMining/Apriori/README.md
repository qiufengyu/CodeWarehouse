## Project: DM Assignment 2 - Frequent Itemset Mining
@date: 2015-11-08
@contact: qiufengyu1024@gmail.com

### Dataset
1. Data: Data Download (Please use editors like Sublime or Notepad++ to open it. )
2. Data Description and Format: The dataset contains 1001 rows and 11 columns. Each column represents an item and there are 11 items. The first row represents the name of each item and they are 1,2,...,10,11. Row 2 to row 1001 represent all the transactions, and each row among them represents a transaction. In row 2 to row 1001, for each transaction, 1 represents that the corresponding item appears in this transaction and 0 represents that the corresponding item does not appear in this transaction. For example, the second row in the dataset (0 1 1 0 0 0 0 0 0 0 1) means that only items 2,3,11 appear in the first transaction.

### Task Description
Please read Section 4.4.2 of the textbook carefully, and implement the Apriori algorithm in Section 4.4.2. The three main steps of the Apriori algorithm are candidate generation, pruning, and support counting. For the support counting step in the Apriori algorithm, please implement the efficient support counting method in Section 4.4.2.1. Please implement the Apriori algorithm by yourself, and do NOT invoke other existing codes or tools.
#### Parameters of the Apriori algorithm:
1. Transactions: the dataset we provide. (Please use the name of items as we provide.)
2. Minimum Support: minsup = 0.144

#### The report should contain pseudo-codes of the three main steps of the Apriori algorithm:
1. candidate generation: non-repetitive and exhaustive way of generating candidates;
2. pruning: the level-wise pruning trick;
3. support counting (as Section 4.4.2.1): counting the number of occurrences of each candidate in the transaction dataset.
Please generate a result.txt file which contains the frequent itemsets you have mined and the corresponding support values. In order for us to evaluate your result efficiently, please organize your result as follows: each line represents a frequent itemset and its support value (the last number in each line). Please arrange the frequent itemsets in lexicographically sorted order, and use only three decimals to show the support values. Separate the numbers in each line with space. Result below is an illustration of the format of result.txt. Please note that it is not the correct answer and only for illustration.
2 0.410
2 6 0.321
2 6 10 0.157
2 10 0.303
6 0.405
6 10 0.310
10 0.421
11 0.417

### Execute
double click apriori.jar or import into Eclipse

### Configuration:
1. with assignment2-data.txt in the same directory
2. Test environment: Windows 7 64-bit, JDK 1.8.0_60, Eclipse Mars
3. Any question, do not hesitate to contact me!
4. Thanks & Regards
