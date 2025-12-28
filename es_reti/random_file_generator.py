import os

os.mkdir("prova")

s = "./prova/prova_"
n = 5 #n file da creare
len = 100
d = 100 #d * len == dim file da creare

for i in range(n):
	with open(s + str(i+1), "w") as f:
		for j in range(len):
			f.write("a"*d);
	len = len * 10
	
