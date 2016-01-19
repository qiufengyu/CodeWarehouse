function pegasos(algorithm, dataset)
% some default conditions
if nargin == 0
    dataset = 1;
    algorithm = 1;
elseif nargin == 1
    dataset = 1;
end
    
%1 or other: for dataset1
%2: for dataset2
if dataset == 2
    M = csvread('dataset1-a9a-training.txt',0,0);
    N = csvread('dataset1-a9a-testing.txt',0,0);
    lambda = 0.00005;
else
    M = csvread('dataset1-a8a-training.txt',0,0);
    N = csvread('dataset1-a8a-testing.txt',0,0);
    lambda = 0.0001;
end

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
% log loss
if algorithm == 2
    for t=1:T
        index = ceil(rand(1,1)*m); % choose index at randomly
        eta=1/(lambda*t); % set eta
        xi = x(index,:);
        yi = y(index,:);
        z = xi*w;
        % log loss
        logl = eta*yi/(1+exp(yi*z));
        w = w + logl*xi';

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
else % hinge loss
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
end

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

