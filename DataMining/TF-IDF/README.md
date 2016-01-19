## Project: DM Assignment 1 - Chinese Text Data Processing
@date: 2015-10-08
@contact: qiufengyu1024@gmail.com
### Dataset
1. Data Source: Forum Classification based on the posts from the Lily BBS)
2. Data Description and Format: The zip file contains 10 txt files (10 classes). Each txt file contains more than 100 lines, and each line represents a raw post. You may regard each line in each txt file as an instance/example of the corresponding class.

### Task Description
1. First, please do the tokenization job to get the words from the Chinese sentences. You may use some existing tools like jieba for python, or IKAnalyzer for java, to help you accomplish the tokenization job.
2. Second, please delete the stop words. We provide a Chinese stop words list for your reference, and you can download it.
3. Third, please extract TF-IDF (Term Frequency¨CInverse Document Frequency) features from the raw text data on the basis of step 1 and step 2 above. You may learn or review TF-IDF here. Please implement extracting TF-IDF features by yourself, and do NOT invoke other existing codes or tools.
4. Four, please generate 10 new txt files which only contain TF-IDF features for each class, each line in each new txt file represents the TF-IDF feature of an instance/example in the corresponding class. Please name the 10 new txt files as same as the original 10 txt files that we provide.
