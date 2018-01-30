load quasar_train.csv;
lambdas = quasar_train(1, :)';
train_qso = quasar_train(2:end, :);

[m, n] = size(train_qso);

load quasar_test.csv;
test_qso = quasar_test(2:end, :);

mtest = size(test_qso,1);

train_smooth = zeros(size(train_qso));
test_smooth = zeros(size(test_qso));

% for i = 1:m
%     train_smooth(i,:) = (weighted_regression(lambdas, train_qso(i,:)', 5))';
% end
% for i = 1:mtest
%     test_smooth(i,:) = (weighted_regression(lambdas, test_qso(i,:)', 5))';
% end
% 
% save('train.mat', 'train_smooth');
% save('test.mat', 'test_smooth');

load train.mat;
load test.mat;

train_left = train_smooth(:,1:50);
train_right = train_smooth(:, 150:end);
test_left = test_smooth(:,1:50);
test_right = test_smooth(:, 150:end);

%% calculate nearest neighbors
train_dist = zeros(m,m);
for i = 1:m
    for j = i+1:m
        diff = train_right(i,:)-train_right(j,:);
        train_dist(i,j) = diff*diff';
    end
end

train_dist = train_dist+train_dist';
train_dist = train_dist/max(train_dist(:));

nearest = 3;
fleft = zeros(m, 50);
for i = 1:m
%     [train_dist_sort, inds] = sort(train_dist(:, i), 1, 'ascend');
%     close_inds = ones(m, 1);
%     close_inds(inds(nearest+1:end)) = 0;
%     h = max(train_dist(:, i));
%     kerns = max(1-train_dist(:, i) /h, 0);
%     kerns = kerns .* close_inds;
%     fleft(i, :) = train_left'*kerns /sum(kerns);
    [train_dist_sort, ind] = sort(train_dist(i,:), 2, 'ascend');
    h = max(train_dist(i, :));
    ker = zeros(m,1);
    for j = 1:nearest
        ker(ind(j)) = max(1-train_dist(i, ind(j))/h, 0);
    end
    fleft(i, :) = (train_left'*ker/sum(ker))';
end

%% error of part(c)(ii)
err = sum( (fleft(:)-train_left(:)).^2 )
errrate = err/m

%% test example 1 and 6
train_test_dist = zeros(mtest, m);
for i = 1:mtest
    for j = 1:m
        diff =train_right(j) - test_right(i);
        train_test_dist(i, j) = diff*diff';        
    end
end
train_test_dist = train_test_dist/max(train_test_dist(:));
% train_test_dist = train_test_dist';

flefttest = zeros(mtest, 50);

for i=1:mtest
    [train_test_dist_sort, ind] = sort(train_test_dist(i,:), 2, 'ascend');
    h = max(train_test_dist(i, :));
    ker = zeros(m,1);
    for j = 1:nearest
        ker(ind(j)) = max(1-train_test_dist(i, ind(j))/h, 0);
    end
    flefttest(i, :) = (train_left'*ker/sum(ker))';
%     [train_dist_sort, inds] = sort(train_test_dist(:, i), 1, 'ascend');
%     close_inds = ones(m, 1);
%     close_inds(inds(nearest+1:end)) = 0;
%     h = max(train_test_dist(:, i));
%     kerns = max(1-train_test_dist(:, i) /h, 0);
%     kerns = kerns .* close_inds;
%     flefttest(i, :) = train_left'*kerns /sum(kerns);
end

err = sum( (flefttest(:)-test_left(:)).^2 )
errrate = err/mtest

%% plot pictures
figure;
h = plot(lambdas, test_smooth(1,:), 'k-');
hold on;
h = plot(lambdas(1:50), flefttest(1,:), 'r-');
h = legend('Smoothed data', 'estimated f_{left}');
h = title('Example 1');

figure;
h = plot(lambdas, test_smooth(6,:), 'k-');
hold on;
h = plot(lambdas(1:50), flefttest(6,:), 'r-');
h = legend('Smoothed data', 'estimated f_{left}');
h = title('Example 6');

