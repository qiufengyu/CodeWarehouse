
function cost = cost(theta, X, y, lambda)

% Your code goes here.
theta_one = theta
theta_one(1) = 0
m = size(X,1)
cost = ((X*theta-y)'*(X*theta-y)+lambda*(theta_one'*theta_one))/(2*m)


% Write a function in Octave that computes the gradient for the cost function of regularized linear regression (taking in the same arguments as your cost function above). Again, remember to vectorize as much of your function as you can.

function grad = grad(theta, X, y, lambda)

% Your code goes here.

[m, n] =size(X)
theta_one = theta(1)
grad = zeros(size(theta))
grad = (X'*(X*theta-y)+lambda*theta)/m
grad(1) = grad(1)-lambda/m*theta_one

