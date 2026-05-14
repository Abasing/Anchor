# Performance Notes

Anchor keeps runtime metrics lightweight on purpose.

## What is measured

- adapter load time
- hook lookup timing
- scheduler delay and runtime timing
- task callback timing
- task cancellations and failures
- `/anchor doctor` scan timing
- `/anchor` command timing
- runtime validation timing

## What is not measured

- per-player long-term telemetry
- remote analytics
- packet-level timings
- full profiling data

## Reading `/anchor metrics`

Useful signals:

- `adapter.load.*`: startup cost of each bridge
- `scheduler.delay.*`: queue delay before a task starts
- `scheduler.runtime.*`: how long scheduled work actually runs
- `scheduler.callback.*`: callback completion overhead
- `scheduler.failed.*`: task bodies throwing exceptions
- `scheduler.cancelled.*`: cancellation volume
- `doctor.scan`: cost of compatibility and validation scans

## Practical advice

- High scheduler delay usually means the server is busy, not that Anchor itself is slow.
- High scheduler runtime usually means the scheduled work is too heavy.
- Frequent failures usually point at consumer plugin code, not the scheduler abstraction itself.
- Repeating tasks should be tracked and cancelled deliberately.
- Use the stress example before claiming Paper and Folia parity for your plugin stack.
