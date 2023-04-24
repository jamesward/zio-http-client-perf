ZIO HTTP Client Perf
--------------------

Results:

`Client.default` - `http://google.com`
 - 1 req = 2787ms
 - 10 sequential = 813ms / req

`Client.default` - `https://google.com`
 - 1 req = 3038ms
 - 10 sequential = 896ms / req

`FixedConnectionPool size 10` - `https://google.com`
 - 1 req = 3105ms
 - 10 sequential = 560ms / req
