normalize <- function(x) {
  x <- sweep(x, 2, apply(x, 2, min))
  sweep(x, 2, apply(x, 2, max), "/")
}

Gaps <- read.csv("~/Dropbox (MIT)/Backup/Data/Gaps_Clustered_v2.txt", sep=";")
Gaps1 <- subset(Gaps, userClass==1)
Gaps2 <- subset(Gaps, userClass==2)
Gaps3 <- subset(Gaps, userClass==3)
###Gaps <- read.csv("~/Dropbox (MIT)/Backup/Data/Gaps2.txt", sep=";")

data <- Gaps

userclass <- data$userClass
callNum <- data$callNum
towerNum <- data$towerNum
towerRatio <- towerNum/callNum
activeHours <- data$activeHours
locNum <- data$locNum
dispNum <- data$dispNum
locRatio <- locNum*1.0/activeHours
dispProb <- dispNum*1.0/activeHours
startHour <- data$start %% 24
weekend <- as.numeric(floor(data$start/24)==1 | floor(data$start/24)==2)
start6 <- as.numeric(startHour >=6 & startHour <9)
start9 <- as.numeric(startHour >=9 & startHour <12)
start12 <- as.numeric(startHour >=12 & startHour <15)
start15 <- as.numeric(startHour >=15 & startHour <18)
start18 <- as.numeric(startHour >=18 & startHour <21)
start21 <- as.numeric(startHour >=21)
endHour <- startHour + (data$end - data$start)
daytime_start <- as.numeric(startHour >=6 & startHour <18)
daytime_end <- as.numeric(endHour >= 6 & startHour < 18)
dur <- endHour - startHour + 1
oneTop2 <- as.numeric(data$preLocRank <= 2 & data$sucLocRank > 2) + as.numeric(data$preLocRank > 2 & data$sucLocRank <= 2)
bothTop2 <- as.numeric(data$preLocRank <= 2 & data$sucLocRank <= 2)
tripBefore <- data$tripBefore
otherLoc <- data$otherLoc
otherLoc5 <- as.numeric(data$otherLoc<5)
otherLoc10 <- as.numeric(data$otherLoc<10)
otherLoc20 <- as.numeric(data$otherLoc<20)
tripProb <- data$tripProb
dist <- data$dist
singleTrip <- data$trip

mydata <- data
mydata <- data.frame(userclass,locNum,dispProb,daytime_start,dur,oneTop2,bothTop2,otherLoc,tripBefore,tripProb,dist,singleTrip)

#library(corrplot)
#cor_mydata <- cor(mydata, use="pairwise.complete.obs")
#corrplot(cor_mydata, method = "circle", tl.col = "black",  tl.cex = 0.8)

#mydata2 <- normalize(na.omit(mydata))
#mydata <- mydata2

test_size <- floor(0.4 * nrow(mydata))
set.seed(111)
test_ind <- sample(seq_len(nrow(mydata)), size=test_size)
training <- mydata[-test_ind,-1]
train_class <- mydata[-test_ind,1]
validation <- mydata[test_ind[1:floor(0.2*nrow(mydata))],-1]
valid_class <- mydata[test_ind[1:floor(0.2*nrow(mydata))],1]
testing <- mydata[test_ind[(floor(0.2*nrow(mydata))+1):length(test_ind)],-1]
test_class <- mydata[test_ind[(floor(0.2*nrow(mydata))+1):length(test_ind)],1]

train_set = training
valid_set = validation
test_set = testing

cluster = 1
train_set = training[train_class==cluster,]
valid_set = validation[valid_class==cluster,]
test_set = testing[test_class==cluster,]

###############
# Logistic Regression
logit.fit <- glm(singleTrip~.,data=train_set,family="binomial")
summary(logit.fit)
chisq <- with(logit.fit, null.deviance - deviance)
df <- with(logit.fit, df.null - df.residual)
Pvalue <- with(logit.fit, pchisq(null.deviance - deviance, df.null - df.residual, lower.tail=FALSE))
Fstat <- chisq/df
Rsquared <- with(logit.fit, 1-deviance/null.deviance)
logit.predict <- predict(logit.fit,valid_set,type="response")
confusion <- table(valid_set$singleTrip,logit.predict>0.5)
misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)
cost <- confusion[1,2]*2 + confusion[2,1]*1

misclassList <- numeric(13)
for (i in 1:13) {
  confusion <- table(valid_set$singleTrip,logit.predict>(0.05*(i+3)))
  misclassList[i] <- (confusion[2,1]+confusion[1,2])/sum(confusion)
}
plot(c(4:16)*0.05,misclassList,type="b",xlab="Cut-off Value",ylab="Misclassification Rate")
cutoff <- (which.min(misclassList) + 3) * 0.05
logit.predict <- predict(logit.fit,test_set,type="response")
confusion <- table(test_set$singleTrip,logit.predict>0.5)
misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)
confusion[1,1]/(confusion[1,1]+confusion[2,1])
confusion[2,2]/(confusion[1,2]+confusion[2,2])
confusion[1,2]/(confusion[1,1]+confusion[1,2])
confusion[2,1]/(confusion[2,1]+confusion[2,2])

###############
# Random Forest
#install.packages("poLCA")
library(poLCA)
lca.fit <- poLCA(singleTrip~.,nclass=3,maxiter=10000,nrep=10,data=train_set)

###############
# Random Forest
library(randomForest)
?randomForest
rf.fit <- randomForest(as.factor(singleTrip)~., data = na.omit(train_set))
print(rf.fit)
importance(rf.fit)
varImpPlot(rf.fit,main="Variable Importance")
plot(rf.fit)
rf.predict <- predict(rf.fit, valid_set, type="response")
confusion <- table(valid_set$singleTrip,rf.predict)
misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)
cost <- confusion[1,2]*2 + confusion[2,1]*1

misclassList <- numeric(9)
for (i in 1:9) {
  rf.predict <- predict(rf.fit, valid_set, type="response", cutoff=c(1-(0.1*i),0.1*i))
  confusion <- table(valid_set$singleTrip,rf.predict)
  misclassList[i] <- (confusion[2,1]+confusion[1,2])/sum(confusion)
}
plot(c(1:9)*0.1,misclassList,type="b",xlab="Cut-off Value",ylab="Misclassification Rate")

par(mfrow=c(1,2))
varImpPlot(rf.fit,main="Variable Importance")
plot(c(1:9)*0.1,misclassList,type="b",xlab="Cut-off Value",ylab="Misclassification Rate",
     main="Misclassification Rate by Cut-off Value")

rf.predict <- predict(rf.fit, test_set, type="response")
confusion <- table(test_set$singleTrip,rf.predict)
misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)

###############
# Neural Networks
library("neuralnet")
library("nnet")
#Select number of hidden nodes
m = 5:20
eval <- matrix(nrow = length(m), ncol = 3)
eval[,1] <- m
for (i in m)
{
  ann.fit <- nnet(as.factor(singleTrip)~.,  data = train_set, size = i)
  ann.predict <- predict(ann.fit, valid_set, type = 'class')
  confusion <- table(valid_set$singleTrip,ann.predict)
  misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)
  cost <- confusion[1,2]*2 + confusion[2,1]*1
  eval[i,2] <- misclass
  eval[i,3] <- cost
}
colnames(eval) <- c('Number', 'MisclassRate', 'MisclassCost')
nodesEval <- eval
#Select decay factor
m = 0:50
eval <- matrix(nrow = length(m), ncol = 3)
eval[,1] <- m
for (i in m)
{
  ann.fit <- nnet(as.factor(singleTrip)~.,  data = train_set, size = 13, decay = i/50)
  ann.predict <- predict(ann.fit, valid_set, type = 'class')
  confusion <- table(valid_set$singleTrip,ann.predict)
  misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)
  cost <- confusion[1,2]*2 + confusion[2,1]*1
  eval[i,2] <- misclass
  eval[i,3] <- cost
}
colnames(eval) <- c('Number', 'MisclassRate', 'MisclassCost')
decayEval <- eval
#Select cutoff value
c <- seq(0.1, 0.9, 0.1)
eval <- matrix(0, nrow = length(c), ncol = 3)
eval[,1] <- m
for (i in 1:length(c))
{
  ann.fit <- nnet(as.factor(singleTrip)~.,  data = train_set, size = 13, decay = 15/50)
  ann.predict <- predict(ann.fit, valid_set, type = 'raw')
  confusion <- table(valid_set$singleTrip,as.numeric(ann.predict>c[i]))
  misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)
  cost <- confusion[1,2]*2 + confusion[2,1]*1
  eval[i,1] <- i
  eval[i,2] <- misclass
  eval[i,3] <- cost
}
colnames(eval) <- c('Number', 'MisclassRate', 'MisclassCost')
cutoffEval <- eval

par(mfrow=c(1,3))
plot(nodesEval[,1],nodesEval[,2],type="b",xlab="Number of Hidden Nodes",ylab="Misclassification Rate")
plot(decayEval[,1]/50,decayEval[,2],type="b",xlab="Decay Factor",ylab="Misclassification Rate")
plot(cutoffEval[,1]/10,cutoffEval[,2],type="b",xlab="Cut-off Value",ylab="Misclassification Rate")

# Plot neural networks
library(devtools)
source_url('https://gist.githubusercontent.com/fawda123/7471137/raw/466c1474d0a505ff044412703516c34f1a4684a5/nnet_plot_update.r')
ann.fit <- nnet(as.factor(singleTrip)~.,  data = train_set, size = 13, decay = 15/50)
plot.nnet(ann.fit, nid=T)
ann.predict <- predict(ann.fit, test_set, type = 'class')
confusion <- table(test_set$singleTrip,ann.predict)
misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)
confusion[1,1]/(confusion[1,1]+confusion[2,1])
confusion[2,2]/(confusion[1,2]+confusion[2,2])
confusion[1,2]/(confusion[1,1]+confusion[1,2])
confusion[2,1]/(confusion[2,1]+confusion[2,2])

###############
# Support Vector Machine
library(e1071)
svm.fit <- svm(as.factor(singleTrip)~., data = train_set, gamma = 0.04)
svm.predict <- predict(svm.fit, valid_set, na.action=na.fail)
confusion <- table(valid_set$singleTrip,svm.predict)
misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)

svm.fit <- svm(as.factor(singleTrip)~., data = train_set, gamma = 0.36, probability = TRUE)
svm.predict <- predict(svm.fit, valid_set, probability = TRUE)
plot(svm.fit, train_set, dur~dist)

#Select gamma
m = 0:50
eval <- matrix(nrow = length(m), ncol = 3)
eval[,1] <- m
for (i in m)
{
  svm.fit <- svm(as.factor(singleTrip)~., data = train_set, gamma = i/50)
  svm.predict <- predict(svm.fit, valid_set)
  confusion <- table(valid_set$singleTrip,svm.predict)
  misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)
  cost <- confusion[1,2]*2 + confusion[2,1]*1
  eval[i,2] <- misclass
  eval[i,3] <- cost
}
colnames(eval) <- c('Number', 'MisclassRate', 'MisclassCost')
gammaEval <- eval

misclassList <- numeric(7)
for (i in 2:8) {
  confusion <- table(valid_set$singleTrip,attr(svm.predict,"probabilities")[,1]>(0.1*i))
  misclassList[i-1] <- (confusion[2,1]+confusion[1,2])/sum(confusion)
}

par(mfrow=c(1,2))
plot(gammaEval[,1]/50,gammaEval[,2],type="b",xlab="Gamma",ylab="Misclassification Rate")
plot(c(2:8)/10,misclassList,type="b",xlab="Cut-Off Value",ylab="Misclassification Rate")

svm.predict <- predict(svm.fit, test_set)
confusion <- table(test_set$singleTrip,svm.predict)
misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)
confusion[1,2]/(confusion[1,1]+confusion[1,2])
confusion[2,1]/(confusion[2,1]+confusion[2,2])

###############
# kNN
library(kknn)
kMisclass <- numeric()
kCost <- numeric()
kList <- numeric()
cList <- numeric()
for (i in 1:20) {
  fitTemp <- kknn(trip~.,train_set,valid_set,k=i)
  for (j in 1:9) {
    confusion <- table(valid_set$trip,fitTemp$fitted.values>(0.1*j))
    misclass <- (confusion[2,1]+confusion[1,2])/sum(confusion)
    cost <- confusion[1,2]*2 + confusion[2,1]*1
    kList <- c(kList, i)
    cList <- c(cList, j)
    kMisclass <- c(kMisclass,misclass)
    kCost <- c(kCost,cost)
  }
}
min(kCost)
kList[which.min(kCost)]
cList[which.min(kCost)]
plot(kMisclass~kList,xlab="k",ylab="Misclassification Rate")
plot(kCost~kList,xlab="k",ylab="Misclassification Cost")
lines(kCost~kList)