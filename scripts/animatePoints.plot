filename="dataCircle.dat"
radius=2.5
replays=1

stats filename
if(!defined(frame) || frame==0) frame=0; dt=2; set xrange [] writeback; set yrange [] writeback
plot filename index frame using 1:2:(radius) with circles
set xrange restore
set yrange restore
pause dt
frame=frame+1
dt=0.1
if(frame<STATS_blocks) reread
pause 2
frame = 0
replays = 1
if(replays>0) reread
