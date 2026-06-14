# ProRF DSL v1

ProRF DSL is the source format for Engineering Workflow Graphs. It is declarative,
typed, graph-first, and compiles to the existing `WorkflowGraph` platform IR.

Supported v1 blocks:

- `imports`: domain pack identifiers such as `rf.std.v1`
- `variables`: typed global values such as `f0 = 2.4 GHz`
- `nodes`: typed node instances with schema-checked parameters
- `edges`: explicit directed port connections
- `scenarios`: named parameter override layers
- `outputs`: declarative export contract

The module intentionally does not execute workflows. Execution remains in the
platform execution engine.

Runnable examples live in:

- `examples/valid/*.prorf`
- `examples/invalid/*.prorf`

The test suite compiles every valid example and checks every invalid example for
the expected diagnostic.

Example:

```prorf
workflow "RF_Link_Budget_v1" {
    imports {
        rf.std.v1
    }

    variables {
        f0 = 2.4 GHz
    }

    nodes {
        tx: Transmitter {
            power = 20 dBm
            frequency = f0
        }

        ch: FreeSpacePath {
            distance = 3 km
            frequency = f0
        }

        rx: Receiver {
            noiseFigure = 2 dB
        }
    }

    edges {
        tx.output.power -> ch.input.power
        ch.output.power -> rx.input.power
    }

    outputs {
        rx.snr
        rx.link_margin
    }
}
```
