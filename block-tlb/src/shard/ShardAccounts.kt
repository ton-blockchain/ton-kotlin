@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.shard

import kotlinx.io.bytestring.ByteString
import org.ton.block.DepthBalanceInfo
import org.ton.kotlin.account.ShardAccount
import org.ton.kotlin.dict.AugmentedDictionary

/**
 * A dictionary of account states.
 */
public typealias ShardAccounts = AugmentedDictionary<ByteString, DepthBalanceInfo, ShardAccount>