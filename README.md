# SMS Network Simulator

A multithreaded Swing application that simulates how an SMS travels from a
sending phone, through the layers of a mobile network, to a receiving phone.

Every device and every station is a separate thread. Messages are encoded as
real PDU structures following the GSM 03.38 and 03.40 standards, passed
between layers as hexadecimal strings, and decoded again at the far end to
find out who the recipient is.

Built for the GUI course at PJATK (project III), using the standard Java
library only — no external dependencies.

## Running

Requires JDK 16 or newer (the code uses `var` and `Stream.toList()`).

```bash
javac -d out $(find . -name "*.java")
java -cp out App
```

On Windows, without a POSIX shell:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName)
java -cp out App
```

The `out/` directory only holds compiled classes and can be deleted at any
time.

When the window is closed, the application writes `VBDData.bin` next to it —
see [Output file](#output-file).

## Using the application

The window is split into three parts.

**Left — sending devices.** "Add" asks for a text and creates a virtual
sending device (VBD) that immediately starts sending that text, over and over,
to a randomly chosen receiver. Each device has:

- a slider setting the pause between messages (100–500 ms, 300 by default);
- its phone number, assigned automatically and not editable;
- a state selector: `Active` sends, `Waiting` pauses the device without
  destroying it;
- "Terminate", which stops the thread and removes the device.

**Right — receiving devices.** "Add" creates a virtual receiving device (VRD)
with the next free number. Each device shows how many messages it has
received, and a check box that makes it reset that counter every 10 seconds.
"Terminate" removes the device.

Messages are only sent while at least one receiver exists; until then the
senders print a reminder to the console.

**Middle — the network.** A column of BTS stations on each side, and one or
more columns of BSC controllers between them. "Add" and "Remove" change the
number of BSC layers; at least one always stays. Each station shows its
number, how many messages it has passed on, and how many are still waiting in
its queue.

Stations are not created by hand. A layer starts empty and grows on demand:
each message goes to the station with the shortest queue, and when every
station already holds 5 messages, the layer adds a new one.

## How a message travels

```
VBD ──► BTS ──► BSC ──► ... ──► BSC ──► BTS ──► VRD
        left    first          last    right
              └── BSC layers, added and removed by the user ──┘
```

1. A VBD thread picks a random recipient, encodes the text as a PDU and emits
   it as a hexadecimal string.
2. Each station holds the message for a while before passing it on: a BTS for
   exactly 3 seconds, a BSC for a random 5 to 14 seconds. A station thread
   wakes up ten times per second and releases everything whose waiting time is
   over.
3. At the end the receiving layer decodes the recipient number out of the PDU
   and delivers the message. If no device carries that number any more — it
   was terminated while the message was in flight — the loss is reported in
   the console instead.

Removing a BSC layer does not destroy the messages inside it: the layer stops
accepting new ones and flushes everything it still holds to the next layer at
once, ignoring the timers.

## Architecture

The model knows nothing about Swing. Devices and layers only fire events, and
the panels listen to them — so a panel appears or disappears because the model
said so, never the other way round.

```
Device ──fires──► DeviceListener ──heard by──► DeviceLayer
                                                    │
                                                  fires
                                                    ▼
                                          DeviceLayerListener
                                           ┌────────┴────────┐
                                    next layer          the UI panel
```

### Packages

| Package | Contents |
| --- | --- |
| `Common` | The base classes every part of the network is built on: `Device`, `Station`, `DeviceLayer`, `StationLayer`, and the listener and event types that connect them. |
| `Models` | The concrete network: `VBD` and `VRD` devices, `BTS` and `BSC` stations, their layers, and `BSCLayers` — the chain of intermediate layers the user can grow and shrink. |
| `SMS` | `PDU` encodes and decodes messages according to the standard; `HEX` converts those bytes to the hexadecimal strings the layers pass around. |
| `Presentation` | The Swing window and one panel type per model type. `MainFrame` also wires the whole network together in its constructor. |

### Threads

The project uses plain `Thread` and `Runnable` only — no `Timer`, no
`Executor`, as the assignment requires.

- **VBD** — one thread per sending device. It loops: send a message, sleep for
  the frequency set on the slider.
- **VRD** — one thread per receiving device, used only to clear the received
  counter every 10 seconds when the check box is ticked.
- **Station** — one thread per BTS and per BSC. It loops ten times per second,
  releasing the messages whose waiting time has passed.

Each station queue and each received counter is guarded by a `ReentrantLock`,
because the owning thread and the threads delivering messages touch them at
the same time.

## Message format

Messages are built as SMS-DELIVER PDUs. Phone numbers are stored two digits
per byte with the digits of each pair swapped, and the text is packed with the
GSM 7-bit alphabet, so that eight characters fit into seven bytes. The full
structure, as produced by `PDU.createSMS`:

| Field | Size | Meaning |
| --- | --- | --- |
| SMSC | 8 B | length, number type, and the sender's number |
| First octet | 1 B | message type and flags |
| TP-OA | 8 B | length, number type, and the recipient's number |
| TP-PID | 1 B | protocol identifier |
| TP-DSC | 1 B | data coding scheme — default alphabet here |
| TP-SCTS | 7 B | timestamp with time zone |
| TP-UDL | 1 B | text length in characters |
| TP-UD | variable | the text, packed 7 bits per character |

`PDU.getToNumberFromSMS` reads the recipient back out of TP-OA. The fields
have no fixed offsets, so each length is used to find the next field.

Characters outside the GSM 7-bit alphabet are skipped during encoding.

## Output file

Closing the window writes `VBDData.bin`, a binary record of every sending
device that was still alive. For each device, in order:

| Bytes | Content |
| --- | --- |
| 6 | the device number, encoded as in a PDU |
| 4 | how many messages it sent, as a big-endian `int` |
| variable | the text it was sending, packed 7 bits per character |

## Project layout

```
App.java                          entry point, starts the window

Common/                           the base types the network is built on
  Device.java                     anything owning a phone number
  DeviceListener.java             events of a single device
  BaseDeviceListener.java         empty listener, for overriding one event
  DeviceLayer.java                a group of devices of one kind
  DeviceLayerListener.java        events of a layer
  BaseDeviceLayerListener.java    empty layer listener
  DeviceLayerEvent.java           carries the device that was added or removed
  SMSEvent.java                   carries one encoded message
  Station.java                    a station with its queue and its own thread
  StationLayer.java               a column of stations, balances the load

Models/                           the concrete network
  VBD.java                        sending device, one thread each
  VBDLayer.java                   all senders, writes the report on exit
  VBDStateEnum.java               Active or Waiting
  VRD.java                        receiving device, counts what arrives
  VRDLayer.java                   all receivers, delivers by number
  VRDNumberProvider.java          lets a sender ask for a recipient
  BTS.java                        base station, holds a message 3 s
  BTSLayer.java                   a column of BTS stations
  BSC.java                        controller, holds a message 5-14 s
  BSCLayer.java                   a column of BSC stations, knows its neighbours
  BSCLayers.java                  the chain of BSC layers the user changes
  BSCLayersListener.java          events of that chain
  BSCLayersEvent.java             carries the layer that was added or removed

SMS/                              the message format
  PDU.java                        builds and reads SMS-DELIVER structures
  HEX.java                        bytes to hexadecimal text and back

Presentation/                     the user interface
  MainFrame.java                  the window, and where the network is wired up
  VBDLayerPanel.java              left panel, the list of senders
  VBDPanel.java                   one sender: slider, number, state, terminate
  VRDLayerPanel.java              right panel, the list of receivers
  VRDPanel.java                   one receiver: number, counter, terminate
  BSCLayersPanel.java             middle panel, all BSC layers side by side
  StationLayerPanel.java          one column of stations
  StationPanel.java               one station: number, processed, waiting
```
