ZIO HTTP Client Perf
--------------------

Results:

```
# 3.0.0-RC2
[info] 1 https curl client = 127ms / req
[info] 10 https curl client = 627ms / req
[info] 1 https jdk client = 451ms / req
[info] 10 https jdk client = 31ms / req
[info] 1 https netty epoll client = 362ms / req
[info] 10 https netty epoll client = 66ms / req
[info] 1 https netty nio client = 69ms / req
[info] 10 https netty nio client = 64ms / req
[info] 1 http zio = 2410ms / req
[info] 10 http zio = 266ms / req
[info] 1 https zio = 2352ms / req
[info] 10 https zio = 256ms / req
[info] 1 https zio fixed pool = 2186ms / req
[info] 10 https zio fixed pool = 255ms / req

# 3.0.0-RC2+73-6411c97a-SNAPSHOT
[info] 1 https curl client = 186ms / req
[info] 10 https curl client = 223ms / req
[info] 1 https jdk client = 1230ms / req
[info] 10 https jdk client = 51ms / req
[info] 1 https netty epoll client = 499ms / req
[info] 10 https netty epoll client = 80ms / req
[info] 1 https netty nio client = 75ms / req
[info] 10 https netty nio client = 70ms / req
[info] 1 http zio = 666ms / req
[info] 10 http zio = 74ms / req
[info] 1 https zio = 237ms / req
[info] 10 https zio = 64ms / req
[info] 1 https zio fixed pool = 2160ms / req
[info] 10 https zio fixed pool = 350ms / req
```
