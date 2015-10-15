#import numpy
import sys
import math
import random


def CBRank(F,n,epsilon,maxCycle,policy):
    print '\n\nOOOOOoo=======================START===========================ooOOOOO'
    count = 0
    T = 5
    error = 100
    Phi = [[0 for j in range(n)] for j in range(n)]
    H = []
    Hprevious = []
    candidatePairs = preparePairs(n)
    steps = float(maxCycle-1)
    numberOfPairs = 0
    if (steps >= 1):
        numberOfPairs = (float((n*n-n)/2-(n-1))/steps)
    print 'Number of pairs: ', numberOfPairs
    #print 'PairsSet: ', pairsSet
    #print 'Pairs: ', pairs
    print '\n=========> Cicle: ', count, ' ================'
    pairs, pairsSet = selectInitialPairs(candidatePairs,n)
    valuedPairs = userFeedback(pairs)
    Phi = initialisePhi(valuedPairs,n)
    D = initialiseD(Phi)
    F_feed = [(n-sum(Phi[i]))*10.0 for i in range(n)]
    F.append(F_feed)
    print 'Initial Chosen Pairs: ', pairs
    print 'Initial Remaining PairsSet: ', pairsSet
    print 'Initial Valued Pairs: ', valuedPairs
    print 'Initial Features: ', F
    Xf, ThetaVector = computeXfTheta(F)
    H = RankBoost(F,Xf,ThetaVector,D,T)
    print 'H: ', H
    #Xf, ThetaVector = computeXfTheta(F)
    count = 1
    while (not (maxCycle == count)): #((error > epsilon) and not (maxCycle) and not (count > 0)): # and not (pairsSet == [])):
        print '\n=========> Cicle: ', count, ' ================'
        #policy = "RND"
        Hprevious = H
        pairs, pairsSet = selectPairs(pairsSet,H,policy,numberOfPairs)
        valuedPairs.extend(userFeedback(pairs))
        Phi = initialisePhi(valuedPairs,n)
        D = initialiseD(Phi)
        #print D
        #F.remove(F_feed)
        F_feed = [(n-sum(Phi[i]))*10.0 for i in range(n)]
        #F_feed = [(sum(Phi[:,i]))*10.0 for i in range(n)]
        F.append(F_feed)
        print 'Chosen Pairs: ', pairs
        print 'Remaining PairsSet: ', pairsSet
        print 'Valued Pairs: ', valuedPairs
        print 'Features: ', F
        Xf, ThetaVector = computeXfTheta(F)
        H = RankBoost(F,Xf,ThetaVector,D,T)
        print 'H: ', H , Hprevious
        #policy = "ALL"
        #pairs, pairsSet = selectPairs(pairsSet,H,policy,numberOfPairs)
        #valuedPairs.extend(userFeedback(pairs))
        #print 'Remaining PairsSet: ', pairsSet
        #print 'Choosen Pairs: ', pairs
        #maxCycle -= 1
        count += 1
    print 'Last Chosen Pairs: ', pairs
    print 'Final Remaining PairsSet: ', pairsSet
    print 'Final Phi: ', Phi
    print 'All Valued Pairs: ', valuedPairs
    #print 'Features: ', F
    return H
        
    
def RankBoost(F,Xf,ThetaVector,D,T):
    h = [[] for j in range(T)]
    alpha = [0 for i in range(T)]
    for t in range(T):
        h[t] = trainWeakLearner(F,Xf,ThetaVector,D)
        alpha[t] = chooseAlpha(D,h[t])
        print '--> alpha, h:', alpha[t], h[t], '\n'
        Dnew = updateD(D,h[t],alpha[t])
        D = Dnew
    H = computeH(alpha,h)
    return H


# Initialize Phi
def initialisePhi(valuedPairs,n):
    # pairs [[alt_i,alt_j,value], ...]
    Phi = [[0 for k in range(n)] for j in range(n)]
    for i in valuedPairs:
        Phi[i[0]][i[1]] = i[2]
        Phi[i[1]][i[0]] = -i[2]
    print 'Phi: ', Phi    
    return Phi

def preparePairs(n):
    pairs = []
    for i in range(n):
        for j in range(n):
            if (i!=j):
                pairs.append([i,j])
    return pairs

# The weak learner
def trainWeakLearner(F,Xf,ThetaVector,D):
    n = len(D)
    rstar = 0.0
    istar = -1
    Thetastar = -1.0
    qstar = -1.0
    h = [0.0 for i in range(n)]
    Pi = [0.0 for i in range(n)]
    for i in range(n):
        for j in range(n):
            Pi[i] = Pi[i] + (D[j][i] - D[i][j])
    print 'pi, D: ', Pi, D
    # Main Cycle
    for i in range(len(Xf)):
        L = 0.0
        R = computeR(Pi,Xf[i])
        for j in range(1,len(ThetaVector[i])):
            L = L + computeSlicesL(ThetaVector[i],Pi,F[i],j)
            if abs(L) > abs(L-R):
                q = 0.0
            else:
                q = 1.0
            #print 'q, L-q*R and rstar', q, L-q*R, rstar
            if abs(L-q*R) > abs(rstar):
                rstar = L-q*R
                istar = i
                Thetastar = ThetaVector[i][j]
                qstar = q
    h = computeWeakLearner(F, istar, Thetastar, qstar)
    return h
                

# Selection of Alpha        
def chooseAlpha(D,h):
    n = len(D)
    r = 0.0
    for i in range(n):
        for j in range(n):
            r += D[i][j]*(h[j]-h[i])
    if (1.0-r) == 0:
        alpha = 1
    else:
        alpha = (math.log1p((1+r)/(1-r))/2.0)
    return alpha


# TO DO
def computeXfTheta(F):
    Xf = []
    ThetaMatrix = []
    #print F
    for i in F:
        notsortedXfi = [[ind,value] for ind,value in enumerate(i)]
        notsortedXfi_without_nan = filter(lambda x: not math.isnan(float(x[1])), notsortedXfi)
        sortedXfi = sorted(notsortedXfi_without_nan, key = lambda x : x[1], reverse=True)
        Xfi = [i[0] for i in sortedXfi]
        Xf.append(Xfi)
        ThetaVector = [i[1] for i in sortedXfi]
        ThetaVector.insert(0,float("inf"))
        ThetaMatrix.append(ThetaVector)
    print 'Xf, thetamatrix: ', Xf,  ThetaMatrix
    return Xf, ThetaMatrix 


# Initialization of D
def initialiseD(Phi):
    n = len(Phi)
    D_1 = [[0.0 for j in range(n)] for j in range(n)]
    D = [[0.0 for j in range(n)] for j in range(n)]
    norm = 0.0
    for i in range(n):
        for j in range(n):
            if Phi[i][j] > 0:
                D_1[i][j] = Phi[i][j]
                norm += Phi[i][j]
    #print norm 
    for i in range(n):
        for j in range(n):
            D[i][j] = float(D_1[i][j]) / float(norm)
    return D 


# Update of D
def updateD(D,ht,alphat):
    n = len(D)
    Zt = 0.0
    for i in range(n):
        for j in range(n):
            Zt += D[i][j]*math.exp(alphat*(ht[i]-ht[j]))
    for i in range(n):
        for j in range(n):
            D[i][j] = (D[i][j]*math.exp(alphat*(ht[i]-ht[j]))) / Zt
    #print 'D: ', D
    return D
            
# Selection of initial the pairs
def selectInitialPairs(pairsSet,n):
    pairs = []
    pairs = choosePolicyINI(pairsSet,n)
    #pairs = choosePolicyALL(pairsSet,[]) # there is the choosePolicyINI to be used
    pairsSet = diffPairs(pairsSet,pairs)
    return pairs, pairsSet

# Selection of the pairs
def selectPairs(pairsSet,H,policy,numberOfPairs):
    pairs = []
    if (policy == "RND"):
        pairs = choosePolicyRND(pairsSet,H,numberOfPairs)
    elif (policy == "ALL"):
        pairs = choosePolicyALL(pairsSet,H)
    #pairs = choosePolicyALL(pairsSet,H)
    #pairs = choosePolicyRND(pairsSet,H,numberOfPairs)
    pairsSet = diffPairs(pairsSet,pairs) 
    return pairs, pairsSet

# Policy for pairs selection: initial set of pairs
def choosePolicyINI(pairsSet,n):
    # the simplest one
    pairsChosen = [[0,1]]
    #print pairsChosen
    for i in range(1,n-1):
        pairsChosen.append([i,i+1])
    #pairsChoosen = pairsSet[:]
    #print 'Chosen: ', pairsChosen
    return pairsChosen

# Policy for pairs selection: the simplest one (all pairs)
def choosePolicyALL(pairsSet,H):
    # the simplest one
    pairsChosen = pairsSet[:]
    #print 'Choosen: ', pairsChosen
    return pairsChosen

# Policy for pairs selection: random TO BE DEFINED
def choosePolicyRND(pairsSet,H,numberOfPairs):
    #print 'Number pairs', numberOfPairs
    pairsChosen = random.sample(pairsSet,int(numberOfPairs))
    #print 'Choosen: ', pairsChosen
    return pairsChosen

# Difference between the pairs already obtained and
# those to be asked
def diffPairs(pairsSet,pairs):
    for i in pairs:
        # remove the pair and its opposit from the set of pairs
        pairsSet.remove(i)
        pairsSet.remove([i[1],i[0]])
    #print 'Remaining: ', pairsSet
    return pairsSet


# User feedback Simulation 
def userFeedback(pairs):
    valuedPairs = []
    #valuesForPairs = [[0,-1,0],[1,0,0],[0,0,0]]
    #valuesForPairs = [[0,-1,0],[1,0,-1],[0,1,0]]
    #valuesForPairs = [[0,-1,-1],[1,0,-1],[1,1,0]]
    valuesForPairs = [[0,1,1],[-1,0,1],[-1,-1,0]]
    #valuesForPairs = [[0,-1,-1],[1,0,-1],[1,1,0]] #[[0,1,1],[0,2,-1],[0,3,-1],[1,2,1],[1,3,1],[2,3,1]]
    for i in pairs:
        #print i[1], 'more important than', i[0], '?'
        #value = input(': ')
        #valuedPairs.append([i[0],i[1],valuesForPairs[i[0]][i[1]]])
        #valuedPairs[len(valuedPairs):] = [[i[0],i[1],valuesForPairs[i[0]][i[1]]]]
         valuedPairs = valuedPairs + [[i[0],i[1],valuesForPairs[i[0]][i[1]]]]
        #valuedPairs = valuedPairs + [[i[0],i[1],value]]
    return valuedPairs


# Compute the linear combination of H
def computeH(alpha,h):
    H = [0 for i in range(len(h[0]))]
    for i in range(len(h[0])):
        for t in range(len(alpha)):
            H[i] = H[i] + alpha[t]*h[t][i]
    return H


def computeR(Pi,Xfi):
    r= 0.0
    for i in Xfi:
        r = r + Pi[i] 
    return r

def computeSlicesL(ThetaVector,Pi,Fi,j):
    sumPi = 0.0
    #print 'Thetavector, Fi, j: ', ThetaVector, Fi, j
    for i in range(len(Fi)):
        #print 'i per l: ', i
        if ((ThetaVector[j-1] >= Fi[i]) and (Fi[i] > ThetaVector[j])):
        #if ((ThetaVector[j-1] <= Fi[i]) and (Fi[i] < ThetaVector[j])):
            sumPi = sumPi + Pi[i]
            print 'F[i]: ', Fi[i]
        print 'sumPi: ', sumPi
    return sumPi

    
# Compute the weak learner from the selected function in F    
def computeWeakLearner(F, istar, Thetastar, qstar):
    h = [0.0 for i in range(len(F[istar]))]
    for j in range(len(F[istar])):
         if math.isnan(float(F[istar][j])):
             h[j] = float(qstar)
         elif F[istar][j] > Thetastar:
             h[j] = 1.0
         elif F[istar] <= Thetastar:
             h[j] = 0.0
    #print '===>>> istar, f, thetastar, qstar: ', istar, F[istar], Thetastar, qstar
    print '-> h: ', h , '\tf: ', F[istar], '\ttheta: ', Thetastar, '\tq: ', qstar, '\tistar', istar
    #print '===>>> h', h
    
    return h
 


def main ():
    #n = input('number: ')
    #n = eval(sys.argv[1])
    #F = eval(sys.argv[2])
    #maxCycle = eval(sys.argv[3])
    #epsilon = eval(sys.argv[4])
    
    n = 3
    candidatePairs = [[0,1],[0,2],[1,2]]
    epsilon = 0.0001
    maxCycle = 2
    policy = "RND"
    H = []
    #F = [[6.0, 6.0, 5.0], [40.0, 20.0, 1.0]] #'NaN']]
    #F = [[40.0, 20.0, 1.0],[6.0, 6.0, 5.0] ]
    #F = [[30.0, 60.0, 10.0],['NaN', 50.0, 1.0] ]
    #F = [[30.0, 40.0, 1.0],[50.0, 6.0, 1.0] ]
    #F = [[40.0, 20.0, 1.0],[50.0, 6.0, 1.0] ]
    #F = [[40.0, 0.0, 0.0],[50.0, 0.0, 0.0] ]
    #F = [[40.0, 20.0, 10.0],[1.0, 1.0, 1.0],[1.0, 1.0, 1.0] ]
    #F = [[1.0, 1.0, 1.0],[5.0, 40.0, 1.0],[1.0, 1.0, 1.0] ]
    #F = [[1.0, 1.0, 1.0],[1.0, 1.0, 1.0] ]
    #F = [[2.5, 1.5, 1.0]]
    #F = []
    #F = [[10.0, 20.0, 30.0]]
    #F = [[20.0, 10.0, 30.0],[100.0, 'NaN', 200.0]]
    #F = [[20.0, 10.0, 30.0],['NaN', 100.0, 200.0]]
    #F = [[20.0, 'NaN', 30.0],['NaN', 100.0, 200.0]]
    #F = [[20.0, 'NaN', 30.0],['NaN', 'NaN', 200.0]]
    #F = [[30.0, 20.0, 10.0]]
    F = [[1.0, 20.0, 'NaN'],[1.0, 30.0, 60.0] ]
    #F = [[40.0, 20.0, 1.0],[10.0, 6.0, 'NaN'] ]
    #F = [[10.0, 6.0, 'NaN'], [40.0, 20.0, 1.0] ]
    #F = [[10.0, 5.0, 'NaN'], [40.0, 20.0, 10.0] ]
    #F = [[10.0, 9.0, 1.0], [40.0, 40.0, 1.0] ]
    #F = [[3.0, 10.0, 1.0], [10.0, 9.0, 1.0] ]
    #F = [[3.0, 10.0, 10.0], [10.0, 9.0, 1.0] ]
    #F = [[40.0, 20.0, 1.0], [1.0, 20.0, 40.0]]
    #F = [[1.0, 20.0, 400.0],[40.0, 20.0, 1.0]]
    #F = [[40.0, 20.0, 1.0], [1.0, 20.0, 400.0]]
    #F = [[40.0, 20.0, 1.0], [1.0, 20.0, 'NaN']]
    #F = [[40.0, 20.0, 1.0], [40.0, 10.0, 5.0], [6.0,4.0,0.0]]
    H = CBRank(F,n,epsilon,maxCycle,policy)
    print '\n=> H: ', H 
    print 'OOOOOoo========================END============================ooOOOOO\n\n'
    #ahp(pairs,n)
    

if __name__ == "__main__":
    main()

  
