users <- read.csv("~/Dropbox (MIT)/Backup/Data/cdr_sample_users_11.csv", sep=";")
#users <- users[which(users$combined_hours>=24 & users$combined_towers>=2),]

mixed_users <- users[which(users$call_hours>0 & users$web_hours>0),]
call_users <- users[which(users$call_hours>0 & users$web_hours==0),]
data_users <- users[which(users$call_hours==0 & users$web_hours>0),]
freq_users <- mixed_users[which(mixed_users$web_hours>360),]
occa_users <- mixed_users[which(mixed_users$web_hours<=360),]

par(mfrow=c(1,3))
boxplot(call_users$call_hours,ylim=c(0,500),ylab="Active Calling Hours",main="Call-only Users")
boxplot(occa_users$call_hours,ylim=c(0,500),ylab="Active Calling Hours",main="Mixed Users (Active 3G Hours<=360)")
boxplot(freq_users$call_hours,ylim=c(0,500),ylab="Active Calling Hours",main="Mixed Users (Active 3G Hours>360)")


par(mfrow=c(1,3))
boxplot(call_users$call_towers,ylim=c(0,500))
boxplot(occa_users$call_towers,ylim=c(0,500))
boxplot(freq_users$call_towers,ylim=c(0,500))

par(mfrow=c(1,3))
boxplot(call_users$call_towers/call_users$call_hours,ylim=c(0,2),ylab="Number of Towers per Active Calling Hour",main="Call-only Users")
boxplot(occa_users$call_towers/occa_users$call_hours,ylim=c(0,2),ylab="Number of Towers per Active Calling Hour",main="Mixed Users (Active 3G Hours<=360)")
boxplot(freq_users$call_towers/freq_users$call_hours,ylim=c(0,2),ylab="Number of Towers per Active Calling Hour",main="Mixed Users (Active 3G Hours>360)")

plot(mixed_users$web_hours, mixed_users$call_hours, pch=19, cex=0.1, xlab="Active 3G Hours", ylab="Active Calling Hours")
plot(mixed_users$web_hours, mixed_users$call_towers, pch=19, cex=0.1, xlab="Active 3G Hours", ylab="Number of Cell Towers used for Phone Calls")

par(mfrow=c(1,2))
hist(mixed_users$web_hours, breaks=seq(0,720,4), xlab="Active Data Hours", xlim=c(0,720), main="Distribution of Mixed Users (Data Usage)")
hist(mixed_users$call_hours, breaks=seq(0,720,4), xlab="Active Call Hours", xlim=c(0,720), main="Distribution of Mixed Users (Voice Calls)")

hist(call_users$call_hours, breaks=90, xlab="Active Calling Hours", xlim=c(0,720), main="Distribution of Call-Only Users")
hist(data_users$web_towers, breaks=100)



# CDR Caller Analysis
normalize <- function(x) {
  x <- sweep(x, 2, apply(x, 2, min))
  sweep(x, 2, apply(x, 2, max), "/")
}

users <- read.csv("~/Dropbox (MIT)/Backup/Data/Callers.txt", sep=";")
mixed_users <- users[which(users$callHours>0 & users$dataHours>0),]
call_users <- users[which(users$callHours>0 & users$dataHours==0),]
data_users <- users[which(users$callHours==0 & users$dataHours>0),]
freq_users <- mixed_users[which(mixed_users$dataHours>360),]
occa_users <- mixed_users[which(mixed_users$dataHours<=360),]
callers <- users[which(users$cdrCount>0),c(2:9)]

mydata <- data.frame(cbind(callers$towerCount/callers$cdrCount, callers$dispCount/callers$cdrCount))
mydata <- mydata[which(callers$dispCount > 0),]
#mydata <- normalize(callers[,1:3])

wss <- (nrow(mydata)-1)*sum(apply(mydata,2,var))
for (i in 2:10) wss[i] <- sum(kmeans(mydata, centers=i)$withinss)
plot (1:10, wss, type="b", xlab="Number of Clusters", ylab="Within groups sum of squares")

fit <- kmeans(mydata, 6)
aggregate(mydata, by=list(fit$cluster),FUN=mean)
aggregate(callers[which(callers$dispCount > 0),], by=list(fit$cluster),FUN=mean)

cor(callers)
plot(callers$cdrCount, callers$towerCount)
hist(callers$towerCount/callers$cdrCount, breaks=100)
plot(callers$cdrCount, callers$dispCount)
hist(callers$dispCount/callers$cdrCount, breaks=100)
plot(callers$callHours, callers$callCont)

# Calling Pattern
pattern <- read.table("~/Dropbox (MIT)/Backup/Data/CallPattern.txt", sep=";", quote="\"")
pattern <- pattern[,1:720]
mydata <- pattern[which(users$cdrCount>0),]

wss <- (nrow(mydata)-1)*sum(apply(mydata,2,var))
for (i in 2:10) wss[i] <- sum(kmeans(mydata, centers=i)$withinss)
plot (1:10, wss, type="b", xlab="Number of Clusters", ylab="Within groups sum of squares")

fit <- kmeans(mydata, 4)
agg <- aggregate(mydata, by=list(fit$cluster),FUN=mean)
clust <- agg[,2:721]
table(fit$cluster)
par(mfrow=c(2,2))
for(i in 1:4) {
  plot(1:720, clust[i,], type="l", ylim=c(0,1))
}

fit <- kmeans(mydata, 3)
agg <- aggregate(mydata, by=list(fit$cluster),FUN=mean)
clust <- agg[,2:721]
#write.table(clust, file="~/Dropbox (MIT)/Backup/Data/UserClassCentroids.csv", sep=",", row.names=FALSE, col.names=FALSE)
table(fit$cluster)
par(mfrow=c(2,2))

plot(1:720, clust[1,], type="l", ylim=c(0,1), main="Cluster 1", xlab="Hour of Month", ylab="Proportion of Active Usersr", axes = FALSE)
axis(2, at=seq(0,1,0.2), labels=seq(0,1,0.2), las=2)
axis(1, at=seq(0,720,24), labels=seq(0,720,24), las=2)

plot(1:720, clust[2,], type="l", ylim=c(0,1), main="Cluster 2", xlab="Hour of Month", ylab="Proportion of Active Usersr", axes = FALSE)
axis(2, at=seq(0,1,0.2), labels=seq(0,1,0.2), las=2)
axis(1, at=seq(0,720,24), labels=seq(0,720,24), las=2)

plot(1:720, clust[3,], type="l", ylim=c(0,1), main="Cluster 3", xlab="Hour of Month", ylab="Proportion of Active Usersr", axes = FALSE)
axis(2, at=seq(0,1,0.2), labels=seq(0,1,0.2), las=2)
axis(1, at=seq(0,720,24), labels=seq(0,720,24), las=2)

plot(1:720, clust[1,], type="l", ylim=c(0,1), xlab="Hour of Month", ylab="Proportion of Active Usersr", axes = FALSE)
axis(2, at=seq(0,1,0.2), labels=seq(0,1,0.2), las=2)
axis(1, at=seq(0,720,24), labels=seq(0,720,24), las=2)
lines(1:720, clust[2,], type="l", col="blue")
lines(1:720, clust[3,], type="l", col="red")
legend("topright", legend=c("User Cluster 1", "User Cluster 2", "User Cluster 3"), col=c("black","blue","red"), lty=c(1,1,1))

par(mfrow=c(1,3))
slices <- table(fit$cluster[which(callers$dataHours==0)])
lbls <- c("Cluster_1", "Cluster_2", "Cluster_3")
pct <- round(slices/sum(slices)*100)
lbls <- paste(lbls, pct)
lbls <- paste(lbls,"%",sep="")
pie(slices,labels=lbls, col=rainbow(length(lbls)), main="Call-Only Users")

slices <- table(fit$cluster[which(callers$dataHours>0 & callers$dataHours<=360)])
lbls <- c("Cluster_1", "Cluster_2", "Cluster_3")
pct <- round(slices/sum(slices)*100)
lbls <- paste(lbls, pct)
lbls <- paste(lbls,"%",sep="")
pie(slices,labels=lbls, col=rainbow(length(lbls)), main="Ocassional 3G Users")

slices <- table(fit$cluster[which(callers$dataHours>360)])
lbls <- c("Cluster_1", "Cluster_2", "Cluster_3")
pct <- round(slices/sum(slices)*100)
lbls <- paste(lbls, pct)
lbls <- paste(lbls,"%",sep="")
pie(slices,labels=lbls, col=rainbow(length(lbls)), main="Frequent 3G Users")

