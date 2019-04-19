from random import randint, random
from time import sleep

from py4j.java_gateway import JavaGateway

gateway = JavaGateway()
e = gateway.entry_point
host = e.getHost()

# %%
help_str = """
    Type print(\"help_str\") to print this again

    Functions:
      println       print to bitwig console
      mess_up       add random stuck notes on all tracks
      mute_all      mute every midi note on every track
      minor_scale   add a minor scale to the currently selected clip
      major_scale   add a major scale to the currently selected clip

    Keyboard shortcuts:
      Page Down     mess_up
      Page Up       mute_all
      Shift+Page Up major_scale
      Shift+Page Dn minor_scale
      Shift+End     randomize_parameters

    Variables:
      transport     transport controller
      clip          clip cursor
      device        device cursor 
      track         track cursor 
      remote        remote parameter controls 
      host          bitwig controller host 
      e             entry_point - contains everything you added in the constructor to GatewayServer on
                                  the Java side. In the example, it points to the main class of the controller
                                  script.
"""
print(help_str)

println = host.println
clip = e.getClip()
device = e.getCursorDevice()
track = e.getCursorTrack()
remote = e.getRemote()
transport = e.getTransport()


# %%


def mess_up():
    print("Adding random stuck notes to all tracks")
    track.selectFirst()
    sleep(0.1)
    while track.hasNext().get():
        print(f"Track: {track.name().get()}")
        for i in range(randint(1, 4)):
            note = randint(24, 72)
            track.startNote(note, 10)
        track.selectNext()
        sleep(0.2)


def mute_all():
    print("Muting all tracks")
    track.selectFirst()
    sleep(0.1)
    while track.hasNext().get():
        print(f"Track: {track.name().get()}")
        for i in range(128):
            track.stopNote(i, 30)
        track.selectNext()
        sleep(0.2)


def randomize_parameters():
    print("Randomizing parameters")
    remote_pages = [p for p in remote.pageNames().get()]
    indices = tuple(i for i, r in enumerate(
        remote_pages) if r.startswith("Perform"))
    print(f"Indices of pages that match criteria: {indices}")
    for ind in indices:
        remote.selectedPageIndex().set(ind)
        print(f"{remote_pages[ind]}")
        for rc_ind in range(8): # range(num_parameters)
            control = remote.getParameter(rc_ind)
            val = random()
            print(f" {val:6.2f} -> {control.name().get()}")
            control.setImmediately(val)


def make_scale(scale):
    def add_scale():
        print('Adding keys: ')
        print(scale)
        clip.scrollToKey(12 * 4)
        clip.scrollToStep(0)
        #sleep(0.1)
        for n in scale:
            print(n)
            clip.setStep(0, n, 80, 1.0)

    return add_scale

major_scale = make_scale([0, 2, 4, 5, 7, 9, 11])
minor_scale = make_scale([0, 2, 3, 5, 7, 8, 11])
