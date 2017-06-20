import numpy as np
import matplotlib.pyplot as plt
from matplotlib import animation

# The indexes for the xpos and ypos
xpos_index = 2
ypos_index = 3

filename = "dataSquareCorner.dat"
f = open(filename, 'r')

veh_data = []
sig_data = []
veh_timestep = []
sig_timestep = []

for line in f:
    line = line.split()
    if len(line) == 0:
        if len(veh_timestep) != 0:
            veh_data.append(veh_timestep)
            sig_data.append(sig_timestep)
        veh_timestep = []
        sig_timestep = []
    else:
        # Fetch the corresponding data out of the line
        if line[0] == "veh":
            line[xpos_index], line[ypos_index] = float(line[xpos_index]), float(line[ypos_index])
            veh_timestep.append([line[xpos_index], line[ypos_index]])
        elif line[0] == "sig":
            sig_timestep.append([float(line[xpos_index]), float(line[ypos_index]), int(line[ypos_index+1])])

veh_data = np.array([np.array(xi) for xi in veh_data])
sig_data = np.array([np.array(xi) for xi in sig_data])

fig, ax = plt.subplots()
line, = ax.plot([], [], 'o')
red, = ax.plot([], [], "*", color="r")
green, = ax.plot([], [], "x", color="g")

colours = {0: "green", 1: "orange", 2: "red", 3: "orange"}

ax.margins(0.05)

def init():
    line.set_data([],[])
    red.set_data([],[])
    green.set_data([],[])
    return line,

def animate(i):
    i = min(i, len(veh_data)-1)
    xdata = veh_data[i,:,0]
    ydata = veh_data[i,:,1]
    line.set_data(xdata, ydata)
    redxsig = []
    redysig = []
    greenxsig = []
    greenysig = []
    for j in range(len(sig_data[i])):
        if 0 < sig_data[i, j, 2] < 4:
            redxsig.append(sig_data[i, j, 0])
            redysig.append(sig_data[i, j, 1])
        else:
            greenxsig.append(sig_data[i, j, 0])
            greenysig.append(sig_data[i, j, 1])
    red.set_data(redxsig, redysig)
    green.set_data(greenxsig, greenysig)
    ax.set_xlim([-10, 660])
    ax.set_ylim([-10, 660])
    return line,

anim = animation.FuncAnimation(fig, animate, init_func=init, interval=25)

plt.show()
