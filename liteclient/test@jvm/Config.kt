import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language
import org.ton.api.liteclient.config.LiteClientConfigGlobal

@Language("JSON")
val CONFIG_RAW = """
{
  "@type": "config.global",
  "dht": {
    "@type": "dht.config.global",
    "k": 6,
    "a": 3,
    "static_nodes": {
      "@type": "dht.nodes",
      "nodes": []
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
      "root_hash": "YRkrcmZMvLBvjanwKCyL3w4oceGPtFfgx8ym1QKCK/4=",
      "seqno": 27747086,
      "file_hash": "N42xzPnJjDlE3hxPXOb+pNzXomgRtpX5AZzMPnIA41s=",
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

private val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "@type"
}

val CONFIG = json.decodeFromString<LiteClientConfigGlobal>(CONFIG_RAW)
