function pegasos_hinge()
% for dataset1
M = csvread('dataset1-a8a-training.txt',0,0);
lambda = 0.0001;
% for dataset2
% M = csvread('dataset1-a9a-training.txt',0,0);
% lambda = 0.00005;

% parameters
[m, n] = size(M);
x = M(:,1:n-1);
y = M(:,n:n);
w = zeros(n-1,1);
W = zeros(n-1, 10);
error = zeros(1,10);
% t = 0.1T, 0.2T, ..., 0.9T, T
Xaxis = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1];
Xpos = 1;
T = 5*m;
% hinge loss for 10 point test
for t=1:T
    index = ceil(rand(1,1)*m); % choose index at randomly
    eta=1/(lambda*t); % set eta
    xi = x(index,:);
    yi = y(index,:);
    z = xi*w;
    % hinge loss
    if yi*z<1
        w = (1-1/t)*w + eta*yi*(xi');
    elseif yi*z>=1
        w = (1-1/t)*w;
    end

    % Projection Step
    proj = (1/sqrt(lambda))/norm(w);
    if proj < 1
        w = proj*w;
    end

    % if w at the position
    if t == round(Xaxis(Xpos)*T)
        W(:,Xpos) = w;
        Xpos = Xpos+1;
    end
end

% test phrase
% for dataset1
N = csvread('dataset1-a8a-testing.txt',0,0);
% for dataset2
% N = csvread('dataset1-a9a-testing.txt',0,0);
% test phrase
[m, n] =  size(N);
X = N(:,1:n-1);
Y = N(:, n:n);
XW = X*W;
% use matlab function, not write by myself
myY = sign(XW);

% test error
for j=1:10 
	error(j) = sum(Y~=myY(:,j));
end

error = error/m;
plot(Xaxis, error);
disp(error');

