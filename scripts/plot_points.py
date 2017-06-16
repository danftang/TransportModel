import numpy as np
from matplotlib import pyplot as plt

filename = "cornerPoints.dat"

f = open(filename, "r")

data = []

for line in f:
    line = line.split()
    point = [float(line[0]), float(line[1])]
    data.append(point)

x_, y_ = zip(*data)

plt.plot(x_, y_, ".")
plt.axis('equal')
plt.show()
