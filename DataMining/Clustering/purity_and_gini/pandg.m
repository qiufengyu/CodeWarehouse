function pandg()
k = 2;
Ground = load('ground.txt');
Result = load('kmeans1_10.txt');
m = zeros(k,k);
[x, y] = size(Ground);

% trick, to calculate m
% -1 in ground -> 2; for dataset 1
% but ground all plus 1 for dataset 2
% for result, as it is
if k == 2
    for i=1:x
        if Ground(i) == -1
            Ground(i) = 2;
        end
    end
end
if k == 10
    for i=1:x    
        Ground(i) = Ground(i)+1;
    end
end
% calculate m_ij
for i=1:x
    m(Ground(i),Result(i)) = m(Ground(i),Result(i))+1;
end

% M_j and P_j
Mj = zeros(k);
Mj = Mj(:,1);
Pj = zeros(k);
Pj = Pj(:,1);
for i=1:k
    summ = 0;
    maxp = 0;
    for j=1:k
        summ = summ + m(j,i);
        if(maxp < m(j,i))
            maxp = m(j,i);
        end
    end
    Mj(i) = summ;
    Pj(i) = maxp;
end

% purity
purity = sum(Pj)/sum(Mj)

% Gj
Gj = zeros(k);
Gj = Gj(:,1);
for i=1:k
    squaresum = 0;
    for j=1:k
        if m(j,i) ~= 0
            squaresum = squaresum+(m(j,i)/Mj(i))^2;
        end
    end
    Gj(i) = 1- squaresum;
end
% Gavg
gini = sum(Gj.*Mj)/sum(Mj)