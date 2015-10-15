import sys


def ahp(pairs,n):
    # pairs [[alt_i,alt_j,value], ...]
    A = [[1 for i in range(n)] for j in range(n)]
    Anorm = [[1 for i in range(n)] for j in range(n)]
    normalise = [0 for i in range(n)]
    for i in pairs:
        A[i[0]][i[1]] = i[2]
        A[i[1]][i[0]] = 1.0/i[2]
    #print A
    for j in range(n):
        for i in range (n):
            normalise[j] = normalise[j] + A[i][j]
    #print normalise
    for i in range(n):
        for j in range(n):
            Anorm[i][j] = (1.0/n)*(A[i][j]*(1.0/normalise[j]))
            #print normalise[j]
            #print Anorm[i][j]
    nrank = [0 for i in range(n)]
    #print Anorm
    for i in range(n):
        nrank[i] = sum(Anorm[i])
    tsum = [0.0 for i in range(n)]
    for i in range(n):
        tsum[i] = nrank[i]*normalise[i]
        #print tsum
    lambdamax = sum(tsum)
    #print lambdamax
    ci = 0
    if n > 1:
        ci = (lambdamax-n)/(n-1.0)
    #print nrank #, ci
    return nrank #, ci

    
def hierarchy (m,n,pairsCriteria,pairsAlternativeTot):
    #m = input('number of criteria: ')
    #n = input('number of alternatives: ')
    #if m > 1:
    #    pairsCriteria = input('insert criteria pairs: ')
    #pairsAlternativeTot = [0 for i in range(m)]
    #for i in range(m):
    #    pairsAlternativeTot[i] = input('insert alternative pairs for criteria '+ str(i+1) + ': ')
    weights = [1 for i in range(m)]
    if m > 1:
        weights = ahp(pairsCriteria,m)
    sumRank = [0 for i in range(n)]
    for j in range(m):
        altRank = ahp(pairsAlternativeTot[j],n)
        weightedRank = map(lambda x: weights[j]*x, altRank)
        #print weightedRank
        sumRank = map(lambda x,y: x+y, sumRank, weightedRank)
    return sumRank


       

# Command line input: num_criteria (m) num_alternatives (n) list_preferences_on_criteria list_preferences_on_alternative_1 list_preferences_on_alternative_m

# Lists are in the form of list of triples [[i,j,value_a],[h,k,value_b], ...]

# Example of input for a problem with 2 criteria and 3 alternatives: 2 3 [[0,1,3]] [[0,1,2],[0,2,3],[1,2,7]] [[0,1,5],[0,2,7],[1,2,9]]
    
def main ():
    #pairs = input() #
    #pairs = input('insert pairs: ')
    #n = input('number: ')
    #print sys.argv[1]
    m = eval(sys.argv[1])
    n = eval(sys.argv[2])
    #print n
    #print m
    pairsCriteria = [[0,0,1]]
    if m > 1:
        pairsCriteria = eval(sys.argv[3])
       # print pairsCriteria
    pairsAlternativeTot = [0 for i in range(m)]
    #print pairsAlternativeTot
    for i in range(4,m+4):
       # print i
        pairsAlternativeTot[i-4] = eval(sys.argv[i])
    #print pairsAlternativeTot
    #pairs = eval(sys.argv[1])
    #[[0,1,2],[0,2,4],[1,2,2]] #[[0,1,9],[0,2,5],[1,2,3]]
    #n = int(sys.argv[2])
    print hierarchy(m,n,pairsCriteria,pairsAlternativeTot)
    #ahp(pairs,n)
    

if __name__ == "__main__":
    main()
