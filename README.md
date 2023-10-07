ZIO HTTP Client Perf
--------------------

Results:

`curl` - `https://google.com
 - 1 req = 412ms
 - 10 sequential = 111ms / req

`java.net.http.HttpClient` - `https://google.com` (No ZIO - baseline)
 - 1 req = 1050ms
 - 10 sequential = 518ms / req

`Client.default` - `http://google.com`
 - 1 req = 3052ms
 - 10 sequential = 482ms / req

`Client.default` - `https://google.com`
 - 1 req = 2765ms
 - 10 sequential = 346ms / req

`FixedConnectionPool size 10` - `https://google.com`
 - 1 req = 2605ms
 - 10 sequential = 350ms / req
