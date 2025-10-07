# 0.5.0

### New features

- Added new TL with kotlinx-serialization support
- All cryptography moved to `ton-kotlin-crypto` module under `org.ton.kotlin.crypto` package
- Updated Ktor to 3.3.0

### Deprecated

- Old TL moved to `ton-kotlin-tl-legacy` module
- `CellBuilder.storeBytes`, use `CellBuilder.storeBits` instead

### Removed

- Private/Public keys from `ton-kotlin-tonapi-tl`, use keys from `ton-kotlin-crypto` module

# 0.3.1

### Fixes

* Fixed invalid TL-B constructor prefix for transactions with text (also known as
  Commentary/Memo)  ([#117](https://github.com/ton-community/ton-kotlin/pull/117))

# 0.3.0

### New features

- Added `WalletV4R2Contract` to `ton-kotlin-contract` ([#109](https://github.com/ton-community/ton-kotlin/issues/109))
- Added support for `MsgAddressExt` for wallet contracts

### Fixes

- Fixed LibraryReference Cell creation ([#101](https://github.com/ton-community/ton-kotlin/issues/101))

### Module structure refactoring

- `ton-kotlin-boc` moved into `ton-kotlin-tvm`
- `ton-kotlin-cell` moved into `ton-kotlin-tvm`
- `ton-kotlin-mnemonic` module merged into `ton-kotlin-crypto`
- `ton-kotlin-block` renamed to `ton-kotlin-block-tlb`
- `ton-kotlin-hashmap` renamed to `ton-kotlin-hashmap-tlb`
- `ton-kotlin-api` renamed to `ton-kotlin-tonapi-tl`
- `ton-kotlin-liteapi` renamed to `ton-kotlin-liteapi-tl`

### Removed

- `ton-kotlin-fift` removed due to lack of interest
- `ton-kotlin-logger` removed due uselessness

### Deprecated

- [VarUInteger](https://github.com/ton-community/ton-kotlin/blob/main/ton-kotlin-block/src/commonMain/kotlin/org/ton/block/VarUInteger.kt#L18)
  arithmetic operators will be removed in the next release due separation of serialization objects
  and actual arithmetics, use `BigInt` instead. ([#102](https://github.com/ton-community/ton-kotlin/issues/102))
