# Supporting Real-Case Summary

| Group | Case ID | Source | Property | M1 | M2 | Valid | Direct Tmean(ms) | Static Q/Sigma/R/T | Dynamic Q/Sigma/R/T | Static Speedup | Dynamic Speedup | Preprocess | Partition |
|---|---|---|---|---|---|---|---:|---|---|---:|---:|---|---|
| PC Channel | PC_2 | pc.PcP1C3K2Source | A[] Buffer.count >= 0 && Buffer.count <= Buffer.len | Buffer+Producer_1 | Consumer_1+Consumer_2+Consumer_3 | Yes | 100 | 1 / 1 / 0 / 290 | 1 / 1 / 0 / 274 | 0.34 | 0.36 | NONE | producer internal to M1 |
| PC Channel | PC_3 | pc.PcP1C3K2Source | A[] Buffer.count >= 0 && Buffer.count <= Buffer.len | Buffer+Consumer_1+Consumer_2+Consumer_3 | Producer_1 | Yes | 91 | 1 / 1 / 0 / 283 | 1 / 1 / 0 / 267 | 0.32 | 0.34 | NONE | all consumers internal to M1 |
| Fischer Channel | Fischer2_1 | fischer.FischerTa3Source | A[] Mutex.hold <= 1 | Mutex | Process_1+Process_2+Global_Var | Yes | 438 | 3 / 4 / 1 / 2506 | 3 / 4 / 1 / 2339 | 0.17 | 0.19 | NONE | monitor-only partition |
| Fischer Channel | Fischer2_2 | fischer.FischerTa3Source | A[] Mutex.hold <= 1 | Mutex+Process_1 | Process_2+Global_Var | Yes | 85 | 1 / 5 / 0 / 614 | 1 / 5 / 0 / 635 | 0.14 | 0.13 | NONE | Process_1 internal to M1 |
| Fischer Channel | Fischer3_1 | fischer.FischerTa4Source | A[] Mutex.hold <= 1 | Mutex | Process_1+Process_2+Process_3+Global_Var | Yes | 92 | 3 / 6 / 1 / 3379 | 3 / 6 / 1 / 3396 | 0.03 | 0.03 | NONE | monitor-only partition |
| Fischer Channel | Fischer3_2 | fischer.FischerTa4Source | A[] Mutex.hold <= 1 | Mutex+Process_1 | Process_2+Process_3+Global_Var | Yes | 87 | 1 / 5 / 0 / 601 | 1 / 5 / 0 / 622 | 0.14 | 0.14 | NONE | Process_1 internal to M1 |
