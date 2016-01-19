import os
import sys
import numpy
import scipy
import xgboost as xgb
from cut import quadratic_weighted_kappa

def eval_wrapper(yhat, y):  
    y = numpy.array(y)
    y = y.astype(int)
    yhat = numpy.array(yhat)
    yhat = numpy.clip(numpy.round(yhat), numpy.min(y), numpy.max(y)).astype(int)   
    return quadratic_weighted_kappa(yhat, y)

def apply_offset(data, bin_offset, sv, scorer=eval_wrapper):
    # data has the format of pred=0, offset_pred=1, labels=2 in the first dim
    data[1, data[0].astype(int)==sv] = data[0, data[0].astype(int)==sv] + bin_offset
    score = scorer(data[1], data[2])
    return score


print('Testing 1')
print('Loading files...')

train = xgb.DMatrix('.\\trainsvm.txt')
test = xgb.DMatrix('.\\testsvm.txt')

idx = numpy.loadtxt('.\\testid.txt', converters={0: lambda x:int(x)})

# setup parameters for xgboost
param = {}
param['objective'] = 'reg:linear'
# param['booster'] = 'gbtree'
# scale weight of positive examples

#param['eta'] = 0.05
#param['max_depth'] = 15
#param['subsample'] = 0.8
#param['colsample_bytree'] = 0.7
#param['min_child_weight'] = 2

param['eta'] = 0.05
param['max_depth'] = 12
param['subsample'] = 0.71
param['colsample_bytree'] = 0.6
param['min_child_weight'] = 40
param['gamma'] = 0.5
num_round = 2500

param['silent'] = 1
param['nthread'] = 10

print('Training Model...')
watchlist = [(train,'train'), (test, 'test')]
bst = xgb.train(param, train, num_round, watchlist);

print('Predicting...')
# get prediction
ypred = bst.predict(test)
ypredtrain = bst.predict(train)
ypred = numpy.clip(ypred, -1.99, 7.99)
ypredtrain = numpy.clip(ypredtrain, -1.99, 7.99)

print('Training offset...')
# train offsets 
offsets = numpy.ones(8)*(-0.5)
offsetpredtrain = numpy.vstack((ypredtrain, ypredtrain, train.get_label()))
for j in range(8):
    train_offset = lambda x: -apply_offset(offsetpredtrain, x, j)
    offsets[j] = scipy.optimize.fmin_powell(train_offset, offsets[j])

print(offsets)

# apply offsets to test
data = numpy.vstack((ypred, ypred, test.get_label()))
for j in range(8):
    data[1, data[0].astype(int)==j] = data[0, data[0].astype(int)==j] + offsets[j] 

final_test_preds = numpy.round(numpy.clip(data[1], 0, 7)).astype(int)

# write out predictions
outfile = 'xgb2500.csv'
fo = open(outfile, 'w')
fo.write('Id,Response\n')
for i in range(len(ypred)):
    realy = final_test_preds[i]+1
    fo.write('%d,%d\n' % ((idx[i]),realy))
fo.close()

print ('Finished writing into prediction file')
