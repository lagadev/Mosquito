"""Generates the original sound effects and music loop used by Mosquito Hunter.
Run from the project root:  python3 tools/generate_sounds.py
All audio is synthesized here, so there are no licensing concerns."""
import math, os, random, struct, wave

RATE = 22050
HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "app", "src", "main", "res", "raw")
os.makedirs(OUT, exist_ok=True)
random.seed(3)


def tone(freq, dur, vol=0.5, slide=0.0, decay=3.5, shape="sine"):
    n = int(RATE * dur)
    out, ph = [], 0.0
    for i in range(n):
        t = i / n
        ph += 2 * math.pi * (freq + slide * t) / RATE
        env = min(1.0, i / 120.0) * math.exp(-decay * t)
        s = math.sin(ph) if shape == "sine" else (2 * ((ph / (2 * math.pi)) % 1.0) - 1)
        out.append(s * env * vol)
    return out


def noise(dur, vol=0.4, decay=10.0):
    n = int(RATE * dur)
    return [(random.random() * 2 - 1) * math.exp(-decay * i / n) * vol for i in range(n)]


def mix(*tracks):
    length = max(len(t) for t in tracks)
    out = [0.0] * length
    for t in tracks:
        for i, s in enumerate(t):
            out[i] += s
    return out


def seq(freqs, step, vol=0.4):
    out = []
    for f in freqs:
        out += tone(f, step, vol, decay=4.0)
    return out


def write(name, samples):
    path = os.path.join(OUT, name + ".wav")
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        frames = bytearray()
        for s in samples:
            s = max(-1.0, min(1.0, s))
            frames += struct.pack("<h", int(s * 30000))
        w.writeframes(bytes(frames))
    print("wrote", path)


write("shoot", mix(tone(1500, 0.16, 0.35, slide=-1100, decay=6), noise(0.06, 0.25, 30)))
write("hit", mix(noise(0.14, 0.6, 18), tone(190, 0.2, 0.5, slide=-110, decay=7)))
write("combo", seq([659, 880, 1047, 1319], 0.07, 0.3))
write("powerup", seq([523, 659, 784, 1046, 1318], 0.075, 0.3))
write("click", tone(900, 0.05, 0.35, decay=12))
write("gameover", seq([392, 330, 262, 196], 0.28, 0.4))

# Buzz: exactly one second so it loops seamlessly (integer cycles per second).
buzz = []
for i in range(RATE):
    t = i / RATE
    s = 0.22 * math.sin(2 * math.pi * 240 * t) * (0.6 + 0.4 * math.sin(2 * math.pi * 12 * t))
    s += 0.08 * math.sin(2 * math.pi * 480 * t)
    buzz.append(s * 0.9)
write("buzz", buzz)

# Music: eight-second ambient loop (Am - F - C - G) with a light arpeggio.
music = [0.0] * (RATE * 8)
chords = [(220.0, 261.63, 329.63), (174.61, 220.0, 261.63),
          (261.63, 329.63, 392.0), (196.0, 246.94, 293.66)]
for ci, ch in enumerate(chords):
    start = ci * 2 * RATE
    for k in range(2 * RATE):
        t = k / RATE
        env = math.sin(math.pi * k / (2 * RATE))
        music[start + k] += sum(math.sin(2 * math.pi * f * t) for f in ch) / 3 * 0.10 * env
for step in range(32):
    ch = chords[step // 8]
    note = ch[step % 3] * 2
    start = step * RATE // 4
    for i, s in enumerate(tone(note, 0.22, 0.07, decay=8)):
        if start + i < len(music):
            music[start + i] += s
write("music", music)
