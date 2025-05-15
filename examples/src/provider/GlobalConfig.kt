package org.ton.kotlin.examples.provider

import kotlinx.serialization.json.Json
import org.ton.kotlin.adnl.liteclient.config.LiteClientConfigGlobal

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

val MAINNET_GLOBAL_CONFIG_JSON = """
{
  "@type": "config.global",
  "dht": {
    "@type": "dht.config.global",
    "k": 6,
    "a": 3,
    "static_nodes": {
      "@type": "dht.nodes",
      "nodes": [
        {
            "@type": "dht.node",
            "id": {
                "@type": "pub.ed25519",
                "key": "6PGkPQSbyFp12esf1NqmDOaLoFA8i9+Mp5+cAx5wtTU="
            },
            "addr_list": {
                "@type": "adnl.addressList",
                "addrs": [
                    {
                        "@type": "adnl.address.udp",
                        "ip": -1185526007,
                        "port": 22096
                    }
                ],
                "version": 0,
                "reinit_date": 0,
                "priority": 0,
                "expire_at": 0
            },
            "version": -1,
            "signature": "L4N1+dzXLlkmT5iPnvsmsixzXU0L6kPKApqMdcrGP5d9ssMhn69SzHFK+yIzvG6zQ9oRb4TnqPBaKShjjj2OBg=="
        },
        {
          "@type": "dht.node",
          "id": {
            "@type": "pub.ed25519",
            "key": "4R0C/zU56k+x2HGMsLWjX2rP/SpoTPIHSSAmidGlsb8="
          },
          "addr_list": {
            "@type": "adnl.addressList",
            "addrs": [
              {
                "@type": "adnl.address.udp",
                "ip": -1952265919,
                "port": 14395
              }
            ],
            "version": 0,
            "reinit_date": 0,
            "priority": 0,
            "expire_at": 0
          },
          "version": -1,
          "signature": "0uwWyCFn2KjPnnlbSFYXLZdwIakaSgI9WyRo87J3iCGwb5TvJSztgA224A9kNAXeutOrXMIPYv1b8Zt8ImsrCg=="
        },
        {
          "@type": "dht.node",
          "id": {
            "@type": "pub.ed25519",
            "key": "/YDNd+IwRUgL0mq21oC0L3RxrS8gTu0nciSPUrhqR78="
          },
          "addr_list": {
            "@type": "adnl.addressList",
            "addrs": [
              {
                "@type": "adnl.address.udp",
                "ip": -1402455171,
                "port": 14432
              }
            ],
            "version": 0,
            "reinit_date": 0,
            "priority": 0,
            "expire_at": 0
          },
          "version": -1,
          "signature": "6+oVk6HDtIFbwYi9khCc8B+fTFceBUo1PWZDVTkb4l84tscvr5QpzAkdK7sS5xGzxM7V7YYQ6gUQPrsP9xcLAw=="
        },
        {
          "@type": "dht.node",
          "id": {
            "@type": "pub.ed25519",
            "key": "DA0H568bb+LoO2LGY80PgPee59jTPCqqSJJzt1SH+KE="
          },
          "addr_list": {
            "@type": "adnl.addressList",
            "addrs": [
              {
                "@type": "adnl.address.udp",
                "ip": -1402397332,
                "port": 14583
              }
            ],
            "version": 0,
            "reinit_date": 0,
            "priority": 0,
            "expire_at": 0
          },
          "version": -1,
          "signature": "cL79gDTrixhaM9AlkCdZWccCts7ieQYQBmPxb/R7d7zHw3bEHL8Le96CFJoB1KHu8C85iDpFK8qlrGl1Yt/ZDg=="
        },
        {
          "@type": "dht.node",
          "id": {
            "@type": "pub.ed25519",
            "key": "MJr8xja0xpu9DoisFXBrkNHNx1XozR7HHw9fJdSyEdo="
          },
          "addr_list": {
            "@type": "adnl.addressList",
            "addrs": [
              {
                "@type": "adnl.address.udp",
                "ip": -2018147130,
                "port": 6302
              }
            ],
            "version": 0,
            "reinit_date": 0,
            "priority": 0,
            "expire_at": 0
          },
          "version": -1,
          "signature": "XcR5JaWcf4QMdI8urLSc1zwv5+9nCuItSE1EDa0dSwYF15R/BtJoKU5YHA4/T8SiO18aVPQk2SL1pbhevuMrAQ=="
        },
        {
          "@type": "dht.node",
          "id": {
            "@type": "pub.ed25519",
            "key": "Fhldu4zlnb20/TUj9TXElZkiEmbndIiE/DXrbGKu+0c="
          },
          "addr_list": {
            "@type": "adnl.addressList",
            "addrs": [
              {
                "@type": "adnl.address.udp",
                "ip": -2018147075,
                "port": 6302
              }
            ],
            "version": 0,
            "reinit_date": 0,
            "priority": 0,
            "expire_at": 0
          },
          "version": -1,
          "signature": "nUGB77UAkd2+ZAL5PgInb3TvtuLLXJEJ2icjAUKLv4qIGB3c/O9k/v0NKwSzhsMP0ljeTGbcIoMDw24qf3goCg=="
        },
		{
		  "@type": "dht.node",
		  "id": {
		    "@type": "pub.ed25519",
		    "key": "gzUNJnBJhdpooYCE8juKZo2y4tYDIQfoCvFm0yBr7y0="
		  },
		  "addr_list": {
		    "@type": "adnl.addressList",
		    "addrs": [
		      {
		        "@type": "adnl.address.udp",
		        "ip": 89013260,
		        "port": 54390
		      }
		    ],
		    "version": 0,
		    "reinit_date": 0,
		    "priority": 0,
		    "expire_at": 0
		  },
		  "version": -1,
		  "signature": "LCrCkjmkMn6AZHW2I+oRm1gHK7CyBPfcb6LwsltskCPpNECyBl1GxZTX45n0xZtLgyBd/bOqMPBfawpQwWt1BA=="
		},
		{
		  "@type": "dht.node",
		  "id": {
		    "@type": "pub.ed25519",
		    "key": "jXiLaOQz1HPayilWgBWhV9xJhUIqfU95t+KFKQPIpXg="
		  },
		  "addr_list": {
		    "@type": "adnl.addressList",
		    "addrs": [
		      {
		        "@type": "adnl.address.udp",
		        "ip": 94452896,
		        "port": 12485
		      }
		    ],
		    "version": 0,
		    "reinit_date": 0,
		    "priority": 0,
		    "expire_at": 0
		  },
		  "version": -1,
		  "signature": "fKSZh9nXMx+YblkQXn3I/bndTD0JZ1yAtK/tXPIGruNglpe9sWMXR+8fy3YogPhLJMdjNiMom1ya+tWG7qvBAQ=="
		},
		{
		  "@type": "dht.node",
		  "id": {
		    "@type": "pub.ed25519",
		    "key": "vhFPq+tgjJi+4ZbEOHBo4qjpqhBdSCzNZBdgXyj3NK8="
		  },
		  "addr_list": {
		    "@type": "adnl.addressList",
		    "addrs": [
		      {
		        "@type": "adnl.address.udp",
		        "ip": 85383775,
		        "port": 36752
		      }
		    ],
		    "version": 0,
		    "reinit_date": 0,
		    "priority": 0,
		    "expire_at": 0
		  },
		  "version": -1,
		  "signature": "kBwAIgJVkz8AIOGoZcZcXWgNmWq8MSBWB2VhS8Pd+f9LLPIeeFxlDTtwAe8Kj7NkHDSDC+bPXLGQZvPv0+wHCg=="
		},
		{
		  "@type": "dht.node",
		  "id": {
		    "@type": "pub.ed25519",
		    "key": "sbsuMcdyYFSRQ0sG86/n+ZQ5FX3zOWm1aCVuHwXdgs0="
		  },
		  "addr_list": {
		    "@type": "adnl.addressList",
		    "addrs": [
		      {
		        "@type": "adnl.address.udp",
		        "ip": 759132846,
		        "port": 50187
		      }
		    ],
		    "version": 0,
		    "reinit_date": 0,
		    "priority": 0,
		    "expire_at": 0
		  },
		  "version": -1,
		  "signature": "9FJwbFw3IECRFkb9bA54YaexjDmlNBArimWkh+BvW88mjm3K2i5V2uaBPS3GubvXWOwdHLE2lzQBobgZRGMyCg=="
		},
		{
		  "@type": "dht.node",
		  "id": {
		    "@type": "pub.ed25519",
		    "key": "aeMgdMdkkbkfAS4+n4BEGgtqhkf2/zXrVWWECOJ/h3A="
		  },
		  "addr_list": {
		    "@type": "adnl.addressList",
		    "addrs": [
		      {
		        "@type": "adnl.address.udp",
		        "ip": -1481887565,
		        "port": 25975
		      }
		    ],
		    "version": 0,
		    "reinit_date": 0,
		    "priority": 0,
		    "expire_at": 0
		  },
		  "version": -1,
		  "signature": "z5ogivZWpQchkS4UR4wB7i2pfOpMwX9Nd/USxinL9LvJPa+/Aw3F1AytR9FX0BqDftxIYvblBYAB5JyAmlj+AA=="
		},
		{
		  "@type": "dht.node",
		  "id": {
		    "@type": "pub.ed25519",
		    "key": "rNzhnAlmtRn9rTzW6o2568S6bbOXly7ddO1olDws5wM="
		  },
		  "addr_list": {
		    "@type": "adnl.addressList",
		    "addrs": [
		      {
		        "@type": "adnl.address.udp",
		        "ip": -2134428422,
		        "port": 45943
		      }
		    ],
		    "version": 0,
		    "reinit_date": 0,
		    "priority": 0,
		    "expire_at": 0
		  },
		  "version": -1,
		  "signature": "sn/+ZfkfCSw2bHnEnv04AXX/Goyw7+StHBPQOdPr+wvdbaJ761D7hyiMNdQGbuZv2Ep2cXJpiwylnZItrwdUDg=="
		}
      ]
    }
  },
  "liteservers": [
    {
      "ip": 84478511,
      "port": 19949,
      "id": {
        "@type": "pub.ed25519",
        "key": "n4VDnSCUuSpjnCyUk9e3QOOd6o0ItSWYbTnW3Wnn8wk="
      }
    },
    {
      "ip": 84478479,
      "port": 48014,
      "id": {
        "@type": "pub.ed25519",
        "key": "3XO67K/qi+gu3T9v8G2hx1yNmWZhccL3O7SoosFo8G0="
      }
    },
    {
      "ip": -2018135749,
      "port": 53312,
      "id": {
        "@type": "pub.ed25519",
        "key": "aF91CuUHuuOv9rm2W5+O/4h38M3sRm40DtSdRxQhmtQ="
      }
    },
    {
      "ip": -2018145068,
      "port": 13206,
      "id": {
        "@type": "pub.ed25519",
        "key": "K0t3+IWLOXHYMvMcrGZDPs+pn58a17LFbnXoQkKc2xw="
      }
    },
    {
      "ip": -2018145059,
      "port": 46995,
      "id": {
        "@type": "pub.ed25519",
        "key": "wQE0MVhXNWUXpWiW5Bk8cAirIh5NNG3cZM1/fSVKIts="
      }
    },
    {
      "ip": 1091931625,
      "port": 30131,
      "id": {
        "@type": "pub.ed25519",
        "key": "wrQaeIFispPfHndEBc0s0fx7GSp8UFFvebnytQQfc6A="
      }
    },
    {
      "ip": 1091931590,
      "port": 47160,
      "id": {
        "@type": "pub.ed25519",
        "key": "vOe1Xqt/1AQ2Z56Pr+1Rnw+f0NmAA7rNCZFIHeChB7o="
      }
    },
    {
      "ip": 1091931623,
      "port": 17728,
      "id": {
        "@type": "pub.ed25519",
        "key": "BYSVpL7aPk0kU5CtlsIae/8mf2B/NrBi7DKmepcjX6Q="
      }
    },
    {
      "ip": 1091931589,
      "port": 13570,
      "id": {
        "@type": "pub.ed25519",
        "key": "iVQH71cymoNgnrhOT35tl/Y7k86X5iVuu5Vf68KmifQ="
      }
    },
    {
      "ip": -1539021362,
      "port": 52995,
      "id": {
        "@type": "pub.ed25519",
        "key": "QnGFe9kihW+TKacEvvxFWqVXeRxCB6ChjjhNTrL7+/k="
      }
    },
    {
      "ip": -1539021936,
      "port": 20334,
      "id": {
        "@type": "pub.ed25519",
        "key": "gyLh12v4hBRtyBygvvbbO2HqEtgl+ojpeRJKt4gkMq0="
      }
    },
    {
      "ip": -1136338705,
      "port": 19925,
      "id": {
        "@type": "pub.ed25519",
        "key": "ucho5bEkufbKN1JR1BGHpkObq602whJn3Q3UwhtgSo4="
      }
    },
    {
      "ip": 868465979,
      "port": 19434,
      "id": {
        "@type": "pub.ed25519",
        "key": "J5CwYXuCZWVPgiFPW+NY2roBwDWpRRtANHSTYTRSVtI="
      }
    },
    {
      "ip": 868466060,
      "port": 23067,
      "id": {
        "@type": "pub.ed25519",
        "key": "vX8d0i31zB0prVuZK8fBkt37WnEpuEHrb7PElk4FJ1o="
      }
    },
    {
      "ip": -2018147130,
      "port": 53560,
      "id": {
        "@type": "pub.ed25519",
        "key": "NlYhh/xf4uQpE+7EzgorPHqIaqildznrpajJTRRH2HU="
      }
    },
    {
      "ip": -2018147075,
      "port": 46529,
      "id": {
        "@type": "pub.ed25519",
        "key": "jLO6yoooqUQqg4/1QXflpv2qGCoXmzZCR+bOsYJ2hxw="
      }
    },
    {
      "ip": 908566172,
      "port": 51565,
      "id": {
        "@type": "pub.ed25519",
        "key": "TDg+ILLlRugRB4Kpg3wXjPcoc+d+Eeb7kuVe16CS9z8="
      }
    },
    {
        "ip": -1185526007,
        "port": 4701,
        "id": {
            "@type": "pub.ed25519",
            "key": "G6cNAr6wXBBByWDzddEWP5xMFsAcp6y13fXA8Q7EJlM="
        }
    }
  ],
  "validator": {
    "@type": "validator.config.global",
    "zero_state": {
      "workchain": -1,
      "shard": -9223372036854775808,
      "seqno": 0,
      "root_hash": "F6OpKZKqvqeFp6CQmFomXNMfMj2EnaUSOXN+Mh+wVWk=",
      "file_hash": "XplPz01CXAps5qeSWUtxcyBfdAo5zVb1N979KLSKD24="
    },
    "init_block": {
      "root_hash": "VpWyfNOLm8Rqt6CZZ9dZGqJRO3NyrlHHYN1k1oLbJ6g=",
      "seqno": 34835953,
      "file_hash": "8o12KX54BtJM8RERD1J97Qe1ZWk61LIIyXydlBnixK8=",
      "workchain": -1,
      "shard": -9223372036854775808
    },
    "hardforks": [
      {
        "file_hash": "t/9VBPODF7Zdh4nsnA49dprO69nQNMqYL+zk5bCjV/8=",
        "seqno": 8536841,
        "root_hash": "08Kpc9XxrMKC6BF/FeNHPS3MEL1/Vi/fQU/C9ELUrkc=",
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

val MAINNET_GLOBAL_CONFIG: LiteClientConfigGlobal =
    JSON.decodeFromString(MAINNET_GLOBAL_CONFIG_JSON)
