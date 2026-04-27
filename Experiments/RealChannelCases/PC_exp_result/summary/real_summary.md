# Real Channel Summary

| Group | Case ID | Source | Property | M1 | M2 | Valid | Direct Tmean(ms) | Static Q/Sigma/R/T | Dynamic Q/Sigma/R/T | Static Speedup | Dynamic Speedup | Preprocess | Partition |
|---|---|---|---|---|---|---|---:|---|---|---:|---:|---|---|
| PC Channel | PC_1 | pc.PcP1C3K2Source | A[] Buffer.count >= 0 && Buffer.count <= Buffer.len | Buffer | Producer_1+Consumer_1+Consumer_2+Consumer_3 | Yes | 434 | 1 / 2 / 0 / 375 | 1 / 2 / 0 / 373 | 1.16 | 1.16 | NONE | binary-channel adaptation |
| PC Channel | PC_2 | pc.PcP1C3K2Source | A[] Buffer.count >= 0 && Buffer.count <= Buffer.len | Buffer+Producer_1 | Consumer_1+Consumer_2+Consumer_3 | Yes | 100 | 1 / 1 / 0 / 290 | 1 / 1 / 0 / 274 | 0.34 | 0.36 | NONE | producer internal to M1 |
| PC Channel | PC_3 | pc.PcP1C3K2Source | A[] Buffer.count >= 0 && Buffer.count <= Buffer.len | Buffer+Consumer_1+Consumer_2+Consumer_3 | Producer_1 | Yes | 91 | 1 / 1 / 0 / 283 | 1 / 1 / 0 / 267 | 0.32 | 0.34 | NONE | all consumers internal to M1 |
