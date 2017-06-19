import numpy as np
import matplotlib.pyplot as plt
from matplotlib import animation

# The indexes for the xpos and ypos
xpos_index = 1
ypos_index = 2

filename = "dataSquareCorner.dat"
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
        # Fetch the corresponding data out of the line
        line[xpos_index], line[ypos_index] = float(line[xpos_index]), float(line[ypos_index])
        timestep.append([line[xpos_index], line[ypos_index]])

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
    ax.set_xlim([-10, 660])
    ax.set_ylim([-10, 660])
    return line,

anim = animation.FuncAnimation(fig, animate, init_func=init, interval=25)

plt.show()
