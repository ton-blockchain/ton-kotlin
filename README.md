# Kotlin/Multiplatform SDK for [The Open Network](https://ton.org)

[![Maven Central][maven-central-svg]][maven-central]
[![Kotlin][kotlin-svg]][kotlin]
[![License][license-svg]][license]
[![Telegram][telegram-svg]][telegram]
[![Based on TON][ton-svg]][ton]

## Modules

### Core components

* `org.ton:ton-kotlin-tvm:0.3.0` - TVM Primitives (Cells, BOC, etc.)
* `org.ton:ton-kotlin-crypto:0.3.0` - Crypto primitives for TON (ED25519, SHA, etc.)
* `org.ton:ton-kotlin-adnl:0.3.0` - ADNL (Abstract Datagram Network Layer) TON Network implementation

### API Interfaces

* `org.ton:ton-kotlin-contract:0.3.0` - Smart-contracts API interface
* `org.ton:ton-kotlin-liteclient:0.3.0` - Lite-client API implementation

### TL-B (TL-Binary)

* `org.ton:ton-kotlin-tlb:0.3.0` - TON TL-B (TL-Binary) serialization/deserialization
* `org.ton:ton-kotlin-block-tlb:0.3.0` - Pre-generated TL-B schemas for TON Blockchain
* `org.ton:ton-kotlin-hashmap-tlb:0.3.0` - Pre-generated TL-B schemas for TON Hashmap (also known as Dictionary)

## Documentation

https://github.com/andreypfau/ton-kotlin/wiki/TON-Kotlin-documentation

<!-- Badges -->

[maven-central]: https://central.sonatype.com/artifact/org.ton/ton-kotlin-tvm/0.3.0

[license]: LICENSE

[kotlin]: http://kotlinlang.org

[ton]: https://ton.org

[telegram]: https://t.me/tonkotlin

[maven-central-svg]: https://img.shields.io/maven-central/v/org.ton/ton-kotlin-tvm?color=blue

[kotlin-svg]: https://img.shields.io/badge/Kotlin-1.9.22-blue.svg?logo=kotlin

[telegram-svg]: https://img.shields.io/badge/Telegram-join%20chat-blue.svg?logo=telegram

[ton-svg]: https://img.shields.io/badge/Based%20on-TON-blue

[license-svg]: https://img.shields.io/github/license/andreypfau/ton-kotlin?color=blue
