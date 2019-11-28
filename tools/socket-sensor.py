#! /usr/bin/env python3
import random
import socket
import sys
import time

try:
    port = int(sys.argv[1])
    lowerBound = int(sys.argv[2])
    upperBound = int(sys.argv[3])
except:
    print("""Usage:
  ./random-sensor.py [PORT] [LOWER_BOUND] [UPPER_BOUND]
  
  Opens socket at PORT and writes random values between LOWER_BOUND and UPPER_BOUND to it.
    
Example
  ./random-sensor.py 8000 80 100 &; telnet localhost 8000
  80
  84
  83
  ...""")
    sys.exit(1)

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind(('127.0.0.1', port))
s.listen(5)
conn, addr = s.accept()

while True:
    conn.sendall("{}\n".format(random.randint(lowerBound, upperBound)).encode("utf-8"))
    time.sleep(0.5)
