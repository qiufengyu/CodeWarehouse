% load_quasar_data
%
% Loads the data in the quasar data files
%
% Upon completion of this script, the matrices and data are as follows:
%
% lambdas - A length n = 450 vector of wavelengths {1150, ..., 1599}
% train_qso - A size m-by-n matrix, where m = 200 and n = 450, of noisy
%      observed quasar spectra for training.
% test_qso - A size m-by-n matrix, where m = 50 and n = 450, of noisy observed
%       quasar spectra for testing.


%% part(b)

load quasar_train.csv;
lambdas = quasar_train(1, :)';
train_qso = quasar_train(2:end, :);

[m, n] = size(train_qso);

y = train_qso(1,:)';
X = [ones(n ,1), lambdas];

h = plot(lambdas, y, 'k+');
hold on;

theta = pinv(X'*X)*X'*y

h = plot(lambdas, theta(1)+theta(2)*lambdas, 'r-');
set(h, 'linewidth', 5);
hold on;

yhat = weighted_regression(lambdas, y, 1);
h = plot(lambdas, yhat, 'b-');
set(h, 'linewidth', 1);
hold on;

yhat = weighted_regression(lambdas, y, 5);
h = plot(lambdas, yhat, 'g-');
set(h, 'linewidth', 1);
hold on;

yhat = weighted_regression(lambdas, y, 10);
h = plot(lambdas, yhat, 'y-');
set(h, 'linewidth', 1);
hold on;

yhat = weighted_regression(lambdas, y, 100);
h = plot(lambdas, yhat, 'c-');
set(h, 'linewidth', 1);
hold on;

yhat = weighted_regression(lambdas, y, 1000);
h = plot(lambdas, yhat, 'm-');
set(h, 'linewidth', 1);
hold on;

h = legend('Raw data', 'unweighted', 'tau = 1', 'tau = 5', 'tau = 10', ...
'tau = 100', 'tau = 1000');

load quasar_test.csv;
test_qso = quasar_test(2:end, :);
