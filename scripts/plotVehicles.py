import numpy as np
import matplotlib.pyplot as plt
from matplotlib import animation

filename = "dataCircle.dat"
f = open(filename, 'r')

data = []
timestep = []

for line in f:
    line = line.split()
    if len(line) == 0:
        if len(timestep) != 0:
            data.append(timestep)
        timestep = []
    else:
        line[0], line[1] = float(line[0]), float(line[1])
        timestep.append(line)

data = np.array([np.array(xi) for xi in data])

fig, ax = plt.subplots()
line, = ax.plot([], [], 'o')

ax.margins(0.05)

def init():
    line.set_data([],[])
    return line,

def animate(i):
    i = min(i, len(data)-1)
    xdata = data[i,:,0]
    ydata = data[i,:,1]
    line.set_data(xdata, ydata)
    ax.set_xlim([-100, 100])
    ax.set_ylim([-100, 100])
    return line,

anim = animation.FuncAnimation(fig, animate, init_func=init, interval=25)

plt.show()
