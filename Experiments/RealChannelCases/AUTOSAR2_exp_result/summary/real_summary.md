# Real Channel Summary

| Group | Case ID | Source | Property | M1 | M2 | Valid | Direct Tmean(ms) | Static Q/Sigma/R/T | Dynamic Q/Sigma/R/T | Static Speedup | Dynamic Speedup | Preprocess | Partition |
|---|---|---|---|---|---|---|---:|---|---|---:|---:|---|---|
| AUTOSAR-2 Channel | Experiment2_1 | autosar2.Experiment2_1 | A[] buffer1.count >= 0 | buffer1 | runnable1..3+buffer2+buffer4+schedule | Yes | ROM | 3 / 2 / 1 / 1457 | 3 / 2 / 1 / 1441 | - | - | NONE | minimal repartition around buffer1 after original split timeout |
| AUTOSAR-2 Channel | Experiment2_2 | autosar2.Experiment2_2 | A[] buffer1.count <= buffer1.len | buffer1 | runnable1..3+buffer2+buffer4+schedule | Yes | ROM | 3 / 2 / 1 / 1490 | 3 / 2 / 1 / 1302 | - | - | NONE | minimal repartition around buffer1 after original split timeout |
| AUTOSAR-2 Channel | Experiment2_3 | autosar2.Experiment2_3 | A[] buffer2.count >= 0 | buffer2 | runnable1..3+buffer1+buffer4+rte+schedule | Yes | ROM | 3 / 2 / 1 / 1479 | 3 / 2 / 1 / 1454 | - | - | NONE | minimal repartition around buffer2 after original split timeout |
| AUTOSAR-2 Channel | Experiment2_4 | autosar2.Experiment2_4 | A[] buffer2.count <= buffer2.len | buffer2 | runnable1..3+buffer1+buffer4+rte+schedule | Yes | ROM | 1 / 2 / 0 / 407 | 1 / 2 / 0 / 403 | - | - | NONE | minimal repartition around buffer2 after original split timeout |
