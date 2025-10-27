# TON SDK — Overview

TON SDK is a modular SDK for building applications on The Open Network (TON). It offers first‑class support for Kotlin
and Java on the JVM/Android, and can be consumed from Swift on iOS via Kotlin Multiplatform. It provides the core data
structures (Cells/BOC), TL‑B codecs, crypto primitives, networking stacks (ADNL/RLDP), and high‑level APIs (contracts,
lite client) so you can integrate TON from server, desktop, Android, or iOS with one codebase.

This document explains the SDK at a high level: what it is, how it is organized, and when to use each module. For
hands‑on guides and API details, see the other topics in this documentation set.

## What is TON SDK?

A collection of modular libraries published on Maven Central under the `org.ton.kotlin` group. Each module focuses on a
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
- Services querying blockchain data via the Lite Client.
- Tooling and middleware that rely on TL‑B codecs and BOC manipulation.

## Installation

Add TON SDK modules from Maven Central. Use the coordinates under the org.ton.kotlin group. Keep module versions
aligned (e.g., 0.5.0).

Gradle (Kotlin DSL):

```kotlin
val tonVersion = "0.5.0"

dependencies {
    implementation("org.ton.kotlin:ton-kotlin-tvm:$tonVersion")        // Cells/BOC
    implementation("org.ton.kotlin:ton-kotlin-crypto:$tonVersion")     // Crypto
    implementation("org.ton.kotlin:ton-kotlin-tlb:$tonVersion")        // TL-B codec
    implementation("org.ton.kotlin:ton-kotlin-liteclient:$tonVersion") // Lite client API
    // Optional:
    implementation("org.ton.kotlin:ton-kotlin-contract:$tonVersion")   // Contracts helpers
    implementation("org.ton.kotlin:ton-kotlin-adnl:$tonVersion")       // ADNL transport
}
```

Gradle (Groovy DSL):

```groovy
def tonVersion = '0.5.0'

dependencies {
    implementation "org.ton.kotlin:ton-kotlin-tvm:$tonVersion"
    implementation "org.ton.kotlin:ton-kotlin-crypto:$tonVersion"
    implementation "org.ton.kotlin:ton-kotlin-tlb:$tonVersion"
    implementation "org.ton.kotlin:ton-kotlin-liteclient:$tonVersion"
    // Optional:
    implementation "org.ton.kotlin:ton-kotlin-contract:$tonVersion"
    implementation "org.ton.kotlin:ton-kotlin-adnl:$tonVersion"
}
```

Maven:

```xml

<dependencies>
    <dependency>
        <groupId>org.ton.kotlin</groupId>
        <artifactId>ton-kotlin-tvm</artifactId>
        <version>0.5.0</version>
    </dependency>
    <dependency>
        <groupId>org.ton.kotlin</groupId>
        <artifactId>ton-kotlin-crypto</artifactId>
        <version>0.5.0</version>
    </dependency>
    <dependency>
        <groupId>org.ton.kotlin</groupId>
        <artifactId>ton-kotlin-tlb</artifactId>
        <version>0.5.0</version>
    </dependency>
    <dependency>
        <groupId>org.ton.kotlin</groupId>
        <artifactId>ton-kotlin-liteclient</artifactId>
        <version>0.5.0</version>
    </dependency>
    <!-- Optional -->
    <dependency>
        <groupId>org.ton.kotlin</groupId>
        <artifactId>ton-kotlin-contract</artifactId>
        <version>0.5.0</version>
    </dependency>
    <dependency>
        <groupId>org.ton.kotlin</groupId>
        <artifactId>ton-kotlin-adnl</artifactId>
        <version>0.5.0</version>
    </dependency>
</dependencies>
```

## Quick start

Add the artifacts you need from Maven Central and start from the level that fits your use case.

### Kotlin (JVM/Android)

Creating a Cell and serializing to BOC:

```kotlin
val cell = org.ton.cell.buildCell {
    storeUInt(0xDEADBEEFu, 32)
}
val boc: ByteArray = org.ton.boc.BagOfCells.of(cell).toByteArray()
```

Decoding a TL‑B object (schema provided by block‑tlb module):

```kotlin
val block = org.ton.tlb.loadTlb<Block>(boc)
```

### Java (JVM)

Creating a Cell and serializing to BOC:

```java
public class Example {
    static void main(String[] args) {
        var builder = org.ton.cell.CellBuilderKt.beginCell();
        builder.storeUInt(0xDEADBEEF, 32);
        org.ton.cell.Cell cell = builder.endCell();
        byte[] boc = org.ton.boc.BagOfCells.Companion.of(new org.ton.cell.Cell[]{cell}).toByteArray();
    }
}
```

See examples/ and tests in the repository for more scenarios.

## Supported platforms

- JVM / Android — Kotlin and Java (first‑class)
- iOS — Swift via Kotlin Multiplatform bindings (availability may vary by module)
- Desktop/Server — JVM
- Other Kotlin targets may be available per‑module; check each module’s README or Gradle metadata.

## Versioning and compatibility

- Semantic‑like versioning across modules. Prefer matching versions (e.g., 0.5.x) for best compatibility.
- Wire formats (BOC, TL‑B) follow TON specifications; backwards compatibility is preserved unless TON protocol changes.

## Where to go next

- Project README: architecture, badges, and links
- Wiki/Docs: deeper guides and API references
- Examples directory: runnable samples
- Telegram chat: community support and announcements

## Glossary

- Cell — The fundamental immutable binary structure in TON used by TVM.
- BOC (Bag of Cells) — A container format for serializing graphs of Cells.
- TL‑B — Type language for describing binary data structures in TON.
- ADNL — Abstract Datagram Network Layer, a TON network transport.
- RLDP — Reliable Link over Datagram Protocol used over ADNL.
- Lite Client — A client that queries TON blockchain data without running a full node.
