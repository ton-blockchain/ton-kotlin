# TON SDK — Overview

TON SDK is a modular SDK for building applications on The Open Network (TON). It offers first‑class support for Kotlin
and Java on the JVM/Android, and can be consumed from Swift on iOS via Kotlin Multiplatform. It provides the core data
structures (Cells/BOC), TL‑B codecs, crypto primitives, networking stacks (ADNL/RLDP), and high‑level APIs (contracts,
lite client) so you can integrate TON from server, desktop, Android, or iOS with one codebase.

This document explains the SDK at a high level: what it is, how it is organized, and when to use each module. For
hands‑on guides and API details, see the other topics in this documentation set.

## What is TON SDK?

A collection of modular libraries published on Maven Central under the `org.ton.sdk` group. Each module focuses on a
well‑defined layer of the TON stack, from low‑level binary formats up to ready‑to‑use APIs.

### Key features

- Kotlin Multiplatform first: shared code for JVM, Android, and other targets.
- Zero‑dependency core for critical primitives (Cells, BOC, TL‑B).
- TL‑B serialization/deserialization with code‑generated schemas for blockchain objects.
- Networking layers for TON (ADNL, RLDP) and a Lite Client API.
- Contract utilities to work with smart‑contracts from Kotlin.
- Carefully tested with cross‑platform test suites.

## Module map (current)

- ton-sdk-blockchain — Core blockchain primitives such as Address, BlockId, Transaction, Message, etc.
- ton-sdk-crypto — ED25519, SHA, CRC, and related crypto utilities.
- ton-sdk-tl — Type Language (TL) serializer for kotlinx-serialization used in network protocols and more.
- ton-sdk-cell — The fundamental immutable binary structure in TON used by TVM; also includes the BOC (Bag of Cells)
  container format.
- ton-sdk-liteapi-client — LiteAPI (LiteClient) for RPC, requests unindexed data from blockchain nodes via LiteServer.
- ton-sdk-toncenter-client — HTTP RPC client for TonCenter V3 (Indexed) and TonCenter V2 (Unindexed).
- ton-sdk-dict — TVM Dictionary built on top of Cells.
- ton-sdk-bitstring — Small module for BitStrings (used by Cells and for byte-level serialization).
- ton-sdk-bigint — Cross‑platform big integer; on JVM it aliases java.math.BigInteger, with platform implementations
  elsewhere.

Note: Artifact coordinates in Maven Central may use org.ton.kotlin with module names like ton-kotlin-*. Refer to the
README for the latest coordinates and versions.

## Typical use cases

- Indexers and backends that need to parse blocks/transactions and verify proofs.
- Wallets and apps that compose and serialize messages and contracts.
- Services querying blockchain data via the Lite Client or TON Center http client.
- Tooling and middleware that rely on TL‑B codecs and BOC manipulation.

## Installation

Add TON SDK modules from Maven Central. Use the coordinates under the org.ton.sdk group. Keep module versions
aligned (e.g., 0.6.0).

<tabs>
<tab id="maven" title="Maven">
<code-block lang="xml" src="../../examples/java-maven-project/pom.xml" include-symbol="dependencies"/>
</tab>
<tab id="gradle-kts" title="build.gradle.kts">
<code-block lang="kotlin" src="../../examples/build.gradle.kts" include-lines="10-12"/>
</tab>
</tabs>

## Quick start

Add the artifacts you need from Maven Central and start from the level that fits your use case.

<tabs>
<tab id="java" title="Java">
<code-block lang="Java" src="../../examples/java-maven-project/src/main/java/org/ton/sdk/example/GetTransactionExample.java" include-symbol="main"/>
</tab>
<tab id="kotlin" title="Kotlin">
<code-block lang="kotlin" src="../../examples/kotlin-gradle-project/src/jvmMain/kotlin/GetTransactionExample.kt" include-symbol="main"/>
</tab>
</tabs>

See examples/ and tests in the repository for more scenarios.

## Supported platforms

- JVM / Android — Kotlin and Java (first‑class)
- iOS — Swift via Kotlin Multiplatform bindings (availability may vary by module)
- Desktop/Server — JVM (JDK 8+)
- Other Kotlin targets may be available per‑module; check each module’s README or Gradle metadata.

## Where to go next

- Project README: architecture, badges, and links
- Wiki/Docs: deeper guides and API references
- Examples directory: runnable samples
- Telegram chat: community support and announcements
