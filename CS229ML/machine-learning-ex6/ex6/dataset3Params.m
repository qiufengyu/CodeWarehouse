function [C, sigma] = dataset3Params(X, y, Xval, yval)
%DATASET3PARAMS returns your choice of C and sigma for Part 3 of the exercise
%where you select the optimal (C, sigma) learning parameters to use for SVM
%with RBF kernel
%   [C, sigma] = DATASET3PARAMS(X, y, Xval, yval) returns your choice of C and 
%   sigma. You should complete this function to return the optimal C and 
%   sigma based on a cross-validation set.
%

% You need to return the following variables correctly.
C = 1;
sigma = 0.3;

% ====================== YOUR CODE HERE ======================
% Instructions: Fill in this function to return the optimal C and sigma
%               learning parameters found using the cross validation set.
%               You can use svmPredict to predict the labels on the cross
%               validation set. For example, 
%                   predictions = svmPredict(model, Xval);
%               will return the predictions on the cross validation set.
%
%  Note: You can compute the prediction error using 
%        mean(double(predictions ~= yval))
%


Clist = [0.01; 0.03; 0.1; 0.3; 1; 3; 10; 30];
sigmalist = [0.01; 0.03; 0.1; 0.3; 1; 3; 10; 30];
cl = length(Clist);
sl = length(sigmalist);

minerror = 999999;
mini = 0;
minj = 0;

for i = 1:cl
    for j = 1:sl        
        model= svmTrain(X, y, Clist(i), @(x1, x2) gaussianKernel(x1, x2, sigmalist(j)));
        pred = svmPredict(model, Xval);
        error = mean(double(pred ~= yval));
        if error < minerror
            minerror = error;
            mini = i;
            minj = j;
        end    
    end    
end

if minj ~= 0 && mini ~= 0
    fprintf('C = %f\n', Clist(mini));
    fprintf('sigma = %f\n', sigmalist(minj));    
end

C = Clist(mini);
sigma = sigmalist(minj);

% =========================================================================

end
