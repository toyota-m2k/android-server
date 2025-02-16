# Android-Server

## このライブラリについて

Androidデバイス上で稼働する http サーバーの基本実装です。
リクエスト文字列に基づいて簡単なルーティングを行い、登録されたハンドラを呼び出します。
Androidデバイスに固定IPアドレスを割り当てて本格的なHTTPサーバーとして運用、ということはないと思いますが、
同一LAN内に存在する２台のスマホの片方にしかないファイルを表示したり、両方のデータを同期する、といった機能を中継サーバーを立てることなしに実現できます。

## インストール

### Gradle

`settings.gradle.kts` で、mavenリポジトリ https://jitpack.io への参照を定義。

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
    }
}
```

モジュールの `build.gradle` で、dependencies を追加。

```kotlin
dependencies {
    implementation("com.github.toyota-m2k:android-media-processor:Tag")
}
```
Tag には、リリースバージョンを指定します。
最新のリリースバージョンは、https://jitpack.io/#toyota-m2k/android-server で確認できます。

## 基本的な使い方

1. ルーティングテーブル (`Route` インスタンスの配列) を作成
2. ルーティングテーブルを引数にして `HttpServer`インスタンスを作成
3. ポート番号を引数に `start()` メソッドを呼んでサーバーを起動
4. stop() メソッドを呼んでサーバーを終了

GET/PUT等のHTTPリクエストに対する応答処理は、すべて ルーティングテーブルに記述します。以下、具体例を示して詳しく説明します。

## 使用例

ここでは、スマホ上の写真と動画ファイルを管理するアプリ（MyMedia）を想定します。ファイルの情報は、`fileDB` に保持していて、各ファイルには、アイテムID `id` が割り振られているものとします。また、`star` 属性（任意の文字列）も付けることができます。

このアプリが持っている情報を REST-APIで公開することにします。
実装するAPIは次の通りです。
|コマンド書式|HTTP Method|処理|
|----|----|----|
|`/`|GET|アプリ情報のHTMLを返す。|
|`/list`|GET|fileDBが管理しているファイルのリストをJSON形式で返す。|
|`/photo?id=<アイテムID>`|GET|idで識別されるimage/png形式のファイルを返します。|
|`/movie?id=<アイテムID>`|GET|idで識別されるvideo/mp4形式のファイルを ストリーム(*)として返します。|
|`/star`|PUT|データボディとして渡された id, star (JSON形式) を、fileDB に書き込みます。|

(注 *) ここでいう"ストリーム"とは、Rangeヘッダで指定された範囲を分割して返すことのできる機能を指します。

まず、サーバー機能の入れ物として MyServer というクラスを用意し、HttpServerのインスタンスの初期化、サーバーを起動・停止するメソッドを作成しました。ルーティングテーブル (`val routes:Array<Route>`) の実装以外に必要なコードは、これですべてです。

```kotlin
class MyServer {
    val httpServer:HttpServer
    val routes:Array<Route>

    init {
        httpServer = HttpServer(routes)
    }

    // サーバーを起動
    fun start(port:Int) {
        httpServer.start(port)
    }
    // サーバーを停止
    fun stop() {
        httpServer.stop()
    }
}
```

続いて、ルーティングテーブルを実装します。

ルーティングテーブルは、Route型インスタンスの配列(Array<Route>)です。
'/' に対して html を返す Route を１つだけもつルーティングテーブルは、次のように書けます。

```kotlin
routes = arrayOf(
    Route("Info", HttpMethod.GET, "/") {_,_->
        TextHttpResponse(
            StatusCode.Ok,
            "<html><body>MyMedia ver 1.0</body></html>",
            "text/html"
        )
    },
)
```

Routeのコンストラクタは、４つの引数を受け取ります。

|引数|型|説明|
|----|-----|-----|
|name|String|任意の文字列。プログラム的には使用しませんので、デバッグ時に名前をつけてください。|
|method|HttpMethod \| String|HTTPメソッド名。通常は HttpMethod.GET のように HttpMethod 列挙型で指定しますが、GET/PUT/POST/DELETE 以外を使う場合は文字列で指定します。|
|pattern|String \| Regex|リクエストの文字列パターン。文字列として渡された場合も正規表現文字列として扱います。|
|process|(Route,HttpRequest) -> IHttpResponse|HTTP要求に対する応答ハンドラ。戻り値として、IHttpResponse 型のインスタンスを返すことでクライアントに応答を返します。|

応答ハンドラは、IHttpResponse 型のインスタンスを返します。現在、TextHttpResponse（データボディをStringで指定）、FileHttpResponse（データボディをFileで指定）StreamingHttpRequest（データボディをFileで指定、Rangeによるランダムシークに対応）の3種類の IHttpResponseクラスを用意しています。

上記 `GET '/'` の例では、TextHttpResponse で "text/html" の文字列を 200 のHTTPステータスとともに返します。尚、利用可能な文字コードは、us-ascii または、UTF-8 です。 UTF-8 を使う場合、必要なら、contentType（第3引数）を、"text/html; charset=UTF-8" と指定してください。

以下、各REST-API をルーティングテーブルに追加すると、次のようになります。

```kotlin
class MyServer {
    private val httpServer:HttpServer
    private val routes:Array<Route>

    init {
         routes = arrayOf<Route>(
            // HTML 文字列を返す例
            Route("Info", HttpMethod.GET, "/") {_,_->
                TextHttpResponse(
                    StatusCode.Ok,
                    "<html><body>MyMedia ver 1.0</body></html>",
                    "text/html"
                )
            },
            // JSON 文字列を返す例
            Route("List", HttpMethod.GET, "/list") { _, request ->
                TextHttpResponse(
                    StatusCode.Ok,
                    fileDB.list().fold(JSONArray()) { array, item ->
                        array.put(JSONObject().apply {
                            put("id", "${item.id}")
                            put("label", item.label)
                            put("type", item.type)  // "photo" or "movie"
                            put("star", item.star})
                        })
                }
            },
            // ファイルを返す例
            Route("Photo", HttpMethod.GET,"/photo\\?.+") { _, request ->
                val p = QueryParams.parse(request.url)
                val id = p["id"]?.toIntOrNull() ?: return@Route HttpErrorResponse.badRequest("id is required.")
                val file:File = fileDB.getFile(id) ?: return@Route HttpErrorResponse.notFound()
                FileHttpResponse(StatusCode.Ok, "image/png", file)
            },
            // ストリーミング（分割応答）の例
            Route("Movie", HttpMethod.GET,"/movie\\?.+") { _, request ->
                val p = QueryParams.parse(request.url)
                val id = p["id"]?.toIntOrNull() ?: return@Route HttpErrorResponse.badRequest("id is required.")
                val file:File = fileDB.getFile(id) ?: return@Route HttpErrorResponse.notFound()
                val range = request.headers["Range"]
                if(range==null) {
                    StreamingHttpResponse(StatusCode.Ok, "video/mp4", file, 0L,0L)
                } else {
                    val regRange = Regex("bytes=(?<start>\\d+)(?:-(?<end>\\d+))?");
                    val c = regRange.find(range) ?: return@Route HttpErrorResponse.badRequest("invalid range")
                    val start = c.groups["start"]?.value?.toLongOrNull() ?: 0L
                    val end = c.groups["end"]?.value?.toLongOrNull() ?: 0L
                    StreamingHttpResponse(StatusCode.Ok, "video/mp4", file, start, end)
                }
            },
            // Put (Post) の例
             Route("Star", HttpMethod.PUT, "/star") { _, request ->
                 val json = request.contentAsJson() ?: return@Route HttpErrorResponse.badRequest("invalid json")
                 val id = json.optInt("id", -1)
                 if(id<0) return@Route HttpErrorResponse.badRequest("id is required.")
                 val star = json.optString("star", "")
                 TextHttpResponse(StatusCode.Ok, "accepted", "text/plain")
             },

        )
        // サーバーインスタンスを作成
        httpServer = HttpServer(routes)
    }

    // サーバーを起動
    fun start(port:Int) {
        httpServer.start(port)
    }
    // サーバーを停止
    fun stop() {
        httpServer.stop()
    }
}
```

`GET /list` コマンドの Route は、TextHttpResponse を使って `application/json` のデータを返しています。REST-API では、JSONでデータを受け渡すことが多いので、TextHttpResponse には、JSONObject または JSONArray を受け取るコンストラクタを用意しています。

`GET /photo` および、`GET /video` コマンドの Route は、正規表現を使って`?id=アイテムid` という形式を含むリクエスト文字列のパターンマッチングを行います。また、実際のURLなど、リクエストとして受け取った情報は、`HttpRequest` 型の引数として lambdaに渡ってきており、`QueryParams` クラスに HttpRequest#`url` を渡して、id パラメータを取得しています。

`photo` の方は、`FileHttpResponse` を使って、単純にファイルを返しますが、`video` の方は、HttpRequest のヘッダ情報をチェックして、Range ヘッダの指定通りに応答を返せるよう、
`StreamingHttpResponse` を使用しています。これにより、例えば Webブラウザの &lt;video> タグで表示した場合などに、効率よく再生・シークが行われます。

尚、動画ファイルを効率よく再生するには、`Fast Start` (moov atom をファイルの先頭に配置) と呼ばれる処理が必要です。通常スマホで撮影した動画は、moov atom がファイルの末尾に配置されており、ブラウザで表示すると、一旦、ファイル全体のダウンロードが走ってしまい、Rangeによる効果が十分得られません。Android端末で動画ファイルの Fast Start を行うには、[android-media-processor](https://github.com/toyota-m2k/android-media-processor?tab=readme-ov-file#faststart) の利用をご検討ください。

最後の `PUT /star` コマンドの　Route は、PUT / POST メソッドに対する応答の例です。ここでは、渡された `HttpRequest` から、`contentAsJson()` メソッドで JSONObject として、データボディを取得して使用しています。現在、リクエストのデータボディは、JSON または、us-ascii の String (`contentAsString()`) として取り出すことができます。それ以外のデータ形式を扱う場合は、`ByteArray`型の、HttpRequest#`content` を使用してください。




