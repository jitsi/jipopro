#!/usr/bin/env python3

import sys
import json

def decrease_instants(events, n):
    for e in events:
        e['instant'] -= n

if len(sys.argv) != 2:
    print("Usage: {} <metadata-filename}".format(sys.argv[0]))
    sys.exit(1)


events = json.load(open(sys.argv[1]))

first_instant = min(events['audio'][0]['instant'],
                    events['video'][0]['instant'])

decrease_instants(events['audio'], first_instant)
decrease_instants(events['video'], first_instant)

print(json.dumps(events, indent=4, sort_keys=True))

