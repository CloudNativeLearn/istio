# 基于istio的微服务demo

## 1 简介

后端为 java go  python 三种语言实现的微服务demo，利用istio实现 服务发现 熔断 链路追踪等操作

!!! 所有镜像已上传dockerhub  k8s可以直接使用yaml文件

```bash
网关 -> java服务  -> python服务 v1
    \
      -> python服务 
```

开始前先将 k8s相关yaml 部署完成

测试地址:  http://106.13.114.80:32347/java   http://106.13.114.80:32347/go

kiali地址: http://106.13.114.80:32688/kiali  admin/admin （服务网格可视化与链路追踪）

grafana地址: http://106.13.114.80:31985/d/G8wLrJIZk/istio-mesh-dashboard

EFK地址 : 已关闭

Zipkin: 已关闭



## 2 服务发现

直接使用dns的解析 查找service 

例如在java项目中 java查找python项目地址的方式为 http://service3python.default.svc:7777

**!!! istio可以注入服务 例如用本集群作为网关 连接其他集群的某个服务** 

https://www.servicemesher.com/istio-handbook/practice/integration-registry.html

## 3  网关设计

```bash
网关  -> 路径为 /java  -> 跳转 java服务
    \
      -> 路径为 /go -> 跳转go服务
```

测试地址：

http://106.13.114.80:32347/java

http://106.13.114.80:32347/go

需要部署的文件

```sheel
kubectl apply -f 虚拟服务/1 网关/gatway-service-无版本.yaml
# 这个yaml文件中定义了一个 Gateway 可以通过Gateway 访问
# VirtualService 为一个虚拟服务 该虚拟服务连接之后两个服务
```

## 5  指定服务版本

需要部署的文件

1 规则/destionationRule-service.yaml

```shell
kubectl apply -f destionationRule-service.yaml
# 这个文件中记录了相应的路由规则  可以理解为service有哪些版本
```

2 网关/gatway-service-v1.yaml

```powershell
kubectl apply -f gatway-service-v1.yaml
# 这个文件部署之后 /java /go 都会进入v1版本
```

3 各个服务/every-service-go1-java1-py1.yaml

```powershell
kubectl apply -f every-service-go1-java1-py1.yaml
# 这个部署之后 go服务v1  java服务v1  python服务v1
```

4 调用之后版本改变 而且固定

http://106.13.114.80:32347/java

http://106.13.114.80:32347/go



## 6 流量镜像

需要部署的文件

网关/gatway-service-v1-java流量镜像.yaml

```shell
kubectl apply -f gatway-service-v1-java流量镜像.yaml
# 这个部署之后 java服务会调用v1 但是也会把流量原封不动复制一份发送到v2
```

效果:

在kiali: 中会发现有一个没有经过网关的java流量直接进入python服务 表示该流量操作是直接复制

## 7 延时返回

需要部署的文件

各个服务/every-service-v1-python2s延时.yaml

```shell
kubectl apply -f every-service-v1-python2s延时.yaml
# 使用该文件之后   调用/java路径服务时候 会有明显卡顿
```

http://106.13.114.80:32347/java



## 8 超时设置

需要部署的文件

网关/gatway-service-v1-java流量镜像-0.5s请求超时.yaml

```shell
kubectl apply -f gatway-service-v1-java流量镜像-0.5s请求超时.yaml
# 使用该文件之后 因为python服务存在两秒延时   java服务0.5就断开 所以调用/java路径时候 会直接无内容返回
```

http://106.13.114.80:32347/java

## 9 熔断

需要部署的文件

网关/gatway-service-v1-熔断.yaml

```shell
kubectl apply -f gatway-service-v1-熔断.yaml
#在请求/java路径服务的时候 并发量超过yi的时候就回直接熔断
```

## 10 故障注入

需要部署的文件

网关/gatway-service-v1-故障注入.yaml

```shell
kubectl apply -f gatway-service-v1-故障注入.yaml
#  0.1表示有千分之一的请求被注入故障， 400 表示故障为该请求的 HTTP 响应码为 400 
# 会随机注入故障 人为注入故障
```

## 11 分布式追踪增强

1 OpenTracing

```txt
实现分布式追踪（Distributed Tracing）的方式一般是在程序代码中进行埋点，采集调用的相关信息后发送到后端的一个追踪服务器进行分析处理。在这种实现方式中，应用代码需要依赖于追踪服务器的 API，导致业务逻辑和追踪的逻辑耦合。为了解决该问题，CNCF （云原生计算基金会）下的 OpenTracing 项目定义了一套分布式追踪的标准，以统一各种分布式追踪实现的实现。OpenTracing 中包含了一套分布式追踪的标准规范，各种语言的 API，以及实现了该标准的编程框架和函数库。
目前已有大量支持 OpenTracing 规范的 Tracer 实现，包括 Jager、Skywalking、LightStep 等。在微服务应用中采用 OpenTracing API 实现分布式追踪，可以避免厂商锁定，能够以较小的代价和任意一个兼容 OpenTracing 的分布式追踪后端 （Tracer） 进行对接。
```

https://www.servicemesher.com/istio-handbook/practice/opentracing.html

2 实现方法级的调用跟踪

```txt
Istio 为微服务提供了开箱即用的分布式追踪功能。在安装了 Istio 的微服务系统中， Sidecar 会拦截服务的入向和出向请求，为微服务的每个 HTTP 远程调用请求自动生成调用跟踪数据。通过在服务网格中接入一个分布式跟踪的后端，例如 zipkin 或者 Jaeger ，就可以查看一个分布式调用请求的端到端详细内容，例如该请求经过了哪些服务，调用了哪个 REST 接口，每个 REST 接口所花费的时间等。

在某些情况下，进程/服务级别的调用跟踪信息有可能不足以分析系统中的问题。例如分析导致客户端调用耗时过长的原因时，我们可以通过 Istio 提供的分布式追踪找到导致瓶颈的微服务进程，但无法进一步找到导致该问题的程序模块和方法。在这种情况下，我们就需要用到进程内方法级的调用跟踪来对调用链进行更细粒度的分析。本文将介绍如何在 Istio 的分布式追踪实现方法级别的调用链。
```

https://www.servicemesher.com/istio-handbook/practice/method-level-tracing.html

3 实现 Kafka 消息跟踪

```txt
在实际项目中，除了同步调用之外，异步消息也是微服务架构中常用的一种通信方式。在本节中，我将继续利用 eshop demo 程序来探讨如何通过 Opentracing 将 Kafka 异步消息也纳入到 Istio 的分布式调用追踪中。
```

https://www.servicemesher.com/istio-handbook/practice/kafka-tracing.html

## 12安全

### 授权

Istio 的授权功能为网格中的工作负载提供网格、命名空间和工作负载级别的访问控制。这种控制层级提供了以下优点：

- 工作负载间和最终用户到工作负载的授权。
- 一个简单的 API：它包括一个单独的并且很容易使用和维护的 [`AuthorizationPolicy` CRD](https://istio.io/latest/zh/docs/reference/config/security/authorization-policy/)。
- 灵活的语义：运维人员可以在 Istio 属性上定义自定义条件，并使用 DENY 和 ALLOW 动作。
- 高性能：Istio 授权是在 Envoy 本地强制执行的。
- 高兼容性：原生支持 HTTP、HTTPS 和 HTTP2，以及任意普通 TCP 协议。

### 认证

Istio 提供两种类型的认证：

- Peer authentication：用于服务到服务的认证，以验证进行连接的客户端。Istio 提供[双向 TLS](https://en.wikipedia.org/wiki/Mutual_authentication) 作为传输认证的全栈解决方案，无需更改服务代码就可以启用它。这个解决方案：
  - 为每个服务提供强大的身份，表示其角色，以实现跨群集和云的互操作性。
  - 保护服务到服务的通信。
  - 提供密钥管理系统，以自动进行密钥和证书的生成，分发和轮换。
- Request authentication：用于最终用户认证，以验证附加到请求的凭据。 Istio 使用 JSON Web Token（JWT）验证启用请求级认证，并使用自定义认证实现或任何 OpenID Connect 的认证实现（例如下面列举的）来简化的开发人员体验。
  - [ORY Hydra](https://www.ory.sh/)
  - [Keycloak](https://www.keycloak.org/)
  - [Auth0](https://auth0.com/)
  - [Firebase Auth](https://firebase.google.com/docs/auth/)
  - [Google Auth](https://developers.google.com/identity/protocols/OpenIDConnect)

在所有情况下，Istio 都通过自定义 Kubernetes API 将认证策略存储在 `Istio config store`。Istiod 使每个代理保持最新状态，并在适当时提供密钥。此外，Istio 的认证机制支持宽容模式（permissive mode），以帮助您了解策略更改在实施之前如何影响您的安全状况。

## 13 策略

### 启动速率限制

在这部分，Istio 将以客户端的 IP 地址限制 `productpage` 的流量。 您将会使用 `X-Forwarded-For` 请求头作为客户端 IP 地址。也将会针对已登录的用户应用有条件的速率限制。

(内部有计数器可以统计)

### 请求头和路由控制

可以使用匹配规则,利用请求头中的内容做限制.

### Denials和黑名单

此任务说明了如何使用简单的 denials、基于属性的黑白名单、基于 IP 的黑白名单来控制对服务的访问。

## 14 日志相关

与k8s不同点:  istio基于envoy,每次访问pod前都要进入一个envoy镜像,所有可以考虑收集所有envoy的日志作为日志收集方案



