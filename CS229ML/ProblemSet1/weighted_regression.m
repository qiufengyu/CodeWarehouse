function yhat = weighted_regression(XX, y, tau)
    m = length(y);
    X = [ones(m,1), XX];
    yhat = zeros(m,1);
    w = zeros(m,1);
    
    for i = 1:m
        w = exp(- (XX-XX(i)).^2 / (2*tau^2));
        W = diag(w);
        XWX = X'*W*X;
        XTWY = X'*W*y;        
        theta = pinv(XWX)*XTWY;
        yhat(i) = X(i, :)*theta;         
    end 
    
end