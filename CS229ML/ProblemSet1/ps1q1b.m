X = load('logistic_x.txt');
y = load('logistic_y.txt');

pos = find(y == 1);
neg = find(y ~= 1);

X = [ones(size(y)), X];
m = size(X,1);
n = size(X,2);

figure;hold on;
plot(X(pos,2:2), X(pos, 3:3), 'k+');
plot(X(neg,2:2), X(neg, 3:3), 'ko', 'MarkerFaceColor', 'y');

theta = zeros(n,1);
max_iter = 100;
grad_l = zeros(n,1);
all_one = ones(m,1);
H = zeros(n);

for i = 1:max_iter
    g = sigmoid(y .* (X* theta));  % sigmoid function
    grad_l = X'*((all_one - g) .* y) /(-m);
    H = (X'* diag(g .* (all_one - g)) *X)/m;
    old_theta = theta;
    fprintf('iteration %d', i);
    theta = theta - pinv(H)*grad_l
    if (old_theta - theta)'*(old_theta - theta)<0.0000001
        break;
    end
end

x1 = min(X(:,2)):.01:max(X(:,2));
x2 = -(theta(1) / theta(3)) - (theta(2) / theta(3)) * x1;
plot(x1,x2, 'linewidth', 2);
xlabel('x1');
ylabel('x2');



