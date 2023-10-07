ZIO HTTP Client Perf
--------------------

Results:

```
[info] 1 https curl client = 636ms / req
[info] 10 https curl client = 537ms / req
[info] 1 https jdk client = 1006ms / req
[info] 10 https jdk client = 155ms / req
[info] 1 https netty epoll client = 359ms / req
[info] 10 https netty epoll client = 578ms / req
[info] 1 https netty nio client = 242ms / req
[info] 10 https netty nio client = 533ms / req
[info] 1 http zio = 2879ms / req
[info] 10 http zio = 457ms / req
[info] 1 https zio = 2738ms / req
[info] 10 https zio = 348ms / req
[info] 1 https zio fixed pool = 2742ms / req
[info] 10 https zio fixed pool = 372ms / req
```
