function nmf()
tol = 0.000001;
maxiter = 2000;
%dataset1:
k = 2;
x = csvread('german.txt',0,0,[0,0,999,23]);
% for dataset2
% x = csvread('mnist.txt',0,0,[0,0,9999,783]);
% k = 10;
[m,n] = size(x);
x = x';
% x: n rows by m columns
u = abs(rand(n, k));
% u: n rows by k columns
v = abs(rand(m, k));
% v: m rows by k columns
for j = 1:maxiter
    % fprintf('iterator = %d\t', j);
    % Multiplicative update formula
    %update U
    xv = x*v;
    vtv = v'*v;
    uvtv = u*vtv;
    u = u .* ((xv) ./ (uvtv + eps(xv)));
    %update V
    xtu = x'*u;
    utu = u'*u;
    vutu = v*utu;
    v = v.* ((xtu) ./ (vutu + eps(xtu)));
    % when stop
%     current_tol = norm(x-u*(v'));   
%     if(current_tol<tol)
%         break;
%     end
%      if(mod(j,200)==0)
%         fprintf('iterator = %d\tcurrent tolerance: %f\n', j, current_tol);
%     end
end

% normalize
sumu = sum(u.*u);
sqrtsumu = sqrt(sumu);
sqrtsumu_m = (repmat(sqrtsumu', 1, m))';
sqrtsumu_n = (repmat(sqrtsumu', 1, n))';
v = v .* sqrtsumu_m;
u = u ./ sqrtsumu_n;
[value, label] = max(v, [], 2);
fid_write = fopen('nmf.txt', 'wt');
for i = 1:m
    fprintf(fid_write, '%d\n', label(i));
end

