#!/usr/bin/env python
from webmapCore.attack.attack import Attack
def compare(res1, res2):
    size1 = res1.size + 1
    size2 = res2.size + 1
    delay1 = res1.elapsed_time.total_seconds()
    delay2 = res2.elapsed_time.total_seconds()

    diff = (size1 / delay1) - (size2 / delay2)
    if diff > 0:
        return 1
    elif diff < 0:
        return -1
    else:
        return 0


class mod_delay(Attack):
    """This class gives a top 10 of the webpages taking the most time to respond (compared to their size)"""

    name = "delay"

    doGET = False
    doPOST = False

    def __init__(self, http, xmlRepGenerator):
        Attack.__init__(self, http, xmlRepGenerator)

    def attack(self, urls, forms):
        browsed_resources = urls + forms
        sorted_resources = sorted(browsed_resources, cmp=compare)
        print("Slowest resources found on the web server:")
        for slow_resource in sorted_resources[:10]:
            speed = (slow_resource.size + 1) / slow_resource.elapsed_time.total_seconds()
            print("With a download speed of {0} bps:".format(speed))
            print slow_resource