# Random Channel Summary

## Main Performance Suites

| Suite | Case ID | Sigma | Profile | Mode | Burst | Valid | Direct Tmean(ms) | Static Q/Sigma/R/T | Dynamic Q/Sigma/R/T | Static Speedup | Dynamic Speedup | Preprocess | Property |
|---|---|---:|---|---|---:|---|---:|---|---|---:|---:|---|---|
| M1_boundary_sched_buffer | M1_s2_base_a | 2 | BASE_A | ONE_MODE | 1 | Yes | 5065 | 1 / 2 / 0 / 444 | 1 / 2 / 0 / 390 | 11.41 | 12.99 | NONE | A[] Buf.count <= Buf.len |
| M1_boundary_sched_buffer | M1_s2_base_b | 2 | BASE_B | ONE_MODE | 1 | Yes | 18771 | 1 / 2 / 0 / 444 | 1 / 2 / 0 / 399 | 42.28 | 47.05 | NONE | A[] Buf.count <= Buf.len |
| M1_boundary_sched_buffer | M1_s2_three | 2 | S2_THREE | THREE_MODE | 1 | Yes | 9926 | 1 / 2 / 0 / 438 | 1 / 2 / 0 / 390 | 22.66 | 25.45 | NONE | A[] Buf.count <= Buf.len |
| M1_boundary_sched_buffer | M1_s2_tight_a | 2 | TIGHT_A | ONE_MODE | 1 | Yes | 37056 | 1 / 2 / 0 / 458 | 1 / 2 / 0 / 431 | 80.91 | 85.98 | NONE | A[] Buf.count <= Buf.len |
| M1_boundary_sched_buffer | M1_s2_tight_b | 2 | TIGHT_B | ONE_MODE | 1 | Yes | 37217 | 1 / 2 / 0 / 438 | 1 / 2 / 0 / 398 | 84.97 | 93.51 | NONE | A[] Buf.count <= Buf.len |
| M1_boundary_sched_buffer | M1_s2_two_a | 2 | TWO_A | TWO_MODE | 1 | Yes | 4380 | 1 / 2 / 0 / 439 | 1 / 2 / 0 / 434 | 9.98 | 10.09 | NONE | A[] Buf.count <= Buf.len |
| M1_boundary_sched_buffer | M1_s2_two_b | 2 | TWO_B | TWO_MODE | 1 | Yes | 18528 | 1 / 2 / 0 / 404 | 1 / 2 / 0 / 421 | 45.86 | 44.01 | NONE | A[] Buf.count <= Buf.len |
| M1_boundary_sched_buffer | M1_s4_three | 4 | S4_THREE | THREE_MODE | 1 | Yes | 38556 | 1 / 4 / 0 / 625 | 1 / 4 / 0 / 622 | 61.69 | 61.99 | NONE | A[] Buf.count <= Buf.len |
| M1_boundary_sched_buffer | M1_s4_two | 4 | S4_TWO | TWO_MODE | 1 | Yes | 4362 | 1 / 4 / 0 / 672 | 1 / 4 / 0 / 694 | 6.49 | 6.29 | NONE | A[] Buf.count <= Buf.len |
| M1_boundary_sched_buffer | M1_s6_stress | 6 | S6_STRESS | THREE_MODE | 1 | Yes | TIMEOUT | 1 / 6 / 0 / 842 | 1 / 6 / 0 / 869 | - | - | NONE | A[] Buf.count <= Buf.len |
| M2_boundary_pipeline_buffer | M2_s2_base_a | 2 | BASE_A | ONE_MODE | 1 | Yes | 3033 | 1 / 2 / 0 / 409 | 1 / 2 / 0 / 384 | 7.42 | 7.90 | NONE | A[] Buf.count <= Buf.len |
| M2_boundary_pipeline_buffer | M2_s2_base_b | 2 | BASE_B | ONE_MODE | 1 | Yes | 3126 | 1 / 2 / 0 / 403 | 1 / 2 / 0 / 391 | 7.76 | 7.99 | NONE | A[] Buf.count <= Buf.len |
| M2_boundary_pipeline_buffer | M2_s2_three | 2 | S2_THREE | THREE_MODE | 1 | Yes | 8133 | 1 / 2 / 0 / 462 | 1 / 2 / 0 / 416 | 17.60 | 19.55 | NONE | A[] Buf.count <= Buf.len |
| M2_boundary_pipeline_buffer | M2_s2_tight_a | 2 | TIGHT_A | ONE_MODE | 1 | Yes | 49716 | 1 / 2 / 0 / 388 | 1 / 2 / 0 / 337 | 128.13 | 147.53 | NONE | A[] Buf.count <= Buf.len |
| M2_boundary_pipeline_buffer | M2_s2_tight_b | 2 | TIGHT_B | ONE_MODE | 1 | Yes | 43325 | 1 / 2 / 0 / 375 | 1 / 2 / 0 / 325 | 115.53 | 133.31 | NONE | A[] Buf.count <= Buf.len |
| M2_boundary_pipeline_buffer | M2_s2_two_a | 2 | TWO_A | TWO_MODE | 1 | Yes | 2598 | 1 / 2 / 0 / 327 | 1 / 2 / 0 / 315 | 7.94 | 8.25 | NONE | A[] Buf.count <= Buf.len |
| M2_boundary_pipeline_buffer | M2_s2_two_b | 2 | TWO_B | TWO_MODE | 1 | Yes | 2586 | 1 / 2 / 0 / 363 | 1 / 2 / 0 / 332 | 7.12 | 7.79 | NONE | A[] Buf.count <= Buf.len |
| M2_boundary_pipeline_buffer | M2_s4_three | 4 | S4_THREE | THREE_MODE | 1 | Yes | 6399 | 1 / 4 / 0 / 557 | 1 / 4 / 0 / 485 | 11.49 | 13.19 | NONE | A[] Buf.count <= Buf.len |
| M2_boundary_pipeline_buffer | M2_s4_two | 4 | S4_TWO | TWO_MODE | 1 | Yes | 2517 | 1 / 4 / 0 / 496 | 1 / 4 / 0 / 475 | 5.07 | 5.30 | NONE | A[] Buf.count <= Buf.len |
| M2_boundary_pipeline_buffer | M2_s6_stress | 6 | S6_STRESS | THREE_MODE | 1 | Yes | TIMEOUT | 1 / 6 / 0 / 658 | 1 / 6 / 0 / 686 | - | - | NONE | A[] Buf.count <= Buf.len |
| M3_boundary_split_pipeline_buffer | M3_s2_base_a | 2 | BASE_A | ONE_MODE | 1 | Yes | 22629 | 1 / 2 / 0 / 327 | 1 / 2 / 0 / 324 | 69.20 | 69.84 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M3_boundary_split_pipeline_buffer | M3_s2_base_b | 2 | BASE_B | ONE_MODE | 1 | Yes | 22482 | 1 / 2 / 0 / 336 | 1 / 2 / 0 / 297 | 66.91 | 75.70 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M3_boundary_split_pipeline_buffer | M3_s2_three | 2 | S2_THREE | THREE_MODE | 1 | Yes | 46408 | 1 / 2 / 0 / 322 | 1 / 2 / 0 / 312 | 144.12 | 148.74 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M3_boundary_split_pipeline_buffer | M3_s2_tight_a | 2 | TIGHT_A | ONE_MODE | 1 | Yes | 4212 | 1 / 2 / 0 / 329 | 1 / 2 / 0 / 313 | 12.80 | 13.46 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M3_boundary_split_pipeline_buffer | M3_s2_tight_b | 2 | TIGHT_B | ONE_MODE | 1 | Yes | 4059 | 1 / 2 / 0 / 315 | 1 / 2 / 0 / 311 | 12.89 | 13.05 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M3_boundary_split_pipeline_buffer | M3_s2_two_a | 2 | TWO_A | TWO_MODE | 1 | Yes | 21571 | 1 / 2 / 0 / 369 | 1 / 2 / 0 / 311 | 58.46 | 69.36 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M3_boundary_split_pipeline_buffer | M3_s2_two_b | 2 | TWO_B | TWO_MODE | 1 | Yes | 21133 | 1 / 2 / 0 / 333 | 1 / 2 / 0 / 302 | 63.46 | 69.98 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M3_boundary_split_pipeline_buffer | M3_s4_three | 4 | S4_THREE | THREE_MODE | 1 | Yes | 6912 | 1 / 4 / 0 / 545 | 1 / 4 / 0 / 544 | 12.68 | 12.71 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M3_boundary_split_pipeline_buffer | M3_s4_two | 4 | S4_TWO | TWO_MODE | 1 | Yes | 2933 | 1 / 4 / 0 / 470 | 1 / 4 / 0 / 471 | 6.24 | 6.23 | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |
| M3_boundary_split_pipeline_buffer | M3_s6_stress | 6 | S6_STRESS | THREE_MODE | 1 | Yes | TIMEOUT | 1 / 6 / 0 / 822 | 1 / 6 / 0 / 806 | - | - | BIDIRECTIONAL_DOMAIN_SPLIT | A[] Buf.count <= Buf.len |

