@file:Suppress("DEPRECATION")

package org.ton.kotlin.block

@Deprecated("use ActionPhase", replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.ActionPhase"))
public typealias TrActionPhase = org.ton.kotlin.transaction.phase.ActionPhase

@Deprecated("use ComputePhase", replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.ComputePhase"))
public typealias TrComputePhase = org.ton.kotlin.transaction.phase.ComputePhase

@Deprecated(
    "use ComputePhase.Skipped",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.ComputePhase.Skipped")
)
public typealias TrPhaseComputeSkipped = org.ton.kotlin.transaction.phase.ComputePhase.Skipped

@Deprecated(
    "use ComputePhase.Skipped",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.ComputePhase.Skipped")
)
public typealias ComputeSkipReason = org.ton.kotlin.transaction.phase.ComputePhase.Skipped

@Deprecated(
    "use ComputePhase.Executed",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.ComputePhase.Executed")
)
public typealias TrPhaseComputeVm = org.ton.kotlin.transaction.phase.ComputePhase.Executed

@Deprecated("use CreditPhase", replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.CreditPhase"))
public typealias TrCreditPhase = org.ton.kotlin.transaction.phase.CreditPhase

@Deprecated("use StoragePhase", replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.StoragePhase"))
public typealias TrStoragePhase = org.ton.kotlin.transaction.phase.StoragePhase

@Deprecated("use BouncePhase", replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.BouncePhase"))
public typealias TrBouncePhase = org.ton.kotlin.transaction.phase.BouncePhase

@Deprecated(
    "use BouncePhase.NoFunds",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.BouncePhase.NoFunds")
)
public typealias TrPhaseBounceNoFunds = org.ton.kotlin.transaction.phase.BouncePhase.NoFunds

@Deprecated(
    "use BouncePhase.Executed",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.BouncePhase.Executed")
)
public typealias TrPhaseBounceOk = org.ton.kotlin.transaction.phase.BouncePhase.Executed

@Deprecated("use TrBouncePhase?", replaceWith = ReplaceWith("org.ton.kotlin.transaction.phase.BouncePhase?"))
public typealias TrPhaseBounceNegFunds = org.ton.kotlin.transaction.phase.BouncePhase?

@Deprecated("use TransactionInfo", replaceWith = ReplaceWith("org.ton.kotlin.transaction.TransactionInfo"))
public typealias TransactionDescr = org.ton.kotlin.transaction.TransactionInfo

@Deprecated(
    "use TransactionInfo.MergeInstall",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.TransactionInfo.MergeInstall")
)
public typealias TransMergeInstall = org.ton.kotlin.transaction.TransactionInfo.MergeInstall

@Deprecated(
    "use TransactionInfo.MergePrepare",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.TransactionInfo.MergePrepare")
)
public typealias TransMergePrepare = org.ton.kotlin.transaction.TransactionInfo.MergePrepare

@Deprecated(
    "use TransactionInfo.Ordinary",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.TransactionInfo.Ordinary")
)
public typealias TransOrd = org.ton.kotlin.transaction.TransactionInfo.Ordinary

@Deprecated(
    "use TransactionInfo.SplitInstall",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.TransactionInfo.SplitInstall")
)
public typealias TransSplitInstall = org.ton.kotlin.transaction.TransactionInfo.SplitInstall

@Deprecated(
    "use TransactionInfo.SplitPrepare",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.TransactionInfo.SplitPrepare")
)
public typealias TransSplitPrepare = org.ton.kotlin.transaction.TransactionInfo.SplitPrepare

@Deprecated(
    "use TransactionInfo.Storage",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.TransactionInfo.Storage")
)
public typealias TransStorage = org.ton.kotlin.transaction.TransactionInfo.Storage

@Deprecated(
    "use TransactionInfo.TickTock",
    replaceWith = ReplaceWith("org.ton.kotlin.transaction.TransactionInfo.TickTock")
)
public typealias TransTickTock = org.ton.kotlin.transaction.TransactionInfo.TickTock

@Deprecated("use org.ton.kotlin.account.Account", replaceWith = ReplaceWith("org.ton.kotlin.account.Account"))
public typealias AccountInfo = org.ton.kotlin.account.Account

@Deprecated("use org.ton.kotlin.account.Account?", replaceWith = ReplaceWith("org.ton.kotlin.account.Account?"))
public typealias Account = org.ton.kotlin.account.Account?

@Deprecated("use org.ton.kotlin.account.Account?", replaceWith = ReplaceWith("org.ton.kotlin.account.Account?"))
public typealias AccountNone = org.ton.kotlin.account.Account?

@Deprecated("use org.ton.kotlin.account.ShardAccount", replaceWith = ReplaceWith("org.ton.kotlin.account.ShardAccount"))
public typealias ShardAccount = org.ton.kotlin.account.ShardAccount
