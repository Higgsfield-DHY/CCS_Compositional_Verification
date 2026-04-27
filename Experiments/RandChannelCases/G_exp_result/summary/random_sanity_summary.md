# Random Channel Summary

## Sanity Suites

| Suite | Case ID | Valid | Direct Tmean(ms) | Static Q/Sigma/R/T | Dynamic Q/Sigma/R/T | Static Speedup | Dynamic Speedup | Preprocess | Property |
|---|---|---|---:|---|---|---:|---:|---|---|
| G1_clean_small | G1_00 | Yes | 725 | 1 / 2 / 0 / 289 | 1 / 2 / 0 / 268 | 2.51 | 2.71 | NONE | A[] Buf.count >= 0 |
| G1_clean_small | G1_01 | No | 66 | 2 / 2 / 1 / 527 | 2 / 2 / 1 / 522 | 0.13 | 0.13 | NONE | A[] Buf.count >= 0 |
| G1_clean_small | G1_02 | Yes | 67 | 1 / 2 / 0 / 262 | 1 / 2 / 0 / 275 | 0.26 | 0.24 | NONE | A[] Buf.count >= 0 |
| G1_clean_small | G1_03 | No | 69 | 2 / 2 / 1 / 541 | 2 / 2 / 1 / 569 | 0.13 | 0.12 | NONE | A[] Buf.count >= 0 |
| G1_clean_small | G1_04 | Yes | 70 | 1 / 2 / 0 / 279 | 1 / 2 / 0 / 274 | 0.25 | 0.26 | NONE | A[] Buf.count <= Buf.len |
| G1_clean_small | G1_05 | No | 67 | 3 / 2 / 2 / 1110 | 3 / 2 / 2 / 1097 | 0.06 | 0.06 | NONE | A[] Buf.count <= Buf.len |
| G1_clean_small | G1_06 | Yes | 70 | 1 / 2 / 0 / 313 | 1 / 2 / 0 / 268 | 0.22 | 0.26 | NONE | A[] Buf.count <= Buf.len |
| G1_clean_small | G1_07 | No | 67 | 3 / 2 / 2 / 1161 | 3 / 2 / 2 / 1129 | 0.06 | 0.06 | NONE | A[] Buf.count <= Buf.len |
| G1_clean_small | G1_08 | Yes | 68 | 1 / 4 / 0 / 411 | 1 / 4 / 0 / 399 | 0.17 | 0.17 | NONE | A[] Mutex.hold <= 1 |
| G1_clean_small | G1_09 | No | 68 | 3 / 4 / 2 / 1985 | 3 / 4 / 2 / 2047 | 0.03 | 0.03 | NONE | A[] Mutex.hold <= 1 |
| G1_clean_small | G1_10 | Yes | 73 | 1 / 4 / 0 / 534 | 1 / 4 / 0 / 525 | 0.14 | 0.14 | NONE | A[] Mutex.hold <= 1 |
| G1_clean_small | G1_11 | No | 78 | 3 / 4 / 2 / 2631 | 3 / 4 / 2 / 2327 | 0.03 | 0.03 | NONE | A[] Mutex.hold <= 1 |
| G2_clean_multi | G2_00 | Yes | 109 | 1 / 2 / 0 / 309 | 1 / 2 / 0 / 347 | 0.35 | 0.31 | NONE | A[] Buf.count >= 0 |
| G2_clean_multi | G2_01 | No | 79 | 2 / 2 / 1 / 644 | 2 / 2 / 1 / 556 | 0.12 | 0.14 | NONE | A[] Buf.count >= 0 |
| G2_clean_multi | G2_02 | Yes | 277 | 1 / 2 / 0 / 265 | 1 / 2 / 0 / 260 | 1.05 | 1.07 | NONE | A[] Buf.count >= 0 |
| G2_clean_multi | G2_03 | No | 66 | 2 / 2 / 1 / 585 | 2 / 2 / 1 / 552 | 0.11 | 0.12 | NONE | A[] Buf.count >= 0 |
| G2_clean_multi | G2_04 | Yes | 69 | 1 / 2 / 0 / 280 | 1 / 2 / 0 / 280 | 0.25 | 0.25 | NONE | A[] Buf.count <= Buf.len |
| G2_clean_multi | G2_05 | No | 72 | 4 / 2 / 2 / 1864 | 4 / 2 / 2 / 1957 | 0.04 | 0.04 | NONE | A[] Buf.count <= Buf.len |
| G2_clean_multi | G2_06 | Yes | 70 | 1 / 2 / 0 / 266 | 1 / 2 / 0 / 271 | 0.26 | 0.26 | NONE | A[] Buf.count <= Buf.len |
| G2_clean_multi | G2_07 | No | 68 | 4 / 2 / 2 / 1838 | 4 / 2 / 2 / 1864 | 0.04 | 0.04 | NONE | A[] Buf.count <= Buf.len |
| G2_clean_multi | G2_08 | Yes | 69 | 1 / 8 / 0 / 690 | 1 / 8 / 0 / 694 | 0.10 | 0.10 | NONE | A[] Mutex.hold <= 1 |
| G2_clean_multi | G2_09 | No | 76 | 3 / 8 / 2 / 3674 | 3 / 8 / 2 / 3762 | 0.02 | 0.02 | NONE | A[] Mutex.hold <= 1 |
| G2_clean_multi | G2_10 | Yes | 70 | 1 / 8 / 0 / 733 | 1 / 8 / 0 / 733 | 0.10 | 0.10 | NONE | A[] Mutex.hold <= 1 |
| G2_clean_multi | G2_11 | No | 74 | 3 / 8 / 2 / 3983 | 3 / 8 / 2 / 3837 | 0.02 | 0.02 | NONE | A[] Mutex.hold <= 1 |
| G3_split_required | G3_00 | Yes | 69 | 1 / 2 / 0 / 277 | 1 / 2 / 0 / 278 | 0.25 | 0.25 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count >= 0 |
| G3_split_required | G3_01 | No | 70 | 2 / 2 / 1 / 552 | 2 / 2 / 1 / 547 | 0.13 | 0.13 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count >= 0 |
| G3_split_required | G3_02 | Yes | 69 | 1 / 2 / 0 / 313 | 1 / 2 / 0 / 282 | 0.22 | 0.24 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count >= 0 |
| G3_split_required | G3_03 | No | 70 | 2 / 2 / 1 / 551 | 2 / 2 / 1 / 559 | 0.13 | 0.13 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count >= 0 |
| G3_split_required | G3_04 | Yes | 73 | 1 / 2 / 0 / 268 | 1 / 2 / 0 / 276 | 0.27 | 0.26 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count >= 0 |
| G3_split_required | G3_05 | No | 69 | 2 / 2 / 1 / 572 | 2 / 2 / 1 / 555 | 0.12 | 0.12 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count >= 0 |
| G3_split_required | G3_06 | Yes | 82 | 1 / 2 / 0 / 290 | 1 / 2 / 0 / 280 | 0.28 | 0.29 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| G3_split_required | G3_07 | No | 66 | 3 / 2 / 2 / 1154 | 3 / 2 / 2 / 1165 | 0.06 | 0.06 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| G3_split_required | G3_08 | Yes | 97 | 1 / 2 / 0 / 286 | 1 / 2 / 0 / 282 | 0.34 | 0.34 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| G3_split_required | G3_09 | No | 66 | 3 / 2 / 2 / 1122 | 3 / 2 / 2 / 1136 | 0.06 | 0.06 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| G3_split_required | G3_10 | Yes | 68 | 1 / 2 / 0 / 280 | 1 / 2 / 0 / 305 | 0.24 | 0.22 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| G3_split_required | G3_11 | No | 64 | 3 / 2 / 2 / 1105 | 3 / 2 / 2 / 1100 | 0.06 | 0.06 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |

