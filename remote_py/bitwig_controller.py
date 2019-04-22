from random import randint, random
import asyncio
from time import sleep

from py4j.java_gateway import JavaGateway


class BitwigController():
    gateway = JavaGateway()
    e = gateway.entry_point
    host = e.getHost()
    println = host.println
    clip = e.getClip()
    device = e.getCursorDevice()
    track = e.getCursorTrack()
    remote = e.getRemote()
    transport = e.getTransport()
    user_track = e.getFollowCursorTrack()
    track_bank = e.getMainTrackBank()
    scene_bank = track_bank.sceneBank()

    def __init__(self):
        pass

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

    async def listen_for_clips(self):
        pass

    # %%
    def enum_tracks(self):
        track_bank = self.track_bank
        track_bank.scrollPosition().set(0)
        num_track = track_bank.getSizeOfBank()
        t_num = 1
        while True:
            for i in range(num_track):
                t = track_bank.getItemAt(i)
                name = f"{t_num}: {t.name().get()}"
                t.name().set(name)
                t_num += 1
            if not track_bank.canScrollForwards().get():
                break
            track_bank.scrollPageForwards()

    # %%

    def mess_up_tb(self):
        track_bank = self.track_bank

        print("Random stuck notes based on trackbank")
        num_tracks = track_bank.getSizeOfBank()
        track_bank.scrollPosition().set(0)
        while True:
            for i in range(num_tracks):
                note = randint(24, 72)
                t = track_bank.getItemAt(i)
                print(f"Track: {t.name().get()}")
                t.startNote(note, 10)
            if not track_bank.canScrollForwards().get():
                break
            track_bank.scrollPageForwards()

    # %%

    def mess_up(self):
        print("Adding random stuck notes to all tracks")
        track = self.track
        track.selectFirst()
        sleep(0.02)
        while track.hasNext().get():
            print(f"Track: {track.name().get()}")
            for i in range(randint(1, 4)):
                note = randint(24, 72)
                track.startNote(note, 10)
            track.selectNext()
            sleep(0.02)

    # %%
    def mute_all(self):
        track = self.track
        print("Muting all tracks")
        track.selectFirst()
        sleep(0.02)
        while track.hasNext().get():
            print(f"Track: {track.name().get()}")
            for i in range(128):
                track.stopNote(i, 30)
            track.selectNext()
            sleep(0.02)

    # %%
    def randomize_parameters(self):
        remote = self.remote
        print("Randomizing parameters")
        remote_pages = [p for p in remote.pageNames().get()]
        indices = tuple(i for i, r in enumerate(
            remote_pages) if r.startswith("Perform"))
        print(f"Indices of pages that match criteria: {indices}")
        for ind in indices:
            remote.selectedPageIndex().set(ind)
            print(f"{remote_pages[ind]}")
            for rc_ind in range(8):  # range(num_parameters)
                control = remote.getParameter(rc_ind)
                val = random()
                print(f" {val:6.2f} -> {control.name().get()}")
                control.setImmediately(val)

    """
    def make_scale(scale):
        def add_scale():
            print('Adding keys: ')
            print(scale)
            clip.scrollToKey(12 * 4)
            clip.scrollToStep(0)
            # sleep(0.1)
            for n in scale:
                print(n)
                clip.setStep(0, n, 80, 1.0)

        return add_scale
    """


    # %%
    def draw_sequencer(self):
        e = self.e
        clip_content = e.getClipContent()
        w = 1024
        h = 12

        from itertools import product

        output = [" " * w for _ in range(h)]
        for y in range(h):
            temp = list(output[y])

            for x in range(w):
                if clip_content[y][x] == 1:
                    temp[x] = "X"
                elif clip_content[y][x] == 2:
                    temp[x] = "#"
                # print(x,y)
            output[y] = "".join(temp)
        for s in reversed(output):
            print(s)