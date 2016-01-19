% http://blog.pluskid.org/?p=287
function spectral()
% nearest = 6 or 9
nearest = 3;
% k = 10;
% x = csvread('mnist.txt',0,0,[0,0,9999,783]);
k = 2;
x = csvread('german.txt',0,0,[0,0,999,23]);
[m, n]=size(x);
% calculate Distance between
Dist = inf(m,m);
R = zeros(m, nearest);
for i=1:m
    A = x(i,:);
    for j=i+1:m
        if i~=j
            B = x(j,:);      
            Dist(i,j) = norm(A-B,2);
        end
    end
end
% try to speed up
for i=2:m
    for j=1:i
        Dist(i,j) = Dist(j,i);
    end
end
% 
% dlmwrite('dist.txt',Dist,'newline', 'pc');
% Dist = csvread('dist.txt',0,0);

% calculate nearest
for num=1:nearest
    [tt, aa] = min(Dist,[],2);
    R(:,num) = aa';
    for i=1:m        
        Dist(i,aa(i)) = inf;
    end
end

% calculate W matrix
W = zeros(m,m);
% R: m*nearest matrix
for i = 1:m
    for j = 1:nearest
        W(i,R(i,j)) = 1;
    end
end

% W must be symmetric
for i=1:m
    for j=1:m
        if(W(i,j) == 1)
            W(j,i) = 1;
        end
    end
end

% calculate Diag D
D = diag(sum(W));
% D = zeros(m,m);
% for i = 1:m
%     s = 0;
%     for j = 1:m
%         s = s + W(j, i);
%     end
%     D(i,i) = s;
% end

% calculate L = D - W
L = D - W;

% L should be symmetric!
% the k-smallest eigenvalues and eigenvector of Ly = lambda(Dy)
opt = struct('issym', true, 'isreal', true);
[V dummy] = eigs(L, D, k, 'sm', opt);
labels = kmeans(V, k);
fid_write = fopen('spectral.txt', 'wt');
for i = 1:m
    fprintf(fid_write,'%d\n', labels(i));
end



