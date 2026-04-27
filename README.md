# CCS Compositional Verification

This repository contains the implementation and experiment artifacts for my undergraduate thesis on **model-learning-based compositional verification of CCS-style timed systems**.

## Overview

The project studies compositional verification for CCS-style timed systems with binary-channel synchronization. The core idea is to combine:

- assume-guarantee reasoning,
- model learning for automatic assumption construction,
- preprocessing for internal/external channel-action splitting,
- UPPAAL-based membership and candidate queries.

The repository includes both the implementation and the experiment data used in the thesis.

## Repository Structure

- `src/`
  Java source code for the verification framework, learning components, timed automata utilities, UPPAAL integration, and experiment runners.
- `Experiments/`
  Random-case and real-case experiment configurations, results, summaries, and related artifacts.
- `pom.xml`
  Maven project file.

## Main Contents

The implementation covers:

- learning-based assumption construction for one-clock timed automata,
- compositional verification workflows for CCS-style timed systems,
- internal/external channel-action splitting for binary-channel models,
- experiment runners for random benchmarks and real-world case studies such as AUTOSAR, Train-Gate, and Producer-Consumer.

## Environment

The project is developed in Java and uses Maven for build management. The verification backend relies on **UPPAAL** (in particular, `verifyta`) for model-checking tasks.

Typical requirements:

- Java 8
- Maven
- UPPAAL / `verifyta`

## Notes

- This repository is intended to host the thesis-related source code and experiment materials only.
- Temporary files, build outputs, and thesis drafts are intentionally excluded to keep the repository clean.
