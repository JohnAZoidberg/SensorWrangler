#! /usr/bin/env python3
import random
import sys
import time

try:
    lowerBound = int(sys.argv[2])
    upperBound = int(sys.argv[3])
    f = open(sys.argv[1], "w")
except:
    print("""Usage:
  ./random-sensor.py [LOGFILE] [LOWER_BOUND] [UPPER_BOUND]
  
  Writes random values between LOWER_BOUND and UPPER_BOUND to LOGFILE.
    
Example
  ./random-sensor.py sensor 80 100 &; tail -f sensor
  80
  84
  83
  ...""")
    sys.exit(1)


while True:
    f.write("{}\n".format(random.randint(lowerBound, upperBound)))
    f.flush()
    time.sleep(0.5)
