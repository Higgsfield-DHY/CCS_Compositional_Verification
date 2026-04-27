# Random Channel Summary

## Nontrivial Learning Showcase

| Suite | Case ID | Sigma | Profile | ShowcaseTarget | Mode | Burst | Valid | Direct Tmean(ms) | Static Q/Sigma/R/T | Dynamic Q/Sigma/R/T | Static Speedup | Dynamic Speedup | Preprocess | Property |
|---|---|---:|---|---|---|---:|---|---:|---|---|---:|---:|---|---|
| M4_nontrivial_learning | M4_pipe_q2r1 | 2 | Q2_R1 | Q2_R1 | TWO_MODE | 1 | Yes | 1162 | 4 / 2 / 1 / 2960 | 4 / 2 / 1 / 3120 | 0.39 | 0.37 | NONE | A[] Buf.count <= Buf.len |
| M4_nontrivial_learning | M4_pipe_q3r1 | 4 | Q3_R1 | Q3_R1 | THREE_MODE | 1 | Yes | 155 | 4 / 4 / 1 / 6352 | 4 / 4 / 1 / 6330 | 0.02 | 0.02 | NONE | A[] Buf.count <= Buf.len |
| M4_nontrivial_learning | M4_pipe_q3r2 | 4 | Q3_R2 | Q3_R2 | THREE_MODE | 1 | Yes | 1089 | 4 / 4 / 3 / 22622 | 4 / 4 / 3 / 23023 | 0.05 | 0.05 | NONE | A[] Buf.count <= Buf.len |
| M4_nontrivial_learning | M4_sched_q2r1 | 2 | Q2_R1 | Q2_R1 | TWO_MODE | 1 | Yes | 161 | 4 / 2 / 1 / 3103 | 4 / 2 / 1 / 3241 | 0.05 | 0.05 | NONE | A[] Buf.count <= Buf.len |
| M4_nontrivial_learning | M4_sched_q3r1 | 4 | Q3_R1 | Q3_R1 | THREE_MODE | 1 | Yes | 283 | 4 / 4 / 1 / 6524 | 4 / 4 / 1 / 6506 | 0.04 | 0.04 | NONE | A[] Buf.count <= Buf.len |
| M4_nontrivial_learning | M4_sched_q3r2 | 4 | Q3_R2 | Q3_R2 | THREE_MODE | 1 | Yes | 981 | 4 / 4 / 3 / 17867 | 4 / 4 / 3 / 17353 | 0.05 | 0.06 | NONE | A[] Buf.count <= Buf.len |
| M4_nontrivial_learning | M4_split_q2r1 | 2 | Q2_R1 | Q2_R1 | TWO_MODE | 1 | Yes | 174 | 4 / 2 / 1 / 3207 | 4 / 2 / 1 / 3156 | 0.05 | 0.06 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M4_nontrivial_learning | M4_split_q3r1 | 4 | Q3_R1 | Q3_R1 | THREE_MODE | 1 | Yes | 170 | 4 / 4 / 1 / 6424 | 4 / 4 / 1 / 6722 | 0.03 | 0.03 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M4_nontrivial_learning | M4_split_q3r2 | 4 | Q3_R2 | Q3_R2 | THREE_MODE | 1 | Yes | 1274 | 4 / 4 / 3 / 23308 | 4 / 4 / 3 / 23409 | 0.05 | 0.05 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |

