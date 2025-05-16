package org.ton.kotlin.contract.wallet

import kotlinx.serialization.json.Json
import org.ton.kotlin.api.liteclient.config.LiteClientConfigGlobal

val TESTNET_GLOBAL_CONFIG_JSON = """
{
	"liteservers": [
		{
			"ip": 822907680,
			"port": 27842,
			"provided":"Beavis",
			"id": {
				"@type": "pub.ed25519",
				"key": "sU7QavX2F964iI9oToP9gffQpCQIoOLppeqL/pdPvpM="
			}
		},
		{
			"ip": -1468571697,
			"port": 27787,
			"provided":"Beavis",
			"id": {
				"@type": "pub.ed25519",
				"key": "Y/QVf6G5VDiKTZOKitbFVm067WsuocTN8Vg036A4zGk="
			}
		},
		{
			"ip": -1468575011,
			"port": 51088,
			"provided":"Beavis",
			"id": {
				"@type": "pub.ed25519",
				"key": "Sy5ghr3EahQd/1rDayzZXt5+inlfF+7kLfkZDJcU/ek="
			}
		},
		{
			"ip": 1844203537,
			"port": 37537,
			"provided":"Neo",
			"id": {
				"@type": "pub.ed25519",
				"key": "K1F7zEe0ETf+SwkefLS56hJE8x42sjCVsBJJuaY7nEA="
			}
		},
		{
			"ip": 1844203589,
			"port": 34411,
			"provided":"Neo",
			"id": {
				"@type": "pub.ed25519",
				"key": "pOpRRpIxDuMRm1qFUPpvVjD62vo8azkO0npw4FPcW/I="
			}
		},
		{
			"ip": 1047529523,
			"port": 37649,
			"provided":"Neo",
			"id": {
				"@type": "pub.ed25519",
				"key": "pRf2sAa7d+Chl8gDclWOMtthtxjKnLYeAIzk869mMvA="
			}
		},
		{
			"ip": 1592601963,
			"port": 13833,
			"id": {
				"@type": "pub.ed25519",
				"key": "QpVqQiv1u3nCHuBR3cg3fT6NqaFLlnLGbEgtBRukDpU="
			}
		},
		{
			"ip": 1162057690,
			"port": 35939,
			"id": {
				"@type": "pub.ed25519",
				"key": "97y55AkdzXWyyVuOAn+WX6p66XTNs2hEGG0jFUOkCIo="
			}
		},
		{
			"ip": -1304477830,
			"port": 20700,
			"id": {
				"@type": "pub.ed25519",
				"key": "dGLlRRai3K9FGkI0dhABmFHMv+92QEVrvmTrFf5fbqA="
			}
		},
		{
			"ip": 1959453117,
			"port": 20700,
			"id": {
				"@type": "pub.ed25519",
				"key": "24RL7iVI20qcG+j//URfd/XFeEG9qtezW2wqaYQgVKw="
			}
		},
		{
			"ip": -809760973,
			"port": 20700,
			"id": {
				"@type": "pub.ed25519",
				"key": "vunMV7K35yPlTQPx/Fqk6s+4/h5lpcbP+ao0Cy3M2hw="
			}
		},
		{
			"ip": 1097633201,
			"port": 17439,
			"id": {
				"@type": "pub.ed25519",
				"key": "0MIADpLH4VQn+INHfm0FxGiuZZAA8JfTujRqQugkkA8="
			}
		},
		{
			"ip": 1091956407,
			"port": 16351,
			"id": {
				"@type": "pub.ed25519",
				"key": "Mf/JGvcWAvcrN3oheze8RF/ps6p7oL6ifrIzFmGQFQ8="
			}
		}
	],
	"dht": {
		"a": 3,
		"k": 3,
		"static_nodes": {
			"nodes": [
				{
					"@type": "dht.node",
					"id": {
						"@type": "pub.ed25519",
						"key": "K2AWu8leN2RjYmhMpYAaGX/F6nGVk9oZw9c09RX3yyc="
					},
					"addr_list": {
						"@type": "adnl.addressList",
						"addrs": [
							{
								"@type": "adnl.address.udp",
								"ip": 1592601963,
								"port": 38723
							}
						],
						"version": 0,
						"reinit_date": 0,
						"priority": 0,
						"expire_at": 0
					},
					"version": -1,
					"signature": "21g16jxnqbb2ENAijrZFccHqLQcmmpkAI1HA46DaPvnVYvMkATFNEyHTy2R1T1jgU5M7CCLGJN+MxhwZfl/ZDA=="
				},
				{
					"@type": "dht.node",
					"id": {
						"@type": "pub.ed25519",
						"key": "fVIJzD9ATMilaPd847eFs6PtGSB67C+D9b4R+nf1+/s="
					},
					"addr_list": {
						"@type": "adnl.addressList",
						"addrs": [
							{
								"@type": "adnl.address.udp",
								"ip": 1097649206,
								"port": 29081
							}
						],
						"version": 0,
						"reinit_date": 0,
						"priority": 0,
						"expire_at": 0
					},
					"version": -1,
					"signature": "wH0HEVT6yAfZZAoD5bF6J3EZWdSFwBGl1ZpOfhxZ0Bp2u52tv8OzjeH8tlZ+geMLTG50Csn5nxSKP1tswTWwBg=="
				},
				{
					"@type": "dht.node",
					"id": {
						"@type": "pub.ed25519",
						"key": "gu+woR+x7PoRmaMqAP7oeOjK2V4U0NU8ofdacWZ34aY="
					},
					"addr_list": {
						"@type": "adnl.addressList",
						"addrs": [
							{
								"@type": "adnl.address.udp",
								"ip": 1162057690,
								"port": 41578
							}
						],
						"version": 0,
						"reinit_date": 0,
						"priority": 0,
						"expire_at": 0
					},
					"version": -1,
					"signature": "0PwDLXpN3IbRQuOTLkZBjkbT6+IkeUcvlhWrUY9us3IfSehmCfQjScR9mkVYsQ6cQHF+JeaFmqzV4GAiUcgjAg=="
				},
				{
					"@type": "dht.node",
					"id": {
						"@type": "pub.ed25519",
						"key": "WC4BO1eZ916FnLBSKmt07Pn5NP4D3/1wary1VjaCLaY="
					},
					"addr_list": {
						"@type": "adnl.addressList",
						"addrs": [
							{
								"@type": "adnl.address.udp",
								"ip": -1304477830,
								"port": 9670
							}
						],
						"version": 0,
						"reinit_date": 0,
						"priority": 0,
						"expire_at": 0
					},
					"version": -1,
					"signature": "cvpzkGeuEuKV+d92qIVkln9ngm8qeDnmYtK5rq8uSet0392hAZcIv2IniDzTw0rN42NaOHL9A4KEelwKu1N2Ag=="
				},
				{
					"@type": "dht.node",
					"id": {
						"@type": "pub.ed25519",
						"key": "nC8dcxV+EV2i0ARvub94IFJKKZUYACfY4xFj1NaG7Pw="
					},
					"addr_list": {
						"@type": "adnl.addressList",
						"addrs": [
							{
								"@type": "adnl.address.udp",
								"ip": 1959453117,
								"port": 63625
							}
						],
						"version": 0,
						"reinit_date": 0,
						"priority": 0,
						"expire_at": 0
					},
					"version": -1,
					"signature": "AHF6joNvQhyFFE0itV4OMA9n3Q8CEHVKapCLqazP7QJ4arsn4pdVkRYiGFEyQkngx+cm8izU4gB0JIaxF6PiBg=="
				},
				{
					"@type": "dht.node",
					"id": {
						"@type": "pub.ed25519",
						"key": "dqsRZLzTg/P7uxUlQpgl4VyTBNYBRMc4js3mnRiolBk="
					},
					"addr_list": {
						"@type": "adnl.addressList",
						"addrs": [
							{
								"@type": "adnl.address.udp",
								"ip": -809760973,
								"port": 40398
							}
						],
						"version": 0,
						"reinit_date": 0,
						"priority": 0,
						"expire_at": 0
					},
					"version": -1,
					"signature": "mJxLrAv5RamN5B9mDz6MhQwFjF92D3drJ5efOSZryDaazil0AR4bRHh4vxzZlYiPhi/X/NyG6WwNvKBz+1ntBw=="
				},
				{
					"@type": "dht.node",
					"id": {
						"@type": "pub.ed25519",
						"key": "fO6cFYRCRrD+yQzOJdHcNWpRFwu+qLhQnddLq0gGbTs="
					},
					"addr_list": {
						"@type": "adnl.addressList",
						"addrs": [
							{
								"@type": "adnl.address.udp",
								"ip": 1097633201,
								"port": 7201
							}
						],
						"version": 0,
						"reinit_date": 0,
						"priority": 0,
						"expire_at": 0
					},
					"version": -1,
					"signature": "o/rhtiUL3rvA08TKBcCn0DCiSjsNQdAv41aw7VVUig7ubaqJzYMv1cW3qMjxvsXn1BOugIheJm7voA1/brbtCg=="
				}
			],
			"@type": "dht.nodes"
		},
		"@type": "dht.config.global"
	},
	"@type": "config.global",
	"validator": {
		"zero_state": {
			"file_hash": "Z+IKwYS54DmmJmesw/nAD5DzWadnOCMzee+kdgSYDOg=",
			"seqno": 0,
			"root_hash": "gj+B8wb/AmlPk1z1AhVI484rhrUpgSr2oSFIh56VoSg=",
			"workchain": -1,
			"shard": -9223372036854775808
		},
		"@type": "validator.config.global",
		"init_block": {
			"workchain": -1,
			"shard": -9223372036854775808,
			"seqno": 17908219,
			"root_hash": "y6qWqhCnLgzWHjUFmXysaiOljuK5xVoCRMLzUwGInVM=",
			"file_hash": "Y/GziXxwuYte0AM4WT7tTWsCx+6rcfLpGmRaEQwhUKI="
		},
		"hardforks": [
			{
				"file_hash": "jF3RTD+OyOoP+OI9oIjdV6M8EaOh9E+8+c3m5JkPYdg=",
				"seqno": 5141579,
				"root_hash": "6JSqIYIkW7y8IorxfbQBoXiuY3kXjcoYgQOxTJpjXXA=",
				"workchain": -1,
				"shard": -9223372036854775808
			},
			{
				"file_hash": "WrNoMrn5UIVPDV/ug/VPjYatvde8TPvz5v1VYHCLPh8=",
				"seqno": 5172980,
				"root_hash": "054VCNNtUEwYGoRe1zjH+9b1q21/MeM+3fOo76Vcjes=",
				"workchain": -1,
				"shard": -9223372036854775808
			},
			{
				"file_hash": "xRaxgUwgTXYFb16YnR+Q+VVsczLl6jmYwvzhQ/ncrh4=",
				"seqno": 5176527,
				"root_hash": "SoPLqMe9Dz26YJPOGDOHApTSe5i0kXFtRmRh/zPMGuI=",
				"workchain": -1,
				"shard": -9223372036854775808
			}
		]
	}
}
""".trimIndent()

private val JSON =
    Json { ignoreUnknownKeys = true }

val TESTNET_GLOBAL_CONFIG: LiteClientConfigGlobal =
    JSON.decodeFromString(TESTNET_GLOBAL_CONFIG_JSON)
