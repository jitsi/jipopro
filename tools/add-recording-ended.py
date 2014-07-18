#!/usr/bin/env python3

import sys, os, json

def preprocess_video_events(events):
    #remove SPEAKER_CHANGED events from the beginning
    for i in range(len(events)):
        if events[i]['type'] == 'SPEAKER_CHANGED':
            del events[i]
        else:
            break

    events = [i for i in events if i['type'] != 'RECORDING_ENDED']

    ended = []
    for event in events:
        if event['type'] == 'RECORDING_STARTED':
            e = dict(filename=event['filename'],
                     mediaType='video',
                     type='RECORDING_ENDED',
                     ssrc=event['ssrc'],
                     instant=event['instant'] + get_duration(
                         dir + event['filename']))
            ended.append(e)
    events += ended

    events.sort(key=lambda x: x['instant'])
    return events

def get_duration(filename):
    proc = os.popen('mkvinfo -s -v ' + filename + " | tail -n 1 | awk '{print $6;}'")
    return int(proc.read())


if len(sys.argv) != 2:
    print("Usage: {} <metadata-filename}".format(sys.argv[0]))
    sys.exit(1)

dir = os.path.dirname(sys.argv[1]) + '/'
events = json.load(open(sys.argv[1]))
events['video'] = preprocess_video_events(events['video'])
print(json.dumps(events, indent=4, sort_keys=True))

