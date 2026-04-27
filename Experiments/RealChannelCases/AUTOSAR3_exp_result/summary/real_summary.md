# Real Channel Summary

| Group | Case ID | Source | Property | M1 | M2 | Valid | Direct Tmean(ms) | Static Q/Sigma/R/T | Dynamic Q/Sigma/R/T | Static Speedup | Dynamic Speedup | Preprocess | Partition |
|---|---|---|---|---|---|---|---:|---|---|---:|---:|---|---|
| AUTOSAR-3 Channel | Experiment3_1 | autosar3.Experiment3_1 | A[] buffer1.count >= 0 | runnable1..7+buffer1..3+task1..3 | schedule | Yes | 108 | 2 / 3 / 1 / 1834 | 2 / 3 / 1 / 1822 | 0.06 | 0.06 | NONE | original split |
| AUTOSAR-3 Channel | Experiment3_2 | autosar3.Experiment3_2 | A[] buffer2.count >= 0 | buffer2 | runnable1..7+buffer1+buffer3+task1..3+schedule | Yes | 179 | 3 / 2 / 1 / 1658 | 3 / 2 / 1 / 1688 | 0.11 | 0.11 | NONE | minimal repartition around buffer2 after original split incompatibility |
| AUTOSAR-3 Channel | Experiment3_3 | autosar3.Experiment3_3 | A[] buffer3.count >= 0 | buffer3 | runnable1..7+buffer1..2+task1..3+schedule | Yes | 92 | 5 / 2 / 3 / 7440 | 5 / 2 / 3 / 7261 | 0.01 | 0.01 | NONE | minimal repartition around buffer3 after original split incompatibility |
