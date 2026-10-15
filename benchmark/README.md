# Aura-Transcribe: Nemotron 3.5 ASR GGUF Quantization Benchmark Suite

A reproducible multilingual benchmark suite for comparing official GGUF quantization variants of **NVIDIA Nemotron 3.5 ASR Streaming 0.6B** (`handy-computer/nemotron-3.5-asr-streaming-0.6b-gguf`) within the `Aura-Transcribe` ecosystem.

---

## 1. Scope & Evaluated Quantizations

The suite evaluates the official Handy-Computer quantizations against the reference production baseline:

| Quantization | Role | Size (MiB) | Size Savings | Verified SHA-256 (LFS OID) |
|---|---|---|---|---|
| **`Q8_0`** | **Production Baseline** | 716.30 | 0.00% | `b94545b313b3223fda7b2857a52681da813935c2127643d1e9ff0c23d988089c` |
| **`Q6_K`** | Candidate | 592.57 | -17.27% | `4ff802c6207c4a7df23242003fd2aa849a1ab02bba6bc80c3db02e7e82606c28` |
| **`Q5_K_M`** | Candidate | 533.72 | -25.49% | `86429e8c4f7fdcf9b3312269ad1ca6669478ba7805331c4aea7a2e33e9910d65` |
| **`Q4_K_M`** | Candidate | 472.86 | -33.99% | `41c99fa5fb6f3d35f68e79adc3e755eca2232a8d921178bd647b71194792b8fd` |

- **Base Model**: `nvidia/nemotron-3.5-asr-streaming-0.6b` (Conformer-RNNT with cache-aware chunk streaming).
- **License**: `openmdw-1.1` ([OpenMDW License](https://openmdw.ai/license/1-1/)).
- **Hardware Target**: ARM64 Android with ARMv8.2-A `dotprod` + `fp16` NEON extensions.

---

## 2. Multilingual Evaluation Corpus

The test corpus contains **20 clips** across 4 target languages (5 clips each), sourced from **Google FLEURS** (`google/fleurs`, CC-BY 4.0):
- **English (`en`)**: 5 clips, 27–31s duration. Everyday conversational, scientific, and financial queries.
- **Spanish (`es`)**: 5 clips, 27–32s duration. Everyday travel, weather advisory, and cultural queries.
- **French (`fr`)**: 5 clips, 27–31s duration. Elisions (`l'`, `d'`), travel, and administrative news.
- **Portuguese (`pt`)**: 5 clips, 28–31s duration. Nasals (`ã`, `õ`, `ç`), weather forecasts, and business.

All clips are sampled at **16 kHz mono 16-bit PCM WAV** (`pcm_s16le`). Manifest: [`benchmark/manifests/corpus.json`](manifests/corpus.json).

---

## 3. Architecture & Execution Modes

The runner (`scripts/benchmark_asr.py`) supports three execution backends:
1. `--backend synthetic`: Deterministic mock backend reflecting empirical Nemotron 3.5 ASR quantization properties. Runs in < 4 seconds with zero network access in standard CI environments.
2. `--backend adb`: Headless on-device runner driving the installed Aura-Transcribe application on an attached Android device via `adb`.
3. `--backend native`: Direct invocation of local ARM64 C++/Rust binaries.

### Evaluated Inference Pipelines
- **Streaming Mode (Primary)**: Simulates real-time 80 ms audio chunk feeds matching Aura-Transcribe's `StreamCmd::Audio` pump and `att_context_right: 13`. Measures **Time-to-First-Partial (TTFP)**, partial hypothesis count, and stability.
- **Offline Mode**: Whole-file transcription with quiet-point splitting (`SPLIT_SEARCH_SAMPLES` and `MAX_RUN_SAMPLES`), measuring throughput and final WER.

### Dual-Track Scoring
- **Standardized Normalized**: Unicode NFC, lowercase, stripped punctuation, preserved diacritics, numbers, and contractions.
- **Raw Verbatim**: Direct case- and punctuation-sensitive matching.

---

## 4. Usage Instructions

### A. Run Automated Unit & Smoke Tests (CI Safe)
```bash
python3 scripts/test_benchmark_suite.py
```

### B. Prepare / Validate Evaluation Audio
```bash
# Generate deterministic local fixtures for offline testing
python3 scripts/prepare_corpus.py --generate-synthetic

# Verify existing corpus audio format and duration specifications
python3 scripts/prepare_corpus.py --verify
```

### C. Run the Benchmark Suite

#### 1. Deterministic Synthetic Benchmark (CI / Local Host)
```bash
python3 scripts/benchmark_asr.py \
  --backend synthetic \
  --profile balanced \
  --output-dir benchmark/results
```

#### 2. Full Evaluation on Connected Android Device
```bash
python3 scripts/benchmark_asr.py \
  --backend adb \
  --profile balanced \
  --modes streaming offline \
  --repetitions 3 \
  --warmup-runs 1 \
  --randomize-order \
  --output-dir benchmark/results
```

#### 3. CLI Options
- `--quantizations`: Subset of quantizations to run (e.g. `--quantizations Q8_0 Q5_K_M`).
- `--languages`: Subset of languages (e.g. `--languages es en`).
- `--clips`: Subset of clip IDs (e.g. `--clips fleurs_es_01 fleurs_en_01`).
- `--modes`: Inference modes to evaluate (`streaming`, `offline`).
- `--repetitions`: Number of evaluation repetitions per clip.
- `--warmup-runs`: Number of warmup runs prior to measurement.
- `--randomize-order`: Randomizes execution order of models to eliminate thermal throttling bias.
- `--download-models`: Downloads missing models from Hugging Face if not already cached and verified locally.

### D. Acceptance Threshold Profiles
- `balanced` (default): Maximum allowable relative WER increase vs `Q8_0` $\le 8.0\%$, Real-Time Factor $\text{RTF} < 0.5$.
- `strict`: Maximum allowable relative WER increase $\le 3.0\%$, $\text{RTF} < 0.5$.
- `edge`: Maximum allowable relative WER increase $\le 15.0\%$, prioritizing RAM and file size reduction.

---

## 5. Interpreting Results
The runner generates three timestamped artifacts in `benchmark/results/`:
- `benchmark_results_<timestamp>.json`: Complete machine-readable telemetry and per-clip records.
- `benchmark_results_<timestamp>.csv`: Summary table for spreadsheet analysis.
- `benchmark_report_<timestamp>.md`: GitHub-flavored Markdown report including:
  - Executive summary table.
  - Per-language WER breakdown.
  - Pareto efficiency frontier (identifying non-dominated variants).
  - Explicit data-driven quantization recommendation.
